package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaIntake;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaIntakeHost;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class BlockEntityLampGrowth extends BlockEntity implements EssentiaIntakeHost {
    private static final int MAX_CHARGES = 20;
    private static final int SCAN_DISTANCE = 6;
    private static final int SUCTION = 128;
    private static final int DRAW_INTERVAL = 5;

    private final EssentiaIntake intake = new EssentiaIntake(this, TCAspects.HERBA, SUCTION, DRAW_INTERVAL);
    private boolean reserve;
    private int charges = -1;
    private BlockPos lastTarget = BlockPos.ZERO;
    private BlockState lastTargetState = null;
    private final List<BlockPos> checklist = new ArrayList<>();

    public BlockEntityLampGrowth(BlockPos pos, BlockState state) {
        super(TCBlockEntities.LAMP_GROWTH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityLampGrowth lamp) {
        boolean powered = level.hasNeighborSignal(pos);
        if (lamp.charges <= 0) {
            if (lamp.reserve) {
                lamp.charges = MAX_CHARGES;
                lamp.reserve = false;
                lamp.setChanged();
            } else if (lamp.intake.pullOne()) {
                lamp.charges = MAX_CHARGES;
                lamp.setChanged();
            }
        }
        BlockLamp.showLit(level, pos, state, lamp.charges > 0 && !powered);
        if (!lamp.reserve && lamp.intake.pullOne()) {
            lamp.reserve = true;
            lamp.setChanged();
        }
        if (lamp.charges == 0) {
            lamp.charges = -1;
        }
        if (!powered && lamp.charges > 0) {
            lamp.updatePlant();
        }
    }

    private void updatePlant() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        BlockState current = server.getBlockState(lastTarget);
        if (lastTargetState != null && current != lastTargetState) {
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, lastTarget.getX() + 0.5, lastTarget.getY() + 0.5, lastTarget.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.0);
            lastTargetState = current;
        }
        if (checklist.isEmpty()) {
            for (int a = -SCAN_DISTANCE; a <= SCAN_DISTANCE; a++) {
                for (int b = -SCAN_DISTANCE; b <= SCAN_DISTANCE; b++) {
                    checklist.add(getBlockPos().offset(a, SCAN_DISTANCE, b));
                }
            }
            Collections.shuffle(checklist);
        }
        BlockPos column = checklist.removeFirst();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(column.getX(), column.getY(), column.getZ());
        while (cursor.getY() >= getBlockPos().getY() - SCAN_DISTANCE) {
            BlockState state = server.getBlockState(cursor);
            if (!state.isAir() && isPlant(state) && cursor.distToCenterSqr(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5) < SCAN_DISTANCE * SCAN_DISTANCE
                    && !isGrownCrop(server, cursor, state) && !state.is(TCBlockTags.LAMP_GROWTH_BLACKLIST)) {
                charges--;
                BlockPos target = cursor.immutable();
                lastTarget = target;
                lastTargetState = state;
                if (state.isRandomlyTicking()) {
                    state.randomTick(server, target, server.getRandom());
                }
                setChanged();
                return;
            }
            cursor.move(0, -1, 0);
        }
    }

    private static boolean isPlant(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof GrassBlock) {
            return false;
        }
        return block instanceof BonemealableBlock || block instanceof CactusBlock || block instanceof SugarCaneBlock || block instanceof NetherWartBlock;
    }

    private static boolean isGrownCrop(ServerLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        }
        if (block instanceof StemBlock) {
            return false;
        }
        if (block instanceof BonemealableBlock growable) {
            return !growable.isValidBonemealTarget(level, pos, state);
        }
        return false;
    }

    public EssentiaIntake intake() {
        return intake;
    }

    @Override
    public Direction intakeFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public boolean wantsEssentia() {
        return !reserve || charges <= 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        reserve = input.getBooleanOr("reserve", false);
        charges = input.getIntOr("charges", -1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("reserve", reserve);
        output.putInt("charges", charges);
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
