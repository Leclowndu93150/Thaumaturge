package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSeed;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSeedRegistry;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class TaintHelper {
    private static final float SEED_FLUX_THRESHOLD = 5.0F;
    private static final float SEED_FLUX_COST = 5.0F;
    private static final double SEED_SPAWN_CHANCE = 0.01;
    private static final float SEED_SPAWN_MIN_PRESSURE = 0.85F;
    private static final float SEED_SPAWN_PRESSURE = 0.08F;
    private static final int SATELLITE_ATTEMPTS = 8;
    private static final int SATELLITE_RANGE_XZ = 9;
    private static final int SATELLITE_RANGE_Y = 3;
    private static final float FEATURE_ON_LEAVES_CHANCE = 0.6F;
    private static final double SEED_VALIDATION_RANGE = 1.0;
    private static final double SEED_EDGE_RATIO = 0.8;
    private static final float MAX_SPREAD_HARDNESS = 10.0F;
    private static final float MAX_CONVERT_HARDNESS = 5.0F;
    private static final double PERCENT = 100.0;
    private static final int SPREAD_HORIZONTAL = 3;
    private static final int SPREAD_VERTICAL = 5;
    private static final int LIGHT_CONVERSION_NEIGHBOURS = 2;
    private static final int HEAVY_CONVERSION_NEIGHBOURS = 3;
    private static final float CONVERSION_PRESSURE = 0.01F;
    private static final float FRONTIER_RATE_SCALE = 5.0F;
    private static final float FRONTIER_MAX_FLUX = 2.0F;
    private static final float FRONTIER_FLUX_ACCELERATION = 0.5F;
    private static final float FRONTIER_PRESSURE = 0.01F;
    private static final float FRONTIER_FLUX_PRESSURE = 0.01F;
    private static final float FRONTIER_MAX_FLUX_PRESSURE = 0.02F;

    private TaintHelper() {}

    public static double spreadArea() {
        return ThaumaturgeCommonConfig.TAINT_SPREAD_AREA.get();
    }

    public static void addTaintSeed(ServerLevel level, BlockPos pos) {
        TaintSeedRegistry.get(level).addSeed(pos);
    }

    public static void removeTaintSeed(ServerLevel level, BlockPos pos) {
        TaintSeedRegistry.get(level).removeSeed(pos);
    }

    public static boolean isNearTaintSeed(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        double area = spreadArea() * spreadArea();
        TaintSeedRegistry registry = TaintSeedRegistry.get(serverLevel);
        List<BlockPos> stale = null;
        boolean found = false;
        for (BlockPos seed : registry.all()) {
            if (seed.distSqr(pos) > area) {
                continue;
            }
            if (serverLevel
                    .getEntitiesOfClass(AbstractTaintSeed.class, new AABB(seed).inflate(SEED_VALIDATION_RANGE))
                    .isEmpty()) {
                if (stale == null) {
                    stale = new ArrayList<>();
                }
                stale.add(seed);
                continue;
            }
            found = true;
            break;
        }
        if (stale != null) {
            for (BlockPos seed : stale) {
                registry.removeSeed(seed);
            }
        }
        return found;
    }

    public static boolean isEcologicallySustained(ServerLevel level, BlockPos pos) {
        return TaintBiomeManager.isTainted(level, pos) || isNearTaintSeed(level, pos);
    }

    public static boolean isAtTaintSeedEdge(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        double area = spreadArea() * spreadArea();
        double fringe = spreadArea() * SEED_EDGE_RATIO * (spreadArea() * SEED_EDGE_RATIO);
        for (BlockPos seed : TaintSeedRegistry.get(serverLevel).all()) {
            double d = seed.distSqr(pos);
            if (d < area && d > fringe) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdjacentToSolidBlock(LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.isFaceSturdy(level, neighbor, direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    public static int countAdjacentTaint(LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).getBlock() instanceof ITaintBlock) {
                count++;
            }
        }
        return count;
    }

    public static boolean placeFibreFromFlux(ServerLevel level, BlockPos pos) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || !ThaumaturgeCommonConfig.TAINT_FROM_FLUX.get()
                || TaintBlooms.isProtected(level, pos)) {
            return false;
        }
        return level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
    }

    public static void spreadFibres(ServerLevel level, BlockPos pos, boolean force) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || !level.hasChunkAt(pos) || TaintBlooms.isProtected(level, pos)) {
            return;
        }
        RandomSource random = level.getRandom();
        if (!force && random.nextDouble() * PERCENT >= ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.get()) {
            return;
        }
        BlockPos target = pos.offset(
                random.nextInt(SPREAD_HORIZONTAL) - 1,
                random.nextInt(SPREAD_VERTICAL) - SPREAD_VERTICAL / 2,
                random.nextInt(SPREAD_HORIZONTAL) - 1);
        if (target.equals(pos) || !level.hasChunkAt(target) || TaintBlooms.isProtected(level, target)) {
            return;
        }
        boolean targetTainted = TaintBiomeManager.isTainted(level, target);
        if (!targetTainted && !force) {
            return;
        }
        BlockState targetState = level.getBlockState(target);
        float hardness = targetState.getDestroySpeed(level, target);
        if (hardness < 0 || hardness > MAX_SPREAD_HARDNESS || targetState.is(TTBlockTags.TAINT_CONVERSION_IMMUNE)) {
            return;
        }

        boolean isLeaves = targetState.is(BlockTags.LEAVES);
        boolean isReplaceable = targetState.isAir() || targetState.canBeReplaced();
        if (!isLeaves && !targetState.liquid() && isReplaceable) {
            if (isAdjacentToSolidBlock(level, target)
                    && !BlockTaintFibre.isOnlyAdjacentToTaint(level, target)
                    && ensureTargetBiome(level, target, force, targetTainted)) {
                level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), Block.UPDATE_ALL);
                TaintEcology.addPressure(level, target, CONVERSION_PRESSURE);
            }
            return;
        }

        if (isLeaves) {
            Direction logFace = findAdjacentTaintLog(level, target);
            if (logFace != null && random.nextFloat() < FEATURE_ON_LEAVES_CHANCE) {
                level.setBlock(
                        target,
                        TTBlocks.TAINT_FEATURE
                                .get()
                                .defaultBlockState()
                                .setValue(DirectionalBlock.FACING, logFace.getOpposite()),
                        Block.UPDATE_ALL);
            }
            return;
        }

        if (hardness < MAX_CONVERT_HARDNESS) {
            BlockState converted = convertedState(targetState, countAdjacentTaint(level, target));
            if (converted != null) {
                if (ensureTargetBiome(level, target, force, targetTainted)) {
                    level.setBlock(target, converted, Block.UPDATE_ALL);
                    TaintEcology.addPressure(level, target, CONVERSION_PRESSURE);
                }
                return;
            }
        }

        trySpawnTaintSeed(level, target, targetState, random);
    }

    public static boolean canHostFoothold(BlockState state) {
        return PhysicalFlux.isPhysicalFlux(state)
                || state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    public static void establishFoothold(ServerLevel level, BlockPos pos, float pressure, int spreadAttempts) {
        if (canHostFoothold(level.getBlockState(pos))) {
            level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
        }
        TaintEcology.addPressure(level, pos, pressure);
        for (int attempt = 0; attempt < spreadAttempts; attempt++) {
            spreadFibres(level, pos, true);
        }
    }

    public static boolean trySpreadTaintedBiome(ServerLevel level, BlockPos pos, RandomSource random) {
        int rate = ThaumaturgeCommonConfig.TAINT_FRONTIER_RATE.get();
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || rate <= 0
                || !TaintBiomeManager.isTainted(level, pos)
                || countAdjacentTaint(level, pos) < LIGHT_CONVERSION_NEIGHBOURS
                || TaintBlooms.isProtected(level, pos)) {
            return false;
        }
        float saturation = Mth.clamp(AuraHelper.getFluxSaturation(level, pos), 0.0F, FRONTIER_MAX_FLUX);
        float acceleration = 1.0F + Math.min(FRONTIER_FLUX_ACCELERATION, saturation * FRONTIER_FLUX_ACCELERATION);
        if (random.nextInt(Math.max(1, Math.round(rate * FRONTIER_RATE_SCALE / acceleration))) != 0) {
            return false;
        }
        BlockPos target = pos.offset(random.nextInt(SPREAD_HORIZONTAL) - 1, 0, random.nextInt(SPREAD_HORIZONTAL) - 1);
        if (!level.hasChunkAt(target) || !TaintBiomeManager.taintColumn(level, target)) {
            return false;
        }
        TaintEcology.addPressure(
                level,
                target,
                FRONTIER_PRESSURE + Math.min(FRONTIER_MAX_FLUX_PRESSURE, saturation * FRONTIER_FLUX_PRESSURE));
        return true;
    }

    private static boolean ensureTargetBiome(
            ServerLevel level, BlockPos target, boolean force, boolean alreadyTainted) {
        return alreadyTainted
                || TaintBiomeManager.isTainted(level, target)
                || force && TaintBiomeManager.taintColumn(level, target);
    }

    private static @Nullable BlockState convertedState(BlockState state, int adjacentTaint) {
        if (adjacentTaint >= LIGHT_CONVERSION_NEIGHBOURS
                && state.is(TTBlockTags.TAINT_CONVERTIBLE_LOG)
                && !(state.getBlock() instanceof ITaintBlock)) {
            Direction.Axis axis = state.hasProperty(RotatedPillarBlock.AXIS)
                    ? state.getValue(RotatedPillarBlock.AXIS)
                    : Direction.Axis.Y;
            return TTBlocks.TAINT_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }
        if (adjacentTaint >= LIGHT_CONVERSION_NEIGHBOURS && state.is(TTBlockTags.TAINT_CONVERTIBLE_CRUST)) {
            return TTBlocks.TAINT_CRUST.get().defaultBlockState();
        }
        if (adjacentTaint >= HEAVY_CONVERSION_NEIGHBOURS && state.is(TTBlockTags.TAINT_CONVERTIBLE_SOIL)) {
            return TTBlocks.TAINT_SOIL.get().defaultBlockState();
        }
        if (adjacentTaint >= HEAVY_CONVERSION_NEIGHBOURS && state.is(TTBlockTags.TAINT_CONVERTIBLE_ROCK)) {
            return TTBlocks.TAINT_ROCK.get().defaultBlockState();
        }
        return null;
    }

    private static void trySpawnTaintSeed(
            ServerLevel level, BlockPos target, BlockState targetState, RandomSource random) {
        if (random.nextDouble() < SEED_SPAWN_CHANCE
                && TaintEcology.getSaturation(level, target) >= SEED_SPAWN_MIN_PRESSURE) {
            tryCreateTaintSeed(level, target, targetState, random, true);
        }
    }

    public static boolean trySpawnSatelliteSeed(ServerLevel level, BlockPos origin, RandomSource random) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || level.getDifficulty() == Difficulty.PEACEFUL
                || TaintBlooms.isProtected(level, origin)
                || TaintEcology.getSaturation(level, origin) < SEED_SPAWN_MIN_PRESSURE) {
            return false;
        }
        for (int attempt = 0; attempt < SATELLITE_ATTEMPTS; attempt++) {
            BlockPos target = origin.offset(
                    random.nextInt(SATELLITE_RANGE_XZ) - SATELLITE_RANGE_XZ / 2,
                    random.nextInt(SATELLITE_RANGE_Y) - SATELLITE_RANGE_Y / 2,
                    random.nextInt(SATELLITE_RANGE_XZ) - SATELLITE_RANGE_XZ / 2);
            if (level.hasChunkAt(target)
                    && tryCreateTaintSeed(level, target, level.getBlockState(target), random, false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryCreateTaintSeed(
            ServerLevel level, BlockPos target, BlockState targetState, RandomSource random, boolean requireSeedEdge) {
        if (!targetState.is(TTBlocks.TAINT_SOIL.get()) && !targetState.is(TTBlocks.TAINT_ROCK.get())) {
            return false;
        }
        if (!level.getBlockState(target.above()).isAir()
                || AuraHelper.getFlux(level, target) < SEED_FLUX_THRESHOLD
                || requireSeedEdge && !isAtTaintSeedEdge(level, target)) {
            return false;
        }
        EntityTaintSeed seed = TTEntities.TAINT_SEED.get().create(level);
        if (seed == null) {
            return false;
        }
        seed.moveTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, random.nextInt(360), 0.0F);
        if (!canSeedSpawnAt(level, seed)) {
            seed.discard();
            return false;
        }
        AuraHelper.drainFlux(level, target, SEED_FLUX_COST, false);
        level.addFreshEntity(seed);
        TaintEcology.addPressure(level, target, SEED_SPAWN_PRESSURE);
        return true;
    }

    private static boolean canSeedSpawnAt(ServerLevel level, EntityTaintSeed seed) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (!level.noCollision(seed)) {
            return false;
        }
        double fringe = spreadArea() * SEED_EDGE_RATIO;
        AABB box = seed.getBoundingBox().inflate(fringe);
        return level.getEntitiesOfClass(AbstractTaintSeed.class, box, other -> other != seed)
                .isEmpty();
    }

    private static @Nullable Direction findAdjacentTaintLog(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(TTBlocks.TAINT_LOG.get())) {
                return direction;
            }
        }
        return null;
    }
}
