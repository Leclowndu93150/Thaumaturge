package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private static final int BOLT_INTERVAL = 10;
    private static final float BOLT_WIDTH = 0.06F;

    private int count = -1;
    private int status = STATUS_IDLE;

    public BlockEntityNodeTransducer(BlockPos pos, BlockState state) {
        super(TCBlockEntities.NODE_TRANSDUCER.get(), pos, state);
    }

    public int getCount() {
        return Math.max(0, count);
    }

    public int getStatus() {
        return status;
    }

    public void serverTick(Level level, BlockPos pos) {
        if (count == -1 || level.getGameTime() % RECHECK_INTERVAL == 0) {
            checkStatus(level, pos);
        }
        BlockEntity below = level.getBlockEntity(pos.below());
        if (status == STATUS_NODE && count >= CHARGE_TARGET && below instanceof BlockEntityNode node && !node.isEnergized()) {
            node.setEnergized(true);
            checkStatus(level, pos);
            setChanged();
        }
        if (status == STATUS_ENERGIZED && count <= REVERT_THRESHOLD && below instanceof BlockEntityNode node && node.isEnergized()) {
            node.setEnergized(false);
            List<AspectInstance> stored = node.getAspects().entries();
            for (AspectInstance entry : stored) {
                node.takeFromContainer(entry.aspect(), entry.amount());
            }
            status = STATUS_IDLE;
            setChanged();
            return;
        }
        if (status != STATUS_IDLE && level.hasNeighborSignal(pos)) {
            if (count < CHARGE_TARGET && hasStabilizer(level, pos)) {
                count++;
                if (status == STATUS_NODE && below instanceof BlockEntityNode node) {
                    AspectList stored = node.getAspects();
                    if (!stored.isEmpty()) {
                        List<AspectInstance> entries = stored.entries();
                        AspectInstance chosen = entries.get(level.getRandom().nextInt(entries.size()));
                        node.takeFromContainer(chosen.aspect(), 1);
                    }
                }
                setChanged();
            }
        } else if (count > 0) {
            count--;
            setChanged();
        }
        if (count > CHARGE_TARGET) {
            count = CHARGE_TARGET;
        }
        if (level.getGameTime() % SYNC_INTERVAL == 0) {
            level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
        }
        if (level instanceof ServerLevel serverLevel && count > REVERT_THRESHOLD && count < CHARGE_TARGET && level.getGameTime() % BOLT_INTERVAL == 0) {
            if (level.getRandom().nextBoolean()) {
                Effects.arcBolt(serverLevel, new Vec3(pos.getX() + 0.25 + level.getRandom().nextFloat() * 0.5, pos.getY() + 0.5, pos.getZ() + 0.25 + level.getRandom().nextFloat() * 0.5))
                        .to(Vec3.atCenterOf(pos.below())).width(BOLT_WIDTH).send();
            }
            if (level.getRandom().nextBoolean() && hasStabilizer(level, pos)) {
                Effects.arcBolt(serverLevel, new Vec3(pos.getX() + 0.25 + level.getRandom().nextFloat() * 0.5, pos.getY() - 1.5, pos.getZ() + 0.25 + level.getRandom().nextFloat() * 0.5))
                        .to(Vec3.atCenterOf(pos.below())).width(BOLT_WIDTH).send();
            }
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private void checkStatus(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos.below()) instanceof BlockEntityNode node) {
            boolean wasEnergized = status == STATUS_ENERGIZED;
            status = node.isEnergized() ? STATUS_ENERGIZED : STATUS_NODE;
            if (node.isEnergized() && (!wasEnergized || count == -1)) {
                count = CHARGE_TARGET;
            } else {
                count = Math.max(0, count);
            }
        } else {
            status = STATUS_IDLE;
            count = 0;
        }
    }

    private static boolean hasStabilizer(Level level, BlockPos pos) {
        BlockPos stabilizerPos = pos.below(2);
        return level.getBlockEntity(stabilizerPos) instanceof BlockEntityNodeStabilizer && !level.hasNeighborSignal(stabilizerPos);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Count", count);
        output.putInt("Status", status);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        count = input.getIntOr("Count", -1);
        status = input.getIntOr("Status", STATUS_IDLE);
    }
}
