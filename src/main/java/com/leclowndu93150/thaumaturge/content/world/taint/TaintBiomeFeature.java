package com.leclowndu93150.thaumaturge.content.world.taint;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintacle;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** TC4-inspired initial infection placed inside naturally generated Tainted Lands. */
public final class TaintBiomeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int GRASS_FIBRE_ATTEMPTS = 10;
    private static final int GENERAL_FIBRE_ATTEMPTS = 8;
    private static final int MAX_BLOBS = 2;
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS;
    // One deterministic local minimum per roughly 9x9-chunk tainted cluster. Small/medium natural
    // patches therefore get one landmark pair; very large patches can get another only after they
    // extend beyond this neighborhood, rather than receiving one pair in every chunk.
    private static final int PATCH_ANCHOR_RADIUS_CHUNKS = 4;
    private static final int ANCHOR_POSITION_ATTEMPTS = 48;

    public TaintBiomeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        ChunkPos chunk = new ChunkPos(origin);
        int chunkMinX = chunk.getMinBlockX();
        int chunkMinZ = chunk.getMinBlockZ();
        boolean any = false;

        int blobs = random.nextInt(MAX_BLOBS + 1);
        for (int i = 0; i < blobs; i++) {
            int x = chunkMinX + random.nextInt(16);
            int z = chunkMinZ + random.nextInt(16);
            any |= placeCrustBlob(level, random, x, z);
        }

        // TC4 first made ten surface attempts biased toward grass/replaceable growth.
        for (int i = 0; i < GRASS_FIBRE_ATTEMPTS; i++) {
            int x = chunkMinX + random.nextInt(16);
            int z = chunkMinZ + random.nextInt(16);
            BlockPos target = firstAirAboveSurface(level, x, z);
            if (!level.getBiome(target).is(TCBiomes.TAINTED_LANDS)) {
                continue;
            }
            BlockState below = level.getBlockState(target.below());
            BlockState here = level.getBlockState(target);
            if (!below.is(net.minecraft.tags.BlockTags.DIRT)
                    || (!here.isAir() && !here.canBeReplaced())
                    || !BlockTaintFibre.hasSolidAttachment(level, target)) {
                continue;
            }
            level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), PLACE_FLAGS);
            any = true;
        }

        // TC4 then made eight general uncovered-surface attempts. The old generator also smeared
        // the biome boundary here; omit that one worldgen-time smear in modern quart-biome storage
        // so rare natural patches do not inflate before the active gameplay frontier starts.
        for (int i = 0; i < GENERAL_FIBRE_ATTEMPTS; i++) {
            int x = chunkMinX + random.nextInt(16);
            int z = chunkMinZ + random.nextInt(16);
            BlockPos target = firstAirAboveSurface(level, x, z);
            if (!level.getBiome(target).is(TCBiomes.TAINTED_LANDS)) {
                continue;
            }
            BlockState here = level.getBlockState(target);
            if ((!here.isAir() && !here.canBeReplaced()) || !BlockTaintFibre.hasSolidAttachment(level, target)) {
                continue;
            }
            level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), PLACE_FLAGS);
            any = true;
        }
        if (isNaturalPatchAnchor(level, chunk)) {
            any |= placeGuaranteedTaintedNode(level, random, chunk);
            any |= placeGuaranteedTaintacle(level, random, chunk);
        }
        return any;
    }

    private static boolean isNaturalPatchAnchor(WorldGenLevel level, ChunkPos chunk) {
        BiomeSource source = level.getLevel().getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler =
                level.getLevel().getChunkSource().randomState().sampler();
        int quartY = QuartPos.fromBlock(level.getSeaLevel());
        if (!chunkContainsNaturalTaint(source, sampler, chunk.x, chunk.z, quartY)) {
            return false;
        }

        long score = anchorScore(level.getSeed(), chunk.x, chunk.z);
        for (int dx = -PATCH_ANCHOR_RADIUS_CHUNKS; dx <= PATCH_ANCHOR_RADIUS_CHUNKS; dx++) {
            for (int dz = -PATCH_ANCHOR_RADIUS_CHUNKS; dz <= PATCH_ANCHOR_RADIUS_CHUNKS; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                int otherX = chunk.x + dx;
                int otherZ = chunk.z + dz;
                if (!chunkContainsNaturalTaint(source, sampler, otherX, otherZ, quartY)) {
                    continue;
                }
                long otherScore = anchorScore(level.getSeed(), otherX, otherZ);
                if (Long.compareUnsigned(otherScore, score) < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean chunkContainsNaturalTaint(
            BiomeSource source, Climate.Sampler sampler, int chunkX, int chunkZ, int quartY) {
        int baseQuartX = chunkX << 2;
        int baseQuartZ = chunkZ << 2;
        for (int qx = 0; qx < 4; qx++) {
            for (int qz = 0; qz < 4; qz++) {
                if (source.getNoiseBiome(baseQuartX + qx, quartY, baseQuartZ + qz, sampler)
                        .is(TCBiomes.TAINTED_LANDS)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static long anchorScore(long seed, int chunkX, int chunkZ) {
        long value = seed ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L);
        value ^= value >>> 30;
        value *= 0xbf58476d1ce4e5b9L;
        value ^= value >>> 27;
        value *= 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    private static boolean placeGuaranteedTaintedNode(WorldGenLevel level, RandomSource random, ChunkPos chunk) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return false;
        }
        BlockPos surface = findTaintedSurface(level, random, chunk, false);
        if (surface == null) {
            return false;
        }
        BlockPos nodePos = surface;
        if (!level.getBlockState(nodePos).isAir()
                && !level.getBlockState(nodePos).canBeReplaced()) {
            nodePos = nodePos.above();
        }
        return NodeGenerator.createGuaranteedTaintedNodeAt(level, nodePos, random);
    }

    private static boolean placeGuaranteedTaintacle(WorldGenLevel level, RandomSource random, ChunkPos chunk) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || level.getLevel().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        BlockPos pos = findTaintedSurface(level, random, chunk, true);
        if (pos == null) {
            return false;
        }
        if (!level.getBlockState(pos).is(TCBlocks.TAINT_FIBRE.get())) {
            level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), PLACE_FLAGS);
        }

        EntityTaintacle taintacle = TCEntities.TAINTACLE.get().create(level.getLevel());
        if (taintacle == null) {
            return false;
        }
        taintacle.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
        taintacle.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.CHUNK_GENERATION, null);
        // This is the single biome landmark spawn, not a regular mob-cap spawn. Persistence keeps
        // it from despawning before the player reaches the newly generated patch.
        taintacle.setPersistenceRequired();
        level.addFreshEntityWithPassengers(taintacle);
        return true;
    }

    private static BlockPos findTaintedSurface(
            WorldGenLevel level, RandomSource random, ChunkPos chunk, boolean requireFibreSupport) {
        for (int attempt = 0; attempt < ANCHOR_POSITION_ATTEMPTS; attempt++) {
            int x = chunk.getMinBlockX() + random.nextInt(16);
            int z = chunk.getMinBlockZ() + random.nextInt(16);
            BlockPos pos = firstAirAboveSurface(level, x, z);
            if (!level.getBiome(pos).is(TCBiomes.TAINTED_LANDS)) {
                continue;
            }
            if ((!level.getBlockState(pos).isAir() && !level.getBlockState(pos).canBeReplaced())
                    || !level.getBlockState(pos).getFluidState().isEmpty()
                    || !BlockTaintFibre.hasSolidAttachment(level, pos)) {
                continue;
            }
            if (requireFibreSupport && level.getBlockState(pos.below()).getDestroySpeed(level, pos.below()) < 0.0F) {
                continue;
            }
            return pos;
        }
        return null;
    }

    private static boolean placeCrustBlob(WorldGenLevel level, RandomSource random, int x, int z) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        BlockPos center = new BlockPos(x, y, z);
        if (!level.getBiome(center).is(TCBiomes.TAINTED_LANDS)) {
            return false;
        }
        int radius = 1 + random.nextInt(2);
        boolean any = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + random.nextInt(2)) {
                    continue;
                }
                int yy = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x + dx, z + dz) - 1;
                BlockPos pos = new BlockPos(x + dx, yy, z + dz);
                BlockState old = level.getBlockState(pos);
                if (old.isAir() || !old.getFluidState().isEmpty() || old.getDestroySpeed(level, pos) < 0.0F) {
                    continue;
                }
                level.setBlock(pos, TCBlocks.TAINT_CRUST.get().defaultBlockState(), PLACE_FLAGS);
                any = true;
            }
        }
        return any;
    }

    private static BlockPos firstAirAboveSurface(WorldGenLevel level, int x, int z) {
        return new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
    }
}
