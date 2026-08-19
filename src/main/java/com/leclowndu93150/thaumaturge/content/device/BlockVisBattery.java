package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public final class BlockVisBattery extends Block {
    public static final MapCodec<BlockVisBattery> CODEC = simpleCodec(BlockVisBattery::new);
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 10);

    private static final int MAX_CHARGE = 10;
    private static final float TRANSFER_AMOUNT = 1.0F;
    private static final float CHARGE_AURA_FRACTION = 0.9F;
    private static final float DISCHARGE_AURA_FRACTION = 0.75F;
    private static final int POWERED_DISCHARGE_DELAY = 5;
    private static final int CHARGE_DELAY_BASE = 100;
    private static final int DISCHARGE_DELAY_BASE = 20;

    public BlockVisBattery(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(CHARGE, 0));
    }

    @Override
    protected MapCodec<BlockVisBattery> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        updateTick(state, level, pos, random);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        updateTick(state, level, pos, random);
    }

    private void updateTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int charge = state.getValue(CHARGE);
        if (level.hasNeighborSignal(pos)) {
            if (charge > 0) {
                AuraHelper.addVis(level, pos, TRANSFER_AMOUNT);
                level.setBlockAndUpdate(pos, state.setValue(CHARGE, charge - 1));
                level.scheduleTick(pos, this, POWERED_DISCHARGE_DELAY);
            }
            return;
        }
        float aura = AuraHelper.getVis(level, pos);
        int base = AuraHelper.getAuraBase(level, pos);
        if (charge < MAX_CHARGE && aura > base * CHARGE_AURA_FRACTION && aura > 1.0F) {
            AuraHelper.drainVis(level, pos, TRANSFER_AMOUNT, false);
            level.setBlockAndUpdate(pos, state.setValue(CHARGE, charge + 1));
            level.scheduleTick(pos, this, CHARGE_DELAY_BASE + random.nextInt(CHARGE_DELAY_BASE));
        } else if (charge > 0 && aura < base * DISCHARGE_AURA_FRACTION) {
            AuraHelper.addVis(level, pos, TRANSFER_AMOUNT);
            level.setBlockAndUpdate(pos, state.setValue(CHARGE, charge - 1));
            level.scheduleTick(pos, this, DISCHARGE_DELAY_BASE + random.nextInt(DISCHARGE_DELAY_BASE));
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.hasNeighborSignal(pos)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return state.getValue(CHARGE);
    }
}
