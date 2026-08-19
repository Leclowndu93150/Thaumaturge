package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class BlockEntityMirrorBase extends BlockEntity {
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
        target.sync();
        this.linked = true;
        onLinkRestored(target);
        setChanged();
        target.setChanged();
        sync();
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
            target.sync();
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
        return level != null && target.link != null && target.link.pos().equals(worldPosition) && target.link.dimension() == level.dimension();
    }

    public boolean isDestinationValid() {
        BlockEntityMirrorBase target = target();
        if (target == null) {
            linked = false;
            setChanged();
            sync();
            return false;
        }
        return !target.isLinkValid();
    }

    private void breakLink() {
        linked = false;
        setChanged();
        sync();
    }

    protected void addInstability(int amount) {
        instability += amount;
        setChanged();
    }

    protected void tickLink() {
        checkInstability();
        if (count++ % inc == 0) {
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

    protected void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        linked = input.getBooleanOr("linked", false);
        instability = input.getIntOr("instability", 0);
        long pos = input.getLongOr("linkPos", 0L);
        String dim = input.getStringOr("linkDim", "");
        if (!dim.isEmpty()) {
            Identifier dimId = Identifier.tryParse(dim);
            if (dimId != null) {
                link = GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimId), BlockPos.of(pos));
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("linked", linked);
        output.putInt("instability", instability);
        if (link != null) {
            output.putLong("linkPos", link.pos().asLong());
            output.putString("linkDim", link.dimension().identifier().toString());
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (linked && link != null) {
            builder.set(TCDataComponents.MIRROR_LINK.get(), link);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        GlobalPos stored = components.get(TCDataComponents.MIRROR_LINK.get());
        if (stored != null) {
            link = stored;
            linked = false;
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (linked) {
            invalidateLink();
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
