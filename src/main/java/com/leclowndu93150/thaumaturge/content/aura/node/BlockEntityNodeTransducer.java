package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityNodeTransducer extends BlockEntity {
    public static final int CHARGE_TARGET = 1000;
    public static final int REVERT_THRESHOLD = 50;

    private static final String KEY_COUNT = "Count";
    private static final String KEY_STATUS = "Status";
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_NODE = 1;
    private static final int STATUS_ENERGIZED = 2;
    private static final int UNSET_COUNT = -1;
    private static final int NEVER_SENT = Integer.MIN_VALUE;
    private static final int LOST_STABILIZER_COUNT = 50;
    private static final int STATUS_RECHECK_INTERVAL = 40;
    private static final int SYNC_INTERVAL = 10;
    private static final int BOLT_INTERVAL = 10;
    private static final float BOLT_WIDTH = 0.06F;
    private static final double BLOCK_CENTER = 0.5;
    private static final double TOP_BOLT_HEIGHT = 0.8;
    private static final double UNDERSIDE_BOLT_HEIGHT = 0.1;
    private static final int STABILIZER_DEPTH = 2;
    private static final float CATASTROPHE_FLUX = 32.0F;
    private static final float BLAST_STRENGTH = 3.0F;
    private static final int FLUX_SCATTER_TRIES = 50;
    private static final int FLUX_SCATTER_REACH = 7;
    private static final float FLARE_SIZE = 0.8F;
    private static final float FLARE_VOLUME = 1.0F;
    private static final float FLARE_PITCH = 1.0F;

    private int count = UNSET_COUNT;
    private int status = STATUS_IDLE;
    private int syncedCount = NEVER_SENT;
    private int syncedStatus = NEVER_SENT;

    public BlockEntityNodeTransducer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.NODE_TRANSDUCER.get(), pos, state);
    }

    public int getCount() {
        return Math.max(0, count);
    }

    public int getStatus() {
        return status;
    }

    public void serverTick(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos nodePos = pos.below();
        BlockEntityNode node = serverLevel.getBlockEntity(nodePos) instanceof BlockEntityNode found ? found : null;
        BlockPos stabilizerPos = pos.below(STABILIZER_DEPTH);
        boolean stabilizerPresent = serverLevel.getBlockEntity(stabilizerPos) instanceof BlockEntityNodeStabilizer && !serverLevel.hasNeighborSignal(stabilizerPos);
        boolean powered = serverLevel.hasNeighborSignal(pos);
        long time = serverLevel.getGameTime();
        int previousCount = count;
        int previousStatus = status;
        boolean lostStabilizer = false;
        if (count == UNSET_COUNT || time % STATUS_RECHECK_INTERVAL == 0) {
            if (node != null && node.isEnergized() && !stabilizerPresent) {
                catastrophe(serverLevel, nodePos);
                node = null;
                lostStabilizer = true;
                status = STATUS_IDLE;
                count = LOST_STABILIZER_COUNT;
            } else {
                followNode(node);
            }
        }
        RandomSource random = serverLevel.getRandom();
        if (!lostStabilizer) {
            charge(node, powered, stabilizerPresent, random);
        }
        if (node != null && status == STATUS_NODE && count >= CHARGE_TARGET && !node.isEnergized()) {
            node.setEnergized(true);
            status = STATUS_ENERGIZED;
            flare(serverLevel, nodePos);
        }
        if (node != null && node.isEnergized() && count <= REVERT_THRESHOLD) {
            node.setEnergized(false);
            node.clearContained();
            status = STATUS_IDLE;
            flare(serverLevel, nodePos);
        }
        if (count != previousCount || status != previousStatus) {
            setChanged();
        }
        if (time % SYNC_INTERVAL == 0 && (count != syncedCount || status != syncedStatus)) {
            syncedCount = count;
            syncedStatus = status;
            BlockState state = getBlockState();
            serverLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        if (node != null && count > REVERT_THRESHOLD && count < CHARGE_TARGET && time % BOLT_INTERVAL == 0) {
            showBolts(serverLevel, pos, nodePos, stabilizerPresent, random);
        }
    }

    private static void catastrophe(ServerLevel level, BlockPos nodePos) {
        if (level.getBlockEntity(nodePos) instanceof BlockEntityNode node) {
            node.removeDepleted(level, nodePos);
        }
        Vec3 centre = Vec3.atCenterOf(nodePos);
        level.explode(null, centre.x, centre.y, centre.z, BLAST_STRENGTH, false, Level.ExplosionInteraction.BLOCK);
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < FLUX_SCATTER_TRIES; attempt++) {
            BlockPos spot = NodeRules.scatter(nodePos, FLUX_SCATTER_REACH, random);
            if (level.hasChunkAt(spot) && level.isEmptyBlock(spot)) {
                fillWithFlux(level, spot, spot.getY() < nodePos.getY());
            }
        }
        AuraHelper.addFlux(level, nodePos, CATASTROPHE_FLUX);
    }

    private static void fillWithFlux(ServerLevel level, BlockPos spot, boolean belowNode) {
        if (belowNode) {
            PhysicalFlux.placeGoo(level, spot, PhysicalFlux.MAX_QUANTA);
        } else {
            PhysicalFlux.placeGas(level, spot, PhysicalFlux.MAX_QUANTA);
        }
    }

    private static void flare(ServerLevel level, BlockPos nodePos) {
        Effects.burst(level, Vec3.atCenterOf(nodePos)).size(FLARE_SIZE).send();
        level.playSound(null, nodePos, TTSounds.CRAFTFAIL.get(), SoundSource.BLOCKS, FLARE_VOLUME, FLARE_PITCH);
    }

    private void charge(@Nullable BlockEntityNode node, boolean powered, boolean stabilizerPresent, RandomSource random) {
        if (node == null || status == STATUS_IDLE || !powered) {
            if (count > 0) {
                count--;
            }
        } else if (stabilizerPresent && count < CHARGE_TARGET) {
            count++;
            if (status == STATUS_NODE) {
                drainOnePoint(node, random);
            }
        }
    }

    private void followNode(@Nullable BlockEntityNode node) {
        int next = node == null ? STATUS_IDLE : node.isEnergized() ? STATUS_ENERGIZED : STATUS_NODE;
        if (next == STATUS_IDLE) {
            count = 0;
        } else if (next == STATUS_ENERGIZED && (status != STATUS_ENERGIZED || count == UNSET_COUNT)) {
            count = CHARGE_TARGET;
        } else if (count == UNSET_COUNT) {
            count = 0;
        }
        status = next;
    }

    private static void drainOnePoint(BlockEntityNode node, RandomSource random) {
        AspectList contained = node.getAspects();
        List<AspectInstance> entries = contained.entries();
        if (!entries.isEmpty()) {
            node.drain(entries.get(random.nextInt(entries.size())).aspect(), 1);
        }
    }

    private static void showBolts(ServerLevel level, BlockPos pos, BlockPos nodePos, boolean stabilizerPresent, RandomSource random) {
        Vec3 nodeCenter = Vec3.atCenterOf(nodePos);
        if (random.nextBoolean()) {
            Effects.boltStrike(level, new Vec3(pos.getX() + BLOCK_CENTER, pos.getY() + TOP_BOLT_HEIGHT, pos.getZ() + BLOCK_CENTER)).to(nodeCenter).width(BOLT_WIDTH).send();
        }
        if (stabilizerPresent && random.nextBoolean()) {
            Effects.boltStrike(level, new Vec3(pos.getX() + BLOCK_CENTER, pos.getY() + UNDERSIDE_BOLT_HEIGHT, pos.getZ() + BLOCK_CENTER)).to(nodeCenter).width(BOLT_WIDTH).send();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(KEY_COUNT, count);
        output.putInt(KEY_STATUS, status);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        count = input.getIntOr(KEY_COUNT, UNSET_COUNT);
        status = input.getIntOr(KEY_STATUS, STATUS_IDLE);
    }
}
