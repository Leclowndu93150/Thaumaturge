package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSeed;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.block.ITaintBlock;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSeedRegistry;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;

public final class TaintHelper {
    private static final float FLUX_PER_CONVERSION = 0.01F;
    private static final float SEED_FLUX_THRESHOLD = 5.0F;
    private static final float SEED_FLUX_COST = 5.0F;
    private static final double SEED_SPAWN_RATE_FACTOR = 0.33;
    private static final float FEATURE_ON_LEAVES_CHANCE = 0.6F;
    private static final double SEED_VALIDATION_RANGE = 1.0;
    private static final float MAX_SPREAD_HARDNESS = 10.0F;
    private static final float MAX_CONVERT_HARDNESS = 5.0F;

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
        for (BlockPos seed : registry.all()) {
            if (seed.distSqr(pos) <= area) {
                AABB box = new AABB(seed).inflate(SEED_VALIDATION_RANGE);
                if (serverLevel.getEntitiesOfClass(AbstractTaintSeed.class, box).isEmpty()) {
                    registry.removeSeed(seed);
                    return false;
                }
                return true;
            }
        }
        return false;
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
        if (!force && ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        RandomSource random = level.getRandom();
        float saturation = AuraHelper.getFluxSaturation(level, pos);
        float mod = 0.001F + saturation * 2.0F;
        double rate = ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.get() / 100.0;
        if (!force && random.nextFloat() > rate * mod) {
            return;
        }
        if (!isNearTaintSeed(level, pos)) {
            return;
        }
        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
        if (target.equals(pos)) {
            return;
        }
        BlockState targetState = level.getBlockState(target);
        float hardness = targetState.getDestroySpeed(level, target);
        if (hardness < 0 || hardness > MAX_SPREAD_HARDNESS) {
            return;
        }

        boolean isLeaves = targetState.is(BlockTags.LEAVES);
        boolean isReplaceable = targetState.isAir() || targetState.canBeReplaced();

        if (!isLeaves && !targetState.liquid() && isReplaceable) {
            if (isAdjacentToSolidBlock(level, target) && !BlockTaintFibre.isOnlyAdjacentToTaint(level, target)) {
                level.setBlock(target, TCBlocks.TAINT_FIBRE.get().defaultBlockState(), Block.UPDATE_ALL);
                AuraHelper.drainFlux(level, target, FLUX_PER_CONVERSION, false);
            }
            return;
        }

        if (isLeaves) {
            Direction logFace = random.nextFloat() < FEATURE_ON_LEAVES_CHANCE ? findAdjacentTaintLog(level, target) : null;
            if (logFace != null) {
                level.setBlock(target, TCBlocks.TAINT_FEATURE.get().defaultBlockState().setValue(DirectionalBlock.FACING, logFace.getOpposite()), Block.UPDATE_ALL);
            } else {
                level.setBlock(target, TCBlocks.TAINT_FIBRE.get().defaultBlockState(), Block.UPDATE_ALL);
                AuraHelper.drainFlux(level, target, FLUX_PER_CONVERSION, false);
            }
            return;
        }

        if (BlockTaintFibre.isHemmedByTaint(level, target) && hardness < MAX_CONVERT_HARDNESS) {
            if (targetState.is(BlockTags.LOGS) && !(targetState.getBlock() instanceof ITaintBlock)) {
                Direction.Axis axis = Direction.Axis.Y;
                if (targetState.hasProperty(RotatedPillarBlock.AXIS)) {
                    axis = targetState.getValue(RotatedPillarBlock.AXIS);
                }
                level.setBlock(target, TCBlocks.TAINT_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis), Block.UPDATE_ALL);
                return;
            }
            if (isCrustConvertible(targetState)) {
                level.setBlock(target, TCBlocks.TAINT_CRUST.get().defaultBlockState(), Block.UPDATE_ALL);
                AuraHelper.drainFlux(level, target, FLUX_PER_CONVERSION, false);
                return;
            }
            if (isSoilConvertible(targetState)) {
                level.setBlock(target, TCBlocks.TAINT_SOIL.get().defaultBlockState(), Block.UPDATE_ALL);
                AuraHelper.drainFlux(level, target, FLUX_PER_CONVERSION, false);
                return;
            }
            if (isRockConvertible(targetState)) {
                level.setBlock(target, TCBlocks.TAINT_ROCK.get().defaultBlockState(), Block.UPDATE_ALL);
                AuraHelper.drainFlux(level, target, FLUX_PER_CONVERSION, false);
                return;
            }
        }

        trySpawnTaintSeed(level, target, targetState, random, rate);
    }

    private static void trySpawnTaintSeed(ServerLevel level, BlockPos target, BlockState targetState, RandomSource random, double rate) {
        if (!targetState.is(TCBlocks.TAINT_SOIL.get()) && !targetState.is(TCBlocks.TAINT_ROCK.get())) {
            return;
        }
        if (!level.getBlockState(target.above()).isAir()) {
            return;
        }
        if (AuraHelper.getFlux(level, target) < SEED_FLUX_THRESHOLD) {
            return;
        }
        if (random.nextFloat() >= rate * SEED_SPAWN_RATE_FACTOR) {
            return;
        }
        if (!isAtTaintSeedEdge(level, target)) {
            return;
        }
        EntityTaintSeed seed = TCEntities.TAINT_SEED.get().create(level, EntitySpawnReason.NATURAL);
        if (seed == null) {
            return;
        }
        seed.snapTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5, random.nextInt(360), 0.0F);
        if (!canSeedSpawnAt(level, seed)) {
            seed.discard();
            return;
        }
        AuraHelper.drainFlux(level, target, SEED_FLUX_COST, false);
        level.addFreshEntity(seed);
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
        return level.getEntitiesOfClass(AbstractTaintSeed.class, box, other -> other != seed).isEmpty();
    }

    private static boolean isCrustConvertible(BlockState state) {
        if (state.is(Blocks.RED_MUSHROOM_BLOCK) || state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.MUSHROOM_STEM)) {
            return true;
        }
        if (state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN) || state.is(Blocks.MELON)) {
            return true;
        }
        if (state.is(Blocks.CACTUS) || state.is(Blocks.SPONGE) || state.is(Blocks.WET_SPONGE)) {
            return true;
        }
        return state.is(BlockTags.CORAL_BLOCKS) || state.is(BlockTags.PLANKS);
    }

    private static boolean isSoilConvertible(BlockState state) {
        return state.is(BlockTags.SAND) || state.is(BlockTags.DIRT) || state.is(Blocks.CLAY);
    }

    private static boolean isRockConvertible(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.STONE_BRICKS) || state.is(Tags.Blocks.STONES)
                || state.is(Tags.Blocks.COBBLESTONES) || state.is(Tags.Blocks.ORES);
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
