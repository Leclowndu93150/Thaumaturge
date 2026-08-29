package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSeed;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.block.ITaintBlock;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBloomRegistry;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSeedRegistry;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class TaintHelper {
    private static final float SEED_FLUX_THRESHOLD = 5.0F;
    private static final float SEED_FLUX_COST = 5.0F;
    private static final double SEED_SPAWN_RATE_FACTOR = 0.01;
    private static final float FEATURE_ON_LEAVES_CHANCE = 0.6F;
    private static final double SEED_VALIDATION_RANGE = 1.0;
    private static final float MAX_SPREAD_HARDNESS = 10.0F;
    private static final float MAX_CONVERT_HARDNESS = 5.0F;
    private static final float ECOLOGY_PRESSURE_PER_CONVERSION = 0.01F;

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
        List<BlockPos> staleSeeds = null;
        boolean foundLiveSeed = false;
        for (BlockPos seed : registry.all()) {
            if (seed.distSqr(pos) > area) {
                continue;
            }
            AABB box = new AABB(seed).inflate(SEED_VALIDATION_RANGE);
            if (serverLevel.getEntitiesOfClass(AbstractTaintSeed.class, box).isEmpty()) {
                if (staleSeeds == null) {
                    staleSeeds = new ArrayList<>();
                }
                staleSeeds.add(seed);
                continue;
            }
            foundLiveSeed = true;
            break;
        }
        if (staleSeeds != null) {
            registry.removeSeeds(staleSeeds);
        }
        return foundLiveSeed;
    }

    public static boolean isEcologicallySustained(ServerLevel level, BlockPos pos) {
        // TC4 taint survives because the land itself is tainted, not because a Seed or aura Flux
        // continuously grants permission. Seeds remain a later-era bootstrap/accelerant and are
        // accepted as a temporary source while they establish Tainted Lands around themselves.
        return TaintBiomeManager.isTainted(level, pos) || isNearTaintSeed(level, pos);
    }

    public static boolean isAtTaintSeedEdge(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        double area = spreadArea() * spreadArea();
        double fringe = spreadArea() * 0.8 * (spreadArea() * 0.8);
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

    public static void spreadFibres(ServerLevel level, BlockPos pos, boolean force) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        if (!level.hasChunkAt(pos) || TaintBloomRegistry.isProtected(level, pos)) {
            return;
        }

        RandomSource random = level.getRandom();
        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
        if (target.equals(pos) || !level.hasChunkAt(target) || TaintBloomRegistry.isProtected(level, target)) {
            return;
        }

        // TC4 only allowed ordinary fibre/terrain colonisation inside Tainted Lands. Later-era
        // explicit outbreak sources (Seed/debug/API force calls) may establish the target biome,
        // but only once they have found a viable placement/conversion target.
        boolean targetBiomeTainted = TaintBiomeManager.isTainted(level, target);
        if (!targetBiomeTainted && !force) {
            return;
        }

        BlockState targetState = level.getBlockState(target);
        float hardness = targetState.getDestroySpeed(level, target);
        if (hardness < 0 || hardness > MAX_SPREAD_HARDNESS || targetState.is(TCBlockTags.TAINT_CONVERSION_IMMUNE)) {
            return;
        }

        boolean isLeaves = targetState.is(BlockTags.LEAVES);
        boolean isReplaceable = targetState.isAir() || targetState.canBeReplaced();

        if (!isLeaves && !targetState.liquid() && isReplaceable) {
            if (isAdjacentToSolidBlock(level, target) && !BlockTaintFibre.isOnlyAdjacentToTaint(level, target)) {
                if (!ensureTargetBiome(level, target, force, targetBiomeTainted)) {
                    return;
                }
                level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), Block.UPDATE_ALL);
                addConversionPressure(level, target);
            }
            return;
        }

        if (isLeaves) {
            Direction logFace =
                    random.nextFloat() < FEATURE_ON_LEAVES_CHANCE ? findAdjacentTaintLog(level, target) : null;
            if (logFace != null) {
                if (!ensureTargetBiome(level, target, force, targetBiomeTainted)) {
                    return;
                }
                level.setBlock(
                        target,
                        TCBlocks.TAINT_FEATURE
                                .get()
                                .defaultBlockState()
                                .setValue(DirectionalBlock.FACING, logFace.getOpposite()),
                        Block.UPDATE_ALL);
                addConversionPressure(level, target);
            } else {
                if (!ensureTargetBiome(level, target, force, targetBiomeTainted)) {
                    return;
                }
                level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), Block.UPDATE_ALL);
                addConversionPressure(level, target);
            }
            return;
        }

        if (hardness < MAX_CONVERT_HARDNESS) {
            // TC4 used two distinct pressure thresholds at the infection front: wood/gourd/cactus-
            // like material converted with two adjacent taint blocks, while ordinary ground/rock
            // needed three. Preserve that brake instead of letting one isolated fibre digest solid
            // terrain. Modern tags keep the rule extensible to modded blocks.
            int adjacentTaint = countAdjacentTaint(level, target);
            if (adjacentTaint >= 2
                    && targetState.is(TCBlockTags.TAINT_CONVERTIBLE_LOG)
                    && !(targetState.getBlock() instanceof ITaintBlock)) {
                if (!ensureTargetBiome(level, target, force, targetBiomeTainted)) {
                    return;
                }
                Direction.Axis axis = Direction.Axis.Y;
                if (targetState.hasProperty(RotatedPillarBlock.AXIS)) {
                    axis = targetState.getValue(RotatedPillarBlock.AXIS);
                }
                level.setBlock(
                        target,
                        TCBlocks.TAINT_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis),
                        Block.UPDATE_ALL);
                addConversionPressure(level, target);
                return;
            }
            if (adjacentTaint >= 2 && isCrustConvertible(targetState)) {
                if (!ensureTargetBiome(level, target, force, targetBiomeTainted)) {
                    return;
                }
                level.setBlock(target, TCBlocks.TAINT_CRUST.get().defaultBlockState(), Block.UPDATE_ALL);
                addConversionPressure(level, target);
                return;
            }
            if (adjacentTaint >= 3 && isSoilConvertible(targetState)) {
                if (!ensureTargetBiome(level, target, force, targetBiomeTainted)) {
                    return;
                }
                level.setBlock(target, TCBlocks.TAINT_SOIL.get().defaultBlockState(), Block.UPDATE_ALL);
                addConversionPressure(level, target);
                return;
            }
            if (adjacentTaint >= 3 && isRockConvertible(targetState)) {
                if (!ensureTargetBiome(level, target, force, targetBiomeTainted)) {
                    return;
                }
                level.setBlock(target, TCBlocks.TAINT_ROCK.get().defaultBlockState(), Block.UPDATE_ALL);
                addConversionPressure(level, target);
                return;
            }
        }

        // Keep TC6 Seed reproduction as an escalation mechanic, but only for an outbreak that
        // already has a Seed edge. It is deliberately not the foundation of ordinary TC4 spread.
        trySpawnTaintSeed(level, target, targetState, random);
    }

    private static boolean ensureTargetBiome(
            ServerLevel level, BlockPos target, boolean force, boolean alreadyTainted) {
        if (alreadyTainted || TaintBiomeManager.isTainted(level, target)) {
            return true;
        }
        return force && TaintBiomeManager.taintColumn(level, target);
    }

    /**
     * TC4 biome-frontier rule. The biome itself is passive: an active taint block at the edge must
     * win a rare roll and have at least two adjacent taint blocks before one neighbouring quart
     * column becomes Tainted Lands. Modern aura Flux accelerates this roll but is never required.
     */
    public static boolean trySpreadTaintedBiome(ServerLevel level, BlockPos pos, RandomSource random) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || !level.hasChunkAt(pos)
                || !TaintBiomeManager.isTainted(level, pos)
                || TaintBloomRegistry.isProtected(level, pos)
                || countAdjacentTaint(level, pos) < 2) {
            return false;
        }
        int spreadRate = ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.get();
        if (spreadRate <= 0) {
            return false;
        }
        float saturation = Math.max(0.0F, Math.min(2.0F, AuraHelper.getFluxSaturation(level, pos)));
        float acceleration = 1.0F + saturation * 1.5F;
        int denominator = Math.max(1, Math.round(spreadRate * 5.0F / acceleration));
        if (random.nextInt(denominator) != 0) {
            return false;
        }

        BlockPos target = pos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
        if (!level.hasChunkAt(target)
                || TaintBiomeManager.isTainted(level, target)
                || TaintBloomRegistry.isProtected(level, target)) {
            return false;
        }
        if (TaintBiomeManager.taintColumn(level, target)) {
            TaintEcology.addPressure(level, target, 0.01F + Math.min(0.02F, saturation * 0.01F));
            return true;
        }
        return false;
    }

    public static int countAdjacentTaint(LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(direction));
            if (state.getBlock() instanceof ITaintBlock) {
                count++;
            }
        }
        return count;
    }

    /**
     * Bounded reproduction hook for mature TC4-style spore colonies. Satellite outbreaks reuse
     * the same Seed collision, spacing, Flux threshold, and Flux cost rules as ordinary spread.
     */
    public static boolean trySpawnSatelliteSeed(ServerLevel level, BlockPos origin, RandomSource random) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || level.getDifficulty() == Difficulty.PEACEFUL
                || TaintBloomRegistry.isProtected(level, origin)
                || TaintEcology.getSaturation(level, origin) < 0.85F) {
            return false;
        }
        for (int i = 0; i < 8; i++) {
            BlockPos target = origin.offset(random.nextInt(9) - 4, random.nextInt(3) - 1, random.nextInt(9) - 4);
            if (!level.hasChunkAt(target)) {
                continue;
            }
            if (tryCreateTaintSeed(level, target, level.getBlockState(target), random, false)) {
                return true;
            }
        }
        return false;
    }

    private static void addConversionPressure(ServerLevel level, BlockPos pos) {
        // The pressure layer is retained for modern atmosphere/fauna escalation, but it no longer
        // grants biome takeover. TC4-style active taint blocks advance the frontier themselves.
        TaintEcology.addPressure(level, pos, ECOLOGY_PRESSURE_PER_CONVERSION);
    }

    private static void trySpawnTaintSeed(
            ServerLevel level, BlockPos target, BlockState targetState, RandomSource random) {
        if (random.nextDouble() >= SEED_SPAWN_RATE_FACTOR || TaintEcology.getSaturation(level, target) < 0.85F) {
            return;
        }
        tryCreateTaintSeed(level, target, targetState, random, true);
    }

    private static boolean tryCreateTaintSeed(
            ServerLevel level, BlockPos target, BlockState targetState, RandomSource random, boolean requireSeedEdge) {
        if (!targetState.is(TCBlocks.TAINT_SOIL.get()) && !targetState.is(TCBlocks.TAINT_ROCK.get())) {
            return false;
        }
        if (!level.getBlockState(target.above()).isAir()
                || AuraHelper.getFlux(level, target) < SEED_FLUX_THRESHOLD
                || (requireSeedEdge && !isAtTaintSeedEdge(level, target))) {
            return false;
        }
        EntityTaintSeed seed = TCEntities.TAINT_SEED.get().create(level);
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
        TaintEcology.addPressure(level, target, 0.08F);
        return true;
    }

    private static boolean canSeedSpawnAt(ServerLevel level, EntityTaintSeed seed) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (!level.noCollision(seed)) {
            return false;
        }
        double fringe = spreadArea() * 0.8;
        AABB box = seed.getBoundingBox().inflate(fringe);
        return level.getEntitiesOfClass(AbstractTaintSeed.class, box, other -> other != seed)
                .isEmpty();
    }

    private static boolean isCrustConvertible(BlockState state) {
        return state.is(TCBlockTags.TAINT_CONVERTIBLE_CRUST);
    }

    private static boolean isSoilConvertible(BlockState state) {
        return state.is(TCBlockTags.TAINT_CONVERTIBLE_SOIL);
    }

    private static boolean isRockConvertible(BlockState state) {
        return state.is(TCBlockTags.TAINT_CONVERTIBLE_ROCK);
    }

    private static Direction findAdjacentTaintLog(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState neighborState = level.getBlockState(pos.relative(direction));
            if (neighborState.is(TCBlocks.TAINT_LOG.get())) {
                return direction;
            }
        }
        return null;
    }
}
