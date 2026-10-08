package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class BlockEntityMirrorBase extends AbstractSyncedBlockEntity {
    private static final int RETRY_BASE_INTERVAL = 40;
    private static final int RETRY_MAX_INTERVAL = 600;
    private static final int RETRY_BACKOFF = 20;
    private static final int INSTABILITY_DECAY_INTERVAL = 100;

    public boolean linked;
    public @Nullable GlobalPos link;
    public int instability;
    protected int count;
    protected int inc = RETRY_BASE_INTERVAL;

    protected BlockEntityMirrorBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract int instabilityThreshold();

    protected abstract boolean isSameKind(BlockEntity target);

    protected @Nullable BlockEntityMirrorBase target() {
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null || link == null) {
            return null;
        }
        BlockEntity be = targetLevel.getBlockEntity(link.pos());
        return be instanceof BlockEntityMirrorBase mirror && isSameKind(mirror) ? mirror : null;
    }

    private boolean isTargetLoaded() {
        ServerLevel targetLevel = targetLevel();
        return targetLevel != null && link != null && targetLevel.isLoaded(link.pos());
    }

    protected @Nullable ServerLevel targetLevel() {
        if (level == null || level.isClientSide() || link == null || level.getServer() == null) {
            return null;
        }
        return level.getServer().getLevel(link.dimension());
    }

    public void restoreLink() {
        if (!isDestinationValid()) {
            return;
        }
        BlockEntityMirrorBase target = target();
        if (target == null || level == null) {
            return;
        }
        target.linked = true;
        target.link = GlobalPos.of(level.dimension(), worldPosition);
        target.onLinkRestored(this);
        target.syncToClient();
        this.linked = true;
        onLinkRestored(target);
        setChanged();
        target.setChanged();
        syncToClient();
    }

    protected void onLinkRestored(BlockEntityMirrorBase other) {}

    public void invalidateLink() {
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null || link == null || !targetLevel.hasChunkAt(link.pos())) {
            return;
        }
        BlockEntityMirrorBase target = target();
        if (target != null) {
            target.linked = false;
            setChanged();
            target.setChanged();
            target.syncToClient();
        }
    }

    public boolean isLinkValid() {
        if (!linked) {
            return false;
        }
        BlockEntityMirrorBase target = target();
        if (target == null) {
            breakLink();
            return false;
        }
        if (!target.linked) {
            breakLink();
            return false;
        }
        if (linksBackToSelf(target)) {
            return true;
        }
        breakLink();
        return false;
    }

    public boolean isLinkValidSimple() {
        if (!linked) {
            return false;
        }
        BlockEntityMirrorBase target = target();
        return target != null && target.linked && linksBackToSelf(target);
    }

    private boolean linksBackToSelf(BlockEntityMirrorBase target) {
        return level != null
                && target.link != null
                && target.link.pos().equals(worldPosition)
                && target.link.dimension() == level.dimension();
    }

    public boolean isDestinationValid() {
        BlockEntityMirrorBase target = target();
        if (target == null) {
            linked = false;
            setChanged();
            syncToClient();
            return false;
        }
        return !target.isLinkValid();
    }

    private void breakLink() {
        linked = false;
        setChanged();
        syncToClient();
    }

    protected void addInstability(int amount) {
        instability += amount;
        setChanged();
    }

    protected void tickLink() {
        checkInstability();
        if (count++ % inc == 0 && isTargetLoaded()) {
            if (!isLinkValidSimple()) {
                if (inc < RETRY_MAX_INTERVAL) {
                    inc += RETRY_BACKOFF;
                }
                restoreLink();
            } else {
                inc = RETRY_BASE_INTERVAL;
            }
        }
    }

    protected void checkInstability() {
        if (level == null) {
            return;
        }
        if (instability > instabilityThreshold()) {
            AuraHelper.polluteAura(level, worldPosition, 1.0F, true);
            instability -= instabilityThreshold();
            setChanged();
        }
        if (instability > 0 && count % INSTABILITY_DECAY_INTERVAL == 0) {
            instability--;
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        linked = input.getBoolean("linked");
        instability = input.getInt("instability");
        long pos = input.getLong("linkPos");
        String dim = input.getString("linkDim");
        if (!dim.isEmpty()) {
            ResourceLocation dimId = ResourceLocation.tryParse(dim);
            if (dimId != null) {
                link = GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimId), BlockPos.of(pos));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putBoolean("linked", linked);
        output.putInt("instability", instability);
        if (link != null) {
            output.putLong("linkPos", link.pos().asLong());
            output.putString("linkDim", link.dimension().location().toString());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (linked && link != null) {
            builder.set(TTDataComponents.MIRROR_LINK.get(), link);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        GlobalPos stored = components.get(TTDataComponents.MIRROR_LINK.get());
        if (stored != null) {
            link = stored;
            linked = false;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (linked) {
            invalidateLink();
        }
    }
}
