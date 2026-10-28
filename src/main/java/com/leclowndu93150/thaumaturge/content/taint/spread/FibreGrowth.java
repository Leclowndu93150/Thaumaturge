package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.leclowndu93150.thaumaturge.api.taint.ITaintOrigin;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
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
    private static final int FEATURE_CHANCE_PERCENT = 60;
    private static final float GROWTH_PRESSURE = 0.01F;
    private static final int SPROUT_ONE_IN = 100;
    private static final int WOODY_TAINT_NEIGHBOURS = 2;
    private static final int GROUND_TAINT_NEIGHBOURS = 3;
    private static final List<Conversion> CONVERSIONS = List.of(
            new Conversion(TTBlockTags.TAINT_CONVERTIBLE_LOG, WOODY_TAINT_NEIGHBOURS, FibreGrowth::taintLog),
            new Conversion(
                    TTBlockTags.TAINT_CONVERTIBLE_CRUST,
                    WOODY_TAINT_NEIGHBOURS,
                    state -> TTBlocks.TAINT_CRUST.get().defaultBlockState()),
            new Conversion(
                    TTBlockTags.TAINT_CONVERTIBLE_SOIL,
                    GROUND_TAINT_NEIGHBOURS,
                    state -> TTBlocks.TAINT_SOIL.get().defaultBlockState()),
            new Conversion(
                    TTBlockTags.TAINT_CONVERTIBLE_ROCK,
                    GROUND_TAINT_NEIGHBOURS,
                    state -> TTBlocks.TAINT_ROCK.get().defaultBlockState()));

    private FibreGrowth() {}

    public static boolean attempt(ServerLevel level, BlockPos origin, boolean force) {
        if (force) {
            return grow(level, origin, true, null);
        }
        ITaintOrigin payer = TaintOrigins.payingOrigin(level, origin, TaintOrigins.growthCost());
        return payer != null && growPaid(level, origin, payer, false);
    }

    public static boolean growFromOrigin(ServerLevel level, BlockPos at, ITaintOrigin origin) {
        return origin.getTaintReserve() >= TaintOrigins.growthCost() && growPaid(level, at, origin, true);
    }

    private static boolean growPaid(ServerLevel level, BlockPos at, ITaintOrigin payer, boolean force) {
        if (!grow(level, at, force, payer)) {
            return false;
        }
        payer.spendTaintReserve(TaintOrigins.growthCost());
        return true;
    }

    private static boolean grow(ServerLevel level, BlockPos origin, boolean force, @Nullable ITaintOrigin payer) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || !level.hasChunkAt(origin)
                || TaintBlooms.isProtected(level, origin)) {
            return false;
        }
        RandomSource random = level.getRandom();
        if (!force && random.nextDouble() * PERCENT >= ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.get()) {
            return false;
        }
        BlockPos target = pickTarget(origin, random);
        if (target.equals(origin)) {
            return false;
        }
        BlockState state = level.getBlockState(target);
        if (!isReachable(level, target, state)) {
            return false;
        }
        if (isOpenSpace(state)) {
            return growInOpenSpace(level, target);
        }
        if (state.is(BlockTags.LEAVES)) {
            return sproutFeature(level, target, random);
        }
        Conversion conversion = convertible(level, target, state);
        if (conversion != null) {
            return convert(level, target, state, conversion);
        }
        if (payer != null
                && random.nextDouble() * SPROUT_ONE_IN < 1.0
                && payer.getTaintReserve() >= TaintFeeding.reserveCap()
                && TaintSprouting.trySprout(level, target, true)) {
            payer.spendTaintReserve(
                    ThaumaturgeCommonConfig.TAINT_CHILD_SEED_COST.get().floatValue());
        }
        return false;
    }

    private static BlockPos pickTarget(BlockPos origin, RandomSource random) {
        int dx;
        int dy;
        int dz;
        do {
            dx = offset(random, HORIZONTAL_REACH);
            dy = offset(random, VERTICAL_REACH);
            dz = offset(random, HORIZONTAL_REACH);
        } while ((dx | dy | dz) == 0);
        return origin.offset(dx, dy, dz);
    }

    private static int offset(RandomSource random, int reach) {
        return random.nextInt(reach * 2 + 1) - reach;
    }

    private static boolean isReachable(ServerLevel level, BlockPos target, BlockState state) {
        if (!level.hasChunkAt(target)
                || TaintBlooms.isProtected(level, target)
                || state.is(TTBlockTags.TAINT_CONVERSION_IMMUNE)) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, target);
        return hardness >= 0.0F && hardness <= MAX_TARGET_HARDNESS;
    }

    private static boolean isOpenSpace(BlockState state) {
        if (state.isAir()) {
            return true;
        }
        return state.canBeReplaced()
                && !state.is(BlockTags.LEAVES)
                && state.getFluidState().isEmpty();
    }

    private static boolean columnReady(ServerLevel level, BlockPos pos) {
        return TaintBiomeManager.isTainted(level, pos) || TaintBiomeManager.taintColumn(level, pos);
    }

    private static boolean growInOpenSpace(ServerLevel level, BlockPos target) {
        if (!TaintHelper.hasSturdyNeighbour(level, target)
                || TaintHelper.isRootless(level, target)
                || !columnReady(level, target)) {
            return false;
        }
        level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), Block.UPDATE_ALL);
        TaintEcology.addPressure(level, target, GROWTH_PRESSURE);
        return true;
    }

    private static boolean sproutFeature(ServerLevel level, BlockPos target, RandomSource random) {
        Direction logSide = null;
        int logCount = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(target.relative(direction)).is(TTBlocks.TAINT_LOG)) {
                logCount++;
                if (random.nextInt(logCount) == 0) {
                    logSide = direction;
                }
            }
        }
        return logSide != null
                && random.nextInt((int) PERCENT) < FEATURE_CHANCE_PERCENT
                && level.setBlock(
                        target,
                        TTBlocks.TAINT_FEATURE
                                .get()
                                .defaultBlockState()
                                .setValue(DirectionalBlock.FACING, logSide.getOpposite()),
                        Block.UPDATE_ALL);
    }

    private static @Nullable Conversion convertible(ServerLevel level, BlockPos target, BlockState state) {
        if (TaintHelper.isTaintBlock(state) || state.getDestroySpeed(level, target) >= MAX_CONVERTIBLE_HARDNESS) {
            return null;
        }
        Conversion conversion = matching(state);
        if (conversion == null || TaintHelper.countAdjacentTaint(level, target) < conversion.taintNeighbours()) {
            return null;
        }
        return conversion;
    }

    private static boolean convert(ServerLevel level, BlockPos target, BlockState state, Conversion conversion) {
        if (!columnReady(level, target)) {
            return false;
        }
        level.setBlock(target, conversion.result().apply(state), Block.UPDATE_ALL);
        TaintEcology.addPressure(level, target, GROWTH_PRESSURE);
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

    private record Conversion(TagKey<Block> family, int taintNeighbours, UnaryOperator<BlockState> result) {}
}
