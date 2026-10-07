package com.leclowndu93150.thaumaturge.content.world.taint;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintacle;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.jspecify.annotations.Nullable;

public final class TaintBiomeFeature extends Feature<TaintBiomeConfig> {
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS;
    private static final int CHUNK_WIDTH = 16;
    private static final int CHUNK_CENTER_QUART = 2;
    private static final float FULL_TURN = 360.0F;

    public TaintBiomeFeature(Codec<TaintBiomeConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<TaintBiomeConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        TaintBiomeConfig config = context.config();
        ChunkPos chunk = ChunkPos.containing(context.origin());
        boolean any = false;
        int blobs = random.nextInt(config.maxCrustBlobs() + 1);
        for (int i = 0; i < blobs; i++) {
            any |= placeCrustBlob(level, random, config, chunk.getMinBlockX() + random.nextInt(CHUNK_WIDTH), chunk.getMinBlockZ() + random.nextInt(CHUNK_WIDTH));
        }
        for (int i = 0; i < config.grassFibreAttempts(); i++) {
            any |= placeFibre(level, config, chunk.getMinBlockX() + random.nextInt(CHUNK_WIDTH), chunk.getMinBlockZ() + random.nextInt(CHUNK_WIDTH), true);
        }
        for (int i = 0; i < config.generalFibreAttempts(); i++) {
            any |= placeFibre(level, config, chunk.getMinBlockX() + random.nextInt(CHUNK_WIDTH), chunk.getMinBlockZ() + random.nextInt(CHUNK_WIDTH), false);
        }
        if (config.landmarks() && !ThaumaturgeCommonConfig.WUSS_MODE.get() && isLandmarkChunk(level, chunk, config.landmarkRadiusChunks())) {
            any |= placeNode(level, random, config, chunk, true);
            any |= placeNode(level, random, config, chunk, false);
            any |= placeTaintacle(level, random, config, chunk);
        }
        return any;
    }

    private static boolean placeFibre(WorldGenLevel level, TaintBiomeConfig config, int x, int z, boolean onDirt) {
        BlockPos ground = ground(level, x, z, config.groundSearchDepth());
        if (ground == null || onDirt && !level.getBlockState(ground).is(BlockTags.SUBSTRATE_OVERWORLD)) {
            return false;
        }
        BlockPos target = ground.above();
        if (!canHostFibre(level, target)) {
            return false;
        }
        level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), PLACE_FLAGS);
        return true;
    }

    private static boolean placeCrustBlob(WorldGenLevel level, RandomSource random, TaintBiomeConfig config, int x, int z) {
        BlockPos center = ground(level, x, z, config.groundSearchDepth());
        if (center == null || !level.getBiome(center).is(TTBiomeTags.IS_TAINTED)) {
            return false;
        }
        int radius = config.crustRadius().sample(random);
        boolean any = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + random.nextInt(2)) {
                    continue;
                }
                BlockPos pos = ground(level, x + dx, z + dz, config.groundSearchDepth());
                if (pos != null && level.getBlockState(pos).getDestroySpeed(level, pos) >= 0.0F) {
                    level.setBlock(pos, config.crust().defaultBlockState(), PLACE_FLAGS);
                    any = true;
                }
            }
        }
        return any;
    }

    private static boolean placeNode(WorldGenLevel level, RandomSource random, TaintBiomeConfig config, ChunkPos chunk, boolean tainted) {
        BlockPos surface = findTaintedSurface(level, random, config, chunk);
        if (surface == null) {
            return false;
        }
        return tainted ? NodeGenerator.createGuaranteedTaintedNodeAt(level, surface, random) : NodeGenerator.createGuaranteedHungryNodeAt(level, surface, random);
    }

    private static boolean placeTaintacle(WorldGenLevel level, RandomSource random, TaintBiomeConfig config, ChunkPos chunk) {
        if (level.getLevel().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        BlockPos pos = findTaintedSurface(level, random, config, chunk);
        if (pos == null) {
            return false;
        }
        EntityTaintacle taintacle = TTEntities.TAINTACLE.get().create(level.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
        if (taintacle == null) {
            return false;
        }
        level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), PLACE_FLAGS);
        taintacle.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * FULL_TURN, 0.0F);
        taintacle.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.CHUNK_GENERATION, null);
        taintacle.setPersistenceRequired();
        level.addFreshEntityWithPassengers(taintacle);
        return true;
    }

    private static @Nullable BlockPos findTaintedSurface(WorldGenLevel level, RandomSource random, TaintBiomeConfig config, ChunkPos chunk) {
        for (int attempt = 0; attempt < config.landmarkAttempts(); attempt++) {
            BlockPos ground = ground(level, chunk.getMinBlockX() + random.nextInt(CHUNK_WIDTH), chunk.getMinBlockZ() + random.nextInt(CHUNK_WIDTH), config.groundSearchDepth());
            if (ground != null && ground.getY() + 1 < level.getMaxY() && canHostFibre(level, ground.above()) && level.getBlockState(ground).getDestroySpeed(level, ground) >= 0.0F) {
                return ground.above();
            }
        }
        return null;
    }

    private static boolean canHostFibre(WorldGenLevel level, BlockPos pos) {
        BlockState here = level.getBlockState(pos);
        return level.getBiome(pos).is(TTBiomeTags.IS_TAINTED) && (here.isAir() || here.canBeReplaced()) && here.getFluidState().isEmpty() && BlockTaintFibre.hasSolidAttachment(level, pos);
    }

    private static @Nullable BlockPos ground(WorldGenLevel level, int x, int z, int depth) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
        int floor = Math.max(level.getMinY(), cursor.getY() - depth);
        while (cursor.getY() >= floor) {
            BlockState state = level.getBlockState(cursor);
            if (!state.getFluidState().isEmpty()) {
                return null;
            }
            if (!state.isAir() && !state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES) && !state.canBeReplaced()) {
                return cursor.immutable();
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    private static boolean isLandmarkChunk(WorldGenLevel level, ChunkPos chunk, int radius) {
        BiomeSource source = level.getLevel().getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getLevel().getChunkSource().randomState().sampler();
        int quartY = QuartPos.fromBlock(level.getSeaLevel());
        if (!isNaturallyTainted(source, sampler, chunk.x(), chunk.z(), quartY)) {
            return false;
        }
        long seed = level.getSeed();
        long own = mix(seed, chunk.x(), chunk.z());
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if ((dx != 0 || dz != 0) && Long.compareUnsigned(mix(seed, chunk.x() + dx, chunk.z() + dz), own) < 0 && isNaturallyTainted(source, sampler, chunk.x() + dx, chunk.z() + dz, quartY)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isNaturallyTainted(BiomeSource source, Climate.Sampler sampler, int chunkX, int chunkZ, int quartY) {
        return source.getNoiseBiome(QuartPos.fromSection(chunkX) + CHUNK_CENTER_QUART, quartY, QuartPos.fromSection(chunkZ) + CHUNK_CENTER_QUART, sampler).is(TTBiomeTags.IS_TAINTED);
    }

    private static long mix(long seed, int x, int z) {
        long value = seed ^ x * 341873128712L ^ z * 132897987541L;
        value ^= value >>> 30;
        value *= 0xbf58476d1ce4e5b9L;
        value ^= value >>> 27;
        value *= 0x94d049bb133111ebL;
        return value ^ value >>> 31;
    }
}
