package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public final class TaintBiomeManager {
    private static final int QUARTS_PER_CHUNK = 4;
    private static final int CLEAN_SEARCH_RADIUS = 256;

    private TaintBiomeManager() {}

    public static boolean isTainted(ServerLevel level, BlockPos pos) {
        return level.hasChunkAt(pos)
                && level.getNoiseBiome(
                                QuartPos.fromBlock(pos.getX()),
                                QuartPos.fromBlock(pos.getY()),
                                QuartPos.fromBlock(pos.getZ()))
                        .is(TTBiomeTags.IS_TAINTED);
    }

    public static boolean isChangedColumn(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = loadedChunk(level, pos);
        TaintColumns columns = chunk == null ? null : chunk.getExistingDataOrNull(TTAttachments.TAINT_COLUMNS.get());
        return columns != null && columns.isChanged(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getZ()));
    }

    public static boolean taintColumn(ServerLevel level, BlockPos pos) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || isTainted(level, pos)
                || level.getBiome(pos).is(BiomeTags.IS_RIVER)
                || TaintBlooms.isProtected(level, pos)) {
            return false;
        }
        return replaceColumn(level, pos, TTBiomes.TAINTED_LANDS);
    }

    public static boolean replaceColumn(ServerLevel level, BlockPos pos, ResourceKey<Biome> biomeKey) {
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk == null) {
            return false;
        }
        int quartX = QuartPos.fromBlock(pos.getX());
        int quartZ = QuartPos.fromBlock(pos.getZ());
        BiomeSnapshot snapshot = BiomeSnapshot.of(level, chunk);
        if (snapshot.columnHas(quartX, quartZ, biome -> biome.is(biomeKey))) {
            return false;
        }
        Holder<Biome> replacement =
                level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biomeKey);
        rewriteColumn(level, chunk, snapshot, quartX, quartZ, (x, y, z, sampler) -> replacement);
        setChanged(chunk, quartX, quartZ, biomeKey.equals(TTBiomes.TAINTED_LANDS));
        return true;
    }

    public static boolean restoreColumn(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk == null || !isTainted(level, pos)) {
            return false;
        }
        int quartX = QuartPos.fromBlock(pos.getX());
        int quartZ = QuartPos.fromBlock(pos.getZ());
        BiomeSnapshot snapshot = BiomeSnapshot.of(level, chunk);
        if (!snapshot.columnHas(quartX, quartZ, TaintBiomeManager::isTaintedBiome)) {
            return false;
        }
        BiomeSource source = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        boolean changed = isChangedColumn(level, pos);
        int[] offset = changed
                ? new int[] {0, 0}
                : nearestCleanOffset(source, quartX, QuartPos.fromBlock(pos.getY()), quartZ, sampler);
        Holder<Biome> fallback =
                level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
        rewriteColumn(level, chunk, snapshot, quartX, quartZ, (x, y, z, ignored) -> {
            Holder<Biome> current = snapshot.get(x, y, z);
            if (!isTaintedBiome(current)) {
                return current;
            }
            Holder<Biome> restored = source.getNoiseBiome(x + offset[0], y, z + offset[1], sampler);
            return isTaintedBiome(restored) ? fallback : restored;
        });
        setChanged(chunk, quartX, quartZ, false);
        return true;
    }

    private static boolean isTaintedBiome(Holder<Biome> biome) {
        return biome.is(TTBiomeTags.IS_TAINTED);
    }

    private static int[] nearestCleanOffset(
            BiomeSource source, int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        if (!isTaintedBiome(source.getNoiseBiome(quartX, quartY, quartZ, sampler))) {
            return new int[] {0, 0};
        }
        for (int radius = 1; radius <= CLEAN_SEARCH_RADIUS; radius++) {
            int half = Math.max(1, radius / 2);
            int[][] offsets = {
                {radius, 0},
                {-radius, 0},
                {0, radius},
                {0, -radius},
                {radius, radius},
                {radius, -radius},
                {-radius, radius},
                {-radius, -radius},
                {radius, half},
                {radius, -half},
                {-radius, half},
                {-radius, -half},
                {half, radius},
                {-half, radius},
                {half, -radius},
                {-half, -radius}
            };
            for (int[] offset : offsets) {
                if (!isTaintedBiome(source.getNoiseBiome(quartX + offset[0], quartY, quartZ + offset[1], sampler))) {
                    return offset;
                }
            }
        }
        return new int[] {0, 0};
    }

    private static void rewriteColumn(
            ServerLevel level,
            LevelChunk chunk,
            BiomeSnapshot snapshot,
            int targetQuartX,
            int targetQuartZ,
            BiomeResolver target) {
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        chunk.fillBiomesFromNoise(
                (x, y, z, ignored) -> x == targetQuartX && z == targetQuartZ
                        ? target.getNoiseBiome(x, y, z, sampler)
                        : snapshot.get(x, y, z),
                sampler);
        chunk.setUnsaved(true);
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.<ChunkAccess>of(chunk));
    }

    private static void setChanged(LevelChunk chunk, int quartX, int quartZ, boolean changed) {
        TaintColumns columns = changed
                ? chunk.getData(TTAttachments.TAINT_COLUMNS.get())
                : chunk.getExistingDataOrNull(TTAttachments.TAINT_COLUMNS.get());
        if (columns == null) {
            return;
        }
        columns.setChanged(quartX, quartZ, changed);
        if (columns.isEmpty()) {
            chunk.removeData(TTAttachments.TAINT_COLUMNS.get());
        }
        chunk.setUnsaved(true);
    }

    private static @Nullable LevelChunk loadedChunk(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkSource()
                .getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (chunk != null) TaintLegacyData.importChunk(level, chunk);
        return chunk;
    }

    private record BiomeSnapshot(ChunkPos chunkPos, int minQuartY, int quartHeight, List<Holder<Biome>> biomes) {
        static BiomeSnapshot of(ServerLevel level, LevelChunk chunk) {
            int minQuartY = QuartPos.fromBlock(level.getMinBuildHeight());
            int quartHeight = QuartPos.fromBlock((level.getMaxBuildHeight() - 1)) - minQuartY + 1;
            int baseX = QuartPos.fromSection(chunk.getPos().x);
            int baseZ = QuartPos.fromSection(chunk.getPos().z);
            List<Holder<Biome>> biomes = new ArrayList<>(quartHeight * QUARTS_PER_CHUNK * QUARTS_PER_CHUNK);
            for (int y = 0; y < quartHeight; y++) {
                for (int z = 0; z < QUARTS_PER_CHUNK; z++) {
                    for (int x = 0; x < QUARTS_PER_CHUNK; x++) {
                        biomes.add(chunk.getNoiseBiome(baseX + x, minQuartY + y, baseZ + z));
                    }
                }
            }
            return new BiomeSnapshot(chunk.getPos(), minQuartY, quartHeight, biomes);
        }

        Holder<Biome> get(int quartX, int quartY, int quartZ) {
            int x = quartX - QuartPos.fromSection(chunkPos.x);
            int z = quartZ - QuartPos.fromSection(chunkPos.z);
            int y = Math.clamp(quartY - minQuartY, 0, quartHeight - 1);
            if (x < 0 || x >= QUARTS_PER_CHUNK || z < 0 || z >= QUARTS_PER_CHUNK) {
                throw new IllegalArgumentException("Biome resolver asked for a quart outside chunk " + chunkPos);
            }
            return biomes.get((y * QUARTS_PER_CHUNK + z) * QUARTS_PER_CHUNK + x);
        }

        boolean columnHas(int quartX, int quartZ, Predicate<Holder<Biome>> predicate) {
            for (int y = 0; y < quartHeight; y++) {
                if (predicate.test(get(quartX, minQuartY + y, quartZ))) {
                    return true;
                }
            }
            return false;
        }
    }
}
