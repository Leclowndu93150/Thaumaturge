package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class BlockEntityMirrorBase extends AbstractSyncedBlockEntity {
    private static final String LINKED_KEY = "linked";
    private static final String INSTABILITY_KEY = "instability";
    private static final String LINK_POS_KEY = "linkPos";
    private static final String LINK_DIM_KEY = "linkDim";
    private static final int BASE_INTERVAL = 40;
    private static final int INTERVAL_STEP = 20;
    private static final int MAX_INTERVAL = 600;
    private static final int DECAY_INTERVAL = 100;
    private static final float FLUX_PER_OVERFLOW = 1.0F;

    public boolean paired;
    public @Nullable GlobalPos link;
    public int stress;
    protected int count;
    protected int inc = BASE_INTERVAL;

    protected BlockEntityMirrorBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract int instabilityThreshold();

    protected abstract boolean isSameKind(BlockEntity other);

    protected @Nullable BlockEntityMirrorBase partner() {
        GlobalPos destination = link;
        ServerLevel destinationLevel = targetLevel();
        if (destination == null || destinationLevel == null) {
            return null;
        }
        BlockEntity found = destinationLevel.getBlockEntity(destination.pos());
        if (found == null || !isSameKind(found)) {
            return null;
        }
        return (BlockEntityMirrorBase) found;
    }

    protected @Nullable ServerLevel targetLevel() {
        if (link == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getLevel(link.dimension());
    }

    public void reconnect() {
        if (level == null || !acceptsPartner()) {
            return;
        }
        BlockEntityMirrorBase other = partner();
        GlobalPos here = globalPosition();
        if (other == null || here == null) {
            return;
        }
        paired = true;
        other.paired = true;
        other.link = here;
        onLinkRestored(other);
        other.onLinkRestored(this);
        setChangedAndSync();
        other.setChangedAndSync();
    }

    protected void onLinkRestored(BlockEntityMirrorBase other) {}

    public void severPartner() {
        if (!partnerAvailable()) {
            return;
        }
        BlockEntityMirrorBase other = partner();
        if (other == null || !other.pointsAt(this)) {
            return;
        }
        other.paired = false;
        other.setChangedAndSync();
    }

    public boolean verifyPairing() {
        if (pairingIntact()) {
            return true;
        }
        if (paired && (link == null || partnerAvailable())) {
            paired = false;
            setChangedAndSync();
        }
        return false;
    }

    public boolean pairingIntact() {
        if (!paired || link == null) {
            return false;
        }
        BlockEntityMirrorBase partner = partner();
        return partner != null && partner.paired && partner.pointsAt(this);
    }

    public boolean acceptsPartner() {
        BlockEntityMirrorBase partner = partner();
        boolean valid = partner != null && !(partner.pairingIntact() && !partner.pointsAt(this));
        if (!valid && paired) {
            paired = false;
            setChangedAndSync();
        }
        return valid;
    }

    protected void pileOn(int amount) {
        stress += amount;
        setChanged();
        shedExcess();
    }

    protected void tickLink() {
        if (level == null) {
            return;
        }
        decayStress(level.getGameTime());
        if (link != null) {
            pollPartner();
        }
    }

    private void decayStress(long gameTime) {
        if (stress > 0 && gameTime % DECAY_INTERVAL == 0) {
            stress--;
            setChanged();
        }
    }

    private void pollPartner() {
        count++;
        if (count < inc || !partnerAvailable()) {
            return;
        }
        count = 0;
        if (verifyPairing()) {
            inc = BASE_INTERVAL;
            return;
        }
        reconnect();
        inc = Math.min(MAX_INTERVAL, inc + INTERVAL_STEP);
    }

    protected void shedExcess() {
        int limit = instabilityThreshold();
        if (level == null || level.isClientSide() || stress <= limit) {
            return;
        }
        AuraHelper.polluteAura(level, worldPosition, FLUX_PER_OVERFLOW, true);
        stress -= limit;
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        stress = input.getIntOr(INSTABILITY_KEY, 0);
        paired = input.getBooleanOr(LINKED_KEY, false);
        GlobalPos restored = readLink(input);
        if (restored != null) {
            link = restored;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(INSTABILITY_KEY, stress);
        output.putBoolean(LINKED_KEY, paired);
        if (link != null) {
            writeLink(output, link);
        }
    }

    private static @Nullable GlobalPos readLink(ValueInput input) {
        Identifier dimensionId = parseDimension(input.getStringOr(LINK_DIM_KEY, ""));
        if (dimensionId == null) {
            return null;
        }
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
        return GlobalPos.of(dimensionKey, BlockPos.of(input.getLongOr(LINK_POS_KEY, 0L)));
    }

    private static void writeLink(ValueOutput output, GlobalPos target) {
        output.putLong(LINK_POS_KEY, target.pos().asLong());
        output.putString(LINK_DIM_KEY, target.dimension().identifier().toString());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (!paired || link == null) {
            return;
        }
        components.set(TTDataComponents.MIRROR_LINK.get(), link);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        Optional.ofNullable(components.get(TTDataComponents.MIRROR_LINK.get())).ifPresent(this::adoptStoredLink);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (paired && link != null) {
            severPartner();
        }
        super.preRemoveSideEffects(pos, state);
    }

    private void adoptStoredLink(GlobalPos stored) {
        paired = false;
        link = stored;
    }

    protected boolean partnerAvailable() {
        GlobalPos destination = link;
        ServerLevel destinationLevel = targetLevel();
        return destination != null && destinationLevel != null && destinationLevel.hasChunkAt(destination.pos());
    }

    private boolean pointsAt(BlockEntityMirrorBase other) {
        GlobalPos otherPosition = other.globalPosition();
        return otherPosition != null && otherPosition.equals(link);
    }

    private @Nullable GlobalPos globalPosition() {
        return level == null ? null : GlobalPos.of(level.dimension(), worldPosition);
    }

    private static @Nullable Identifier parseDimension(String name) {
        return name.isEmpty() ? null : Identifier.tryParse(name);
    }
}
