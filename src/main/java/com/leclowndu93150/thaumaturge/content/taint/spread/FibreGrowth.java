package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

public final class FibreGrowth {
    private static final double PERCENT = 100.0;
    private static final int HORIZONTAL_REACH = 1;
    private static final int VERTICAL_REACH = 2;
    private static final float MAX_TARGET_HARDNESS = 10.0F;
    private static final float MAX_CONVERTIBLE_HARDNESS = 5.0F;
    private static final float FEATURE_CHANCE = 0.6F;
    private static final float GROWTH_PRESSURE = 0.01F;
    private static final int SPROUT_ONE_IN = 100;
    private static final float SPROUT_MIN_SATURATION = 0.85F;
    private static final int WOODY_TAINT_NEIGHBOURS = 2;
    private static final int GROUND_TAINT_NEIGHBOURS = 3;
    private static final List<Conversion> CONVERSIONS = List.of(new Conversion(TTBlockTags.TAINT_CONVERTIBLE_LOG, WOODY_TAINT_NEIGHBOURS, FibreGrowth::taintLog),
            new Conversion(TTBlockTags.TAINT_CONVERTIBLE_CRUST, WOODY_TAINT_NEIGHBOURS, state -> TTBlocks.TAINT_CRUST.get().defaultBlockState()),
            new Conversion(TTBlockTags.TAINT_CONVERTIBLE_SOIL, GROUND_TAINT_NEIGHBOURS, state -> TTBlocks.TAINT_SOIL.get().defaultBlockState()),
            new Conversion(TTBlockTags.TAINT_CONVERTIBLE_ROCK, GROUND_TAINT_NEIGHBOURS, state -> TTBlocks.TAINT_ROCK.get().defaultBlockState()));

    private FibreGrowth() {}

    public static void attempt(ServerLevel level, BlockPos origin, boolean force) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || !level.hasChunkAt(origin) || TaintBlooms.isProtected(level, origin)) {
            return;
        }
        RandomSource random = level.getRandom();
        if (!force && random.nextDouble() * PERCENT >= ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.get()) {
            return;
        }
        BlockPos target = pickTarget(origin, random);
        BlockState state = level.getBlockState(target);
        if (!isReachable(level, target, state, force)) {
            return;
        }
        if (isOpenSpace(state)) {
            growInOpenSpace(level, target, force);
        } else if (state.is(BlockTags.LEAVES)) {
            sproutFeature(level, target, random);
        } else if (!tryConvert(level, target, state, force) && random.nextInt(SPROUT_ONE_IN) == 0 && TaintEcology.getSaturation(level, target) >= SPROUT_MIN_SATURATION) {
            TaintSprouting.trySprout(level, target, true);
        }
    }

    private static BlockPos pickTarget(BlockPos origin, RandomSource random) {
        int dx;
        int dy;
        int dz;
        do {
            dx = offset(random, HORIZONTAL_REACH);
            dy = offset(random, VERTICAL_REACH);
            dz = offset(random, HORIZONTAL_REACH);
        } while (dx == 0 && dy == 0 && dz == 0);
        return origin.offset(dx, dy, dz);
    }

    private static int offset(RandomSource random, int reach) {
        return random.nextInt(reach * 2 + 1) - reach;
    }

    private static boolean isReachable(ServerLevel level, BlockPos target, BlockState state, boolean force) {
        if (!level.hasChunkAt(target) || TaintBlooms.isProtected(level, target) || state.is(TTBlockTags.TAINT_CONVERSION_IMMUNE)) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, target);
        if (hardness < 0.0F || hardness > MAX_TARGET_HARDNESS) {
            return false;
        }
        return force || TaintBiomeManager.isTainted(level, target);
    }

    private static boolean isOpenSpace(BlockState state) {
        if (state.isAir()) {
            return true;
        }
        return state.canBeReplaced() && state.getFluidState().isEmpty() && !state.is(BlockTags.LEAVES) && !TaintHelper.isTaintBlock(state);
    }

    private static boolean columnReady(ServerLevel level, BlockPos pos, boolean force) {
        if (TaintBiomeManager.isTainted(level, pos)) {
            return true;
        }
        return force && TaintBiomeManager.taintColumn(level, pos);
    }

    private static void growInOpenSpace(ServerLevel level, BlockPos target, boolean force) {
        if (!TaintHelper.hasSturdyNeighbour(level, target) || TaintHelper.isRootless(level, target) || !columnReady(level, target, force)) {
            return;
        }
        level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), Block.UPDATE_ALL);
        TaintEcology.addPressure(level, target, GROWTH_PRESSURE);
    }

    private static void sproutFeature(ServerLevel level, BlockPos target, RandomSource random) {
        List<Direction> awayFromLogs = new ArrayList<>(Direction.values().length);
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(target.relative(direction)).is(TTBlocks.TAINT_LOG)) {
                awayFromLogs.add(direction.getOpposite());
            }
        }
        if (awayFromLogs.isEmpty() || random.nextFloat() >= FEATURE_CHANCE) {
            return;
        }
        Direction facing = awayFromLogs.get(random.nextInt(awayFromLogs.size()));
        level.setBlock(target, TTBlocks.TAINT_FEATURE.get().defaultBlockState().setValue(DirectionalBlock.FACING, facing), Block.UPDATE_ALL);
    }

    private static boolean tryConvert(ServerLevel level, BlockPos target, BlockState state, boolean force) {
        if (TaintHelper.isTaintBlock(state) || state.getDestroySpeed(level, target) >= MAX_CONVERTIBLE_HARDNESS) {
            return false;
        }
        Conversion conversion = matching(state);
        if (conversion == null || TaintHelper.countAdjacentTaint(level, target) < conversion.taintNeighbours()) {
            return false;
        }
        if (columnReady(level, target, force)) {
            level.setBlock(target, conversion.result().apply(state), Block.UPDATE_ALL);
            TaintEcology.addPressure(level, target, GROWTH_PRESSURE);
        }
        return true;
    }

    private static @Nullable Conversion matching(BlockState state) {
        for (Conversion conversion : CONVERSIONS) {
            if (state.is(conversion.family())) {
                return conversion;
            }
        }
        return null;
    }

    private static BlockState taintLog(BlockState state) {
        Property<Direction.Axis> axis = BlockStateProperties.AXIS;
        return TTBlocks.TAINT_LOG.get().withAxis(state.hasProperty(axis) ? state.getValue(axis) : Direction.Axis.Y);
    }

    private record Conversion(TagKey<Block> family, int taintNeighbours, UnaryOperator<BlockState> result) {
    }
}
