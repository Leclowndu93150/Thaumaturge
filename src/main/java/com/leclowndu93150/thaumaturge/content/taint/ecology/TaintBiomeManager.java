package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Applies and restores the TC4-style Tainted Lands biome without force-loading chunks.
 *
 * <p>Modern Minecraft stores biomes at quart resolution (4x4x4 cells), so one legacy x/z biome
 * column maps to one 4x4 quart column here. Infection deliberately replaces that quart column at
 * every biome Y layer, preserving TC4's two-dimensional "land takeover" semantics. Restoration
 * asks the active chunk generator's biome source what each Y layer originally should have been,
 * which also preserves modded/TerraBlender biome choices instead of blindly restoring Plains.
 */
public final class TaintBiomeManager {
    private TaintBiomeManager() {}

    public static boolean isTainted(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBiome(pos).is(TCBiomes.TAINTED_LANDS);
    }

    public static boolean isDynamicallyTainted(ServerLevel level, BlockPos pos) {
        return isTainted(level, pos) && TaintBiomeState.get(level).isDynamic(pos);
    }

    public static boolean taintColumn(ServerLevel level, BlockPos pos) {
        if (isTainted(level, pos)) {
            return false;
        }
        // Keep rivers as natural firebreaks. TC4's fibre already struggled to cross open water;
        // preserving modern river biomes makes that containment behavior explicit and prevents
        // natural/dynamic Tainted Lands from painting over river channels.
        if (level.getBiome(pos).is(BiomeTags.IS_RIVER)) {
            return false;
        }
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk == null) {
            return false;
        }
        Holder<Biome> tainted =
                level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(TCBiomes.TAINTED_LANDS);
        int targetQuartX = QuartPos.fromBlock(pos.getX());
        int targetQuartZ = QuartPos.fromBlock(pos.getZ());
        BiomeSnapshot snapshot = snapshot(level, chunk);
        if (snapshot.columnMatches(targetQuartX, targetQuartZ, biome -> biome.is(TCBiomes.TAINTED_LANDS))) {
            return false;
        }

        rewriteColumn(level, chunk, snapshot, targetQuartX, targetQuartZ, (quartX, quartY, quartZ, sampler) -> tainted);
        TaintBiomeState.get(level).markDynamic(pos);
        return true;
    }

    public static boolean replaceColumn(
            ServerLevel level, BlockPos pos, net.minecraft.resources.ResourceKey<Biome> biomeKey) {
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk == null) {
            return false;
        }
        Holder<Biome> replacement =
                level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biomeKey);
        int targetQuartX = QuartPos.fromBlock(pos.getX());
        int targetQuartZ = QuartPos.fromBlock(pos.getZ());
        BiomeSnapshot snapshot = snapshot(level, chunk);
        if (snapshot.columnMatches(targetQuartX, targetQuartZ, biome -> biome.is(biomeKey))) {
            return false;
        }
        rewriteColumn(
                level, chunk, snapshot, targetQuartX, targetQuartZ, (quartX, quartY, quartZ, sampler) -> replacement);
        if (!biomeKey.equals(TCBiomes.TAINTED_LANDS)) {
            TaintBiomeState.get(level).clearDynamic(pos);
        }
        return true;
    }

    public static boolean restoreColumn(ServerLevel level, BlockPos pos) {
        if (!isTainted(level, pos)) {
            return false;
        }
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk == null) {
            return false;
        }
        int targetQuartX = QuartPos.fromBlock(pos.getX());
        int targetQuartZ = QuartPos.fromBlock(pos.getZ());
        BiomeSnapshot snapshot = snapshot(level, chunk);
        if (!snapshot.columnMatches(targetQuartX, targetQuartZ, biome -> biome.is(TCBiomes.TAINTED_LANDS))) {
            return false;
        }

        BiomeSource originalSource = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        boolean dynamic = TaintBiomeState.get(level).isDynamic(pos);
        CleanOffset naturalFallback = dynamic
                ? CleanOffset.SAME_COLUMN
                : findNearestCleanGeneratorColumn(
                        originalSource, targetQuartX, QuartPos.fromBlock(pos.getY()), targetQuartZ, sampler);
        Holder<Biome> emergencyFallback =
                level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);

        rewriteColumn(level, chunk, snapshot, targetQuartX, targetQuartZ, (quartX, quartY, quartZ, ignoredSampler) -> {
            Holder<Biome> current = snapshot.get(quartX, quartY, quartZ);
            if (!current.is(TCBiomes.TAINTED_LANDS)) {
                return current;
            }
            Holder<Biome> restored = originalSource.getNoiseBiome(
                    quartX + naturalFallback.dx(), quartY, quartZ + naturalFallback.dz(), sampler);
            return restored.is(TCBiomes.TAINTED_LANDS) ? emergencyFallback : restored;
        });
        TaintBiomeState.get(level).clearDynamic(pos);
        return true;
    }

    private static CleanOffset findNearestCleanGeneratorColumn(
            BiomeSource source, int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        if (!source.getNoiseBiome(quartX, quartY, quartZ, sampler).is(TCBiomes.TAINTED_LANDS)) {
            return CleanOffset.SAME_COLUMN;
        }
        // Natural Tainted Lands has no hidden pre-replacement biome to restore. Sample outward
        // along cardinal, diagonal, and half-diagonal rays; this stays bounded even when a Bloom
        // is cleansing a large natural biome while still finding a nearby climate-compatible
        // generator column in ordinary TerraBlender region shapes.
        for (int radius = 1; radius <= 256; radius++) {
            int half = Math.max(1, radius / 2);
            int[][] offsets = {
                {radius, 0}, {-radius, 0}, {0, radius}, {0, -radius},
                {radius, radius}, {radius, -radius}, {-radius, radius}, {-radius, -radius},
                {radius, half}, {radius, -half}, {-radius, half}, {-radius, -half},
                {half, radius}, {-half, radius}, {half, -radius}, {-half, -radius}
            };
            for (int[] offset : offsets) {
                CleanOffset clean = cleanOffset(source, quartX, quartY, quartZ, offset[0], offset[1], sampler);
                if (clean != null) {
                    return clean;
                }
            }
        }
        return CleanOffset.SAME_COLUMN;
    }

    private static CleanOffset cleanOffset(
            BiomeSource source, int quartX, int quartY, int quartZ, int dx, int dz, Climate.Sampler sampler) {
        return source.getNoiseBiome(quartX + dx, quartY, quartZ + dz, sampler).is(TCBiomes.TAINTED_LANDS)
                ? null
                : new CleanOffset(dx, dz);
    }

    private static void rewriteColumn(
            ServerLevel level,
            LevelChunk chunk,
            BiomeSnapshot snapshot,
            int targetQuartX,
            int targetQuartZ,
            BiomeResolver targetResolver) {
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        chunk.fillBiomesFromNoise(
                (quartX, quartY, quartZ, ignoredSampler) -> {
                    if (quartX == targetQuartX && quartZ == targetQuartZ) {
                        return targetResolver.getNoiseBiome(quartX, quartY, quartZ, sampler);
                    }
                    return snapshot.get(quartX, quartY, quartZ);
                },
                sampler);
        chunk.setUnsaved(true);
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
    }

    private static LevelChunk loadedChunk(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        return level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static BiomeSnapshot snapshot(ServerLevel level, LevelChunk chunk) {
        int minQuartY = QuartPos.fromBlock(level.getMinBuildHeight());
        int maxQuartY = QuartPos.fromBlock(level.getMaxBuildHeight() - 1);
        int quartHeight = maxQuartY - minQuartY + 1;
        int baseQuartX = chunk.getPos().x << 2;
        int baseQuartZ = chunk.getPos().z << 2;
        List<Holder<Biome>> biomes = new ArrayList<>(quartHeight * 16);
        for (int quartY = minQuartY; quartY <= maxQuartY; quartY++) {
            for (int localQuartZ = 0; localQuartZ < 4; localQuartZ++) {
                for (int localQuartX = 0; localQuartX < 4; localQuartX++) {
                    biomes.add(chunk.getNoiseBiome(baseQuartX + localQuartX, quartY, baseQuartZ + localQuartZ));
                }
            }
        }
        return new BiomeSnapshot(chunk.getPos(), minQuartY, quartHeight, biomes);
    }

    private record CleanOffset(int dx, int dz) {
        private static final CleanOffset SAME_COLUMN = new CleanOffset(0, 0);
    }

    private record BiomeSnapshot(ChunkPos chunkPos, int minQuartY, int quartHeight, List<Holder<Biome>> biomes) {
        private Holder<Biome> get(int quartX, int quartY, int quartZ) {
            int localQuartX = quartX - (chunkPos.x << 2);
            int localQuartZ = quartZ - (chunkPos.z << 2);
            int localQuartY = quartY - minQuartY;
            if (localQuartX < 0
                    || localQuartX >= 4
                    || localQuartZ < 0
                    || localQuartZ >= 4
                    || localQuartY < 0
                    || localQuartY >= quartHeight) {
                throw new IllegalArgumentException("Biome resolver requested coordinates outside its chunk snapshot");
            }
            return biomes.get(localQuartY * 16 + localQuartZ * 4 + localQuartX);
        }

        private boolean columnMatches(int quartX, int quartZ, java.util.function.Predicate<Holder<Biome>> predicate) {
            for (int quartY = minQuartY; quartY < minQuartY + quartHeight; quartY++) {
                if (predicate.test(get(quartX, quartY, quartZ))) {
                    return true;
                }
            }
            return false;
        }
    }
}
