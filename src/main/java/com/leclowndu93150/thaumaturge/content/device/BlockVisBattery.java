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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public final class BlockVisBattery extends Block {
    private static final int MAX_CHARGE = 10;
    private static final float VIS_STEP = 1.0F;
    private static final float CHARGE_BAND = 0.9F;
    private static final float RELEASE_BAND = 0.75F;
    private static final float MIN_CHARGE_VIS = 1.0F;
    private static final int POWERED_DELAY = 5;
    private static final int CHARGE_DELAY_MIN = 100;
    private static final int CHARGE_DELAY_SPREAD = 100;
    private static final int RELEASE_DELAY_MIN = 20;
    private static final int RELEASE_DELAY_SPREAD = 20;
    private static final int WAKE_DELAY = 1;

    public static final MapCodec<BlockVisBattery> CODEC = simpleCodec(BlockVisBattery::new);
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, MAX_CHARGE);

    public BlockVisBattery(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
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
        evaluate(state, level, pos, random);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        evaluate(state, level, pos, random);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return state.getValue(CHARGE);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (level instanceof ServerLevel server && server.hasNeighborSignal(pos) && state.getValue(CHARGE) > 0) {
            server.getBlockTicks().clearArea(new BoundingBox(pos));
            server.scheduleTick(pos, this, WAKE_DELAY);
        }
    }

    private void evaluate(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int charge = state.getValue(CHARGE);
        Transition transition = level.hasNeighborSignal(pos) ? whilePowered(charge) : whileIdle(level, pos, charge, random);
        if (transition != null) {
            settle(state, level, pos, transition);
        }
    }

    private static @Nullable Transition whilePowered(int charge) {
        return charge > 0 ? new Transition(charge - 1, true, POWERED_DELAY) : null;
    }

    private static @Nullable Transition whileIdle(ServerLevel level, BlockPos pos, int charge, RandomSource random) {
        float vis = AuraHelper.getVis(level, pos);
        int base = AuraHelper.getAuraBase(level, pos);
        if (charge < MAX_CHARGE && vis > base * CHARGE_BAND && vis > MIN_CHARGE_VIS) {
            float drained = AuraHelper.drainVis(level, pos, VIS_STEP, false);
            if (drained < VIS_STEP) {
                AuraHelper.addVis(level, pos, drained);
                return null;
            }
            return new Transition(charge + 1, false, jitter(random, CHARGE_DELAY_MIN, CHARGE_DELAY_SPREAD));
        }
        if (charge > 0 && vis < base * RELEASE_BAND) {
            return new Transition(charge - 1, true, jitter(random, RELEASE_DELAY_MIN, RELEASE_DELAY_SPREAD));
        }
        return null;
    }

    private void settle(BlockState state, ServerLevel level, BlockPos pos, Transition transition) {
        if (transition.emitsVis()) {
            AuraHelper.addVis(level, pos, VIS_STEP);
        }
        level.setBlock(pos, state.setValue(CHARGE, transition.charge()), Block.UPDATE_ALL);
        level.scheduleTick(pos, this, transition.delay());
    }

    private static int jitter(RandomSource random, int min, int spread) {
        return min + random.nextInt(spread);
    }

    private record Transition(int charge, boolean emitsVis, int delay) {
    }
}
