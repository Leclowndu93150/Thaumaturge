package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityNodeTransducer extends BlockEntity {
    public static final int CHARGE_TARGET = 1000;
    public static final int REVERT_THRESHOLD = 50;

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_NODE = 1;
    private static final int STATUS_ENERGIZED = 2;
    private static final int RECHECK_INTERVAL = 40;
    private static final int SYNC_INTERVAL = 10;
    private static final int NOT_SYNCED = Integer.MIN_VALUE;
    private static final int BOLT_INTERVAL = 10;
    private static final float BOLT_WIDTH = 0.06F;
    private static final int UNSET_COUNT = -1;
    private static final double BLOCK_CENTRE = 0.5;
    private static final double TOP_BOLT_HEIGHT = 0.8;
    private static final double UNDERSIDE_BOLT_HEIGHT = 0.1;

    private int count = UNSET_COUNT;
    private int status = STATUS_IDLE;
    private int syncedCount = NOT_SYNCED;
    private int syncedStatus = NOT_SYNCED;
    private float animationFromCount;
    private long animationStartTick;
    private boolean animationInitialized;

    public BlockEntityNodeTransducer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.NODE_TRANSDUCER.get(), pos, state);
    }

    public int getCount() {
        return Math.max(0, count);
    }

    public int getStatus() {
        return status;
    }

    public float getRenderCount(float partialTick) {
        if (level == null || !level.isClientSide() || !animationInitialized) {
            return getCount();
        }
        float progress =
                Mth.clamp((level.getGameTime() - animationStartTick + partialTick) / SYNC_INTERVAL, 0.0F, 1.0F);
        return Mth.lerp(progress, animationFromCount, getCount());
    }

    public void serverTick(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos nodePos = pos.below();
        BlockEntityNode node = serverLevel.getBlockEntity(nodePos) instanceof BlockEntityNode found ? found : null;
        boolean stabilized = hasStabilizer(serverLevel, pos);
        long time = serverLevel.getGameTime();
        int previousCount = count;
        int previousStatus = status;
        boolean lostStabilizer = false;
        if (count == UNSET_COUNT || time % RECHECK_INTERVAL == 0) {
            if (node != null && node.isEnergized() && !stabilized) {
                catastrophicFailure(serverLevel, nodePos);
                node = null;
                lostStabilizer = true;
                status = STATUS_IDLE;
                count = REVERT_THRESHOLD;
            } else {
                checkStatus(node);
            }
        }
        RandomSource random = serverLevel.getRandom();
        if (!lostStabilizer) {
            charge(node, serverLevel.hasNeighborSignal(pos), stabilized, random);
        }
        if (node != null && status == STATUS_NODE && count >= CHARGE_TARGET && !node.isEnergized()) {
            node.setEnergized(true);
            status = STATUS_ENERGIZED;
        }
        if (node != null && node.isEnergized() && count <= REVERT_THRESHOLD) {
            node.setEnergized(false);
            List<AspectInstance> stored = node.getAspects().entries();
            for (AspectInstance entry : stored) {
                node.drain(entry.aspect(), entry.amount());
            }
            status = STATUS_IDLE;
        }
        if (count != previousCount || status != previousStatus) {
            setChanged();
        }
        if (time % SYNC_INTERVAL == 0 && (count != syncedCount || status != syncedStatus)) {
            syncedCount = count;
            syncedStatus = status;
            serverLevel.sendBlockUpdated(pos, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
        if (node != null && count > REVERT_THRESHOLD && count < CHARGE_TARGET && time % BOLT_INTERVAL == 0) {
            showBolts(serverLevel, pos, nodePos, stabilized, random);
        }
    }

    private void charge(@Nullable BlockEntityNode node, boolean powered, boolean stabilized, RandomSource random) {
        if (node == null || status == STATUS_IDLE || !powered) {
            if (count > 0) {
                count--;
            }
        } else if (stabilized && count < CHARGE_TARGET) {
            count++;
            if (status == STATUS_NODE) {
                drainOnePoint(node, random);
            }
        }
    }

    private static void drainOnePoint(BlockEntityNode node, RandomSource random) {
        List<AspectInstance> entries = node.getAspects().entries();
        if (!entries.isEmpty()) {
            node.drain(entries.get(random.nextInt(entries.size())).aspect(), 1);
        }
    }

    private static void showBolts(
            ServerLevel level, BlockPos pos, BlockPos nodePos, boolean stabilized, RandomSource random) {
        Vec3 nodeCentre = Vec3.atCenterOf(nodePos);
        if (random.nextBoolean()) {
            bolt(level, pos, TOP_BOLT_HEIGHT, nodeCentre);
        }
        if (stabilized && random.nextBoolean()) {
            bolt(level, pos, UNDERSIDE_BOLT_HEIGHT, nodeCentre);
        }
    }

    private static void bolt(ServerLevel level, BlockPos pos, double height, Vec3 target) {
        Vec3 origin = new Vec3(pos.getX() + BLOCK_CENTRE, pos.getY() + height, pos.getZ() + BLOCK_CENTRE);
        Effects.arcBolt(level, origin).to(target).width(BOLT_WIDTH).send();
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private void checkStatus(@Nullable BlockEntityNode node) {
        if (node == null) {
            status = STATUS_IDLE;
            count = 0;
            return;
        }
        boolean wasEnergized = status == STATUS_ENERGIZED;
        status = node.isEnergized() ? STATUS_ENERGIZED : STATUS_NODE;
        if (node.isEnergized() && (!wasEnergized || count == UNSET_COUNT)) {
            count = CHARGE_TARGET;
        } else {
            count = Math.max(0, count);
        }
    }

    private static void catastrophicFailure(ServerLevel level, BlockPos nodePos) {
        AuraHelper.polluteAura(level, nodePos, 32.0F, true);
        level.removeBlock(nodePos, false);
        Vec3 centre = Vec3.atCenterOf(nodePos);
        level.explode(null, centre.x, centre.y, centre.z, 3.0F, Level.ExplosionInteraction.NONE);
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 50; attempt++) {
            BlockPos target = nodePos.offset(
                    random.nextInt(8) - random.nextInt(8),
                    random.nextInt(8) - random.nextInt(8),
                    random.nextInt(8) - random.nextInt(8));
            if (!level.hasChunkAt(target)) {
                continue;
            }
            if (target.getY() < nodePos.getY()) {
                PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA);
            } else {
                PhysicalFlux.placeGas(level, target, PhysicalFlux.MAX_QUANTA);
            }
        }
    }

    private static boolean hasStabilizer(Level level, BlockPos pos) {
        BlockPos stabilizerPos = pos.below(2);
        return level.getBlockEntity(stabilizerPos) instanceof BlockEntityNodeStabilizer
                && !level.hasNeighborSignal(stabilizerPos);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putInt("Count", count);
        output.putInt("Status", status);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        float previousRenderCount = getRenderCount(0.0F);
        super.loadAdditional(input, registries);
        count = input.contains("Count") ? input.getInt("Count") : UNSET_COUNT;
        status = input.getInt("Status");
        if (level != null && level.isClientSide()) {
            animationFromCount = animationInitialized ? previousRenderCount : getCount();
            animationStartTick = level.getGameTime();
            animationInitialized = true;
        }
    }
}
