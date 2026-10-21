package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import java.util.Random;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class AuraGenHandler {
    private static final int SAMPLE_HEIGHT = 50;
    private static final int CHUNK_CENTER_OFFSET = 8;
    private static final int CHUNK_WIDTH = 16;
    private static final float DEFAULT_MODIFIER = 0.5F;
    private static final double VARIANCE_FACTOR = 0.1;
    private static final long UNSIGNED_INT_MASK = 0xFFFFFFFFL;
    private static final int KEY_Z_SHIFT = 32;
    private static final int[][] SAMPLE_OFFSETS = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private AuraGenHandler() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AuraManager.onChunkLoaded(level, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AuraManager.onChunkUnloaded(level, event.getChunk().getPos());
        }
    }

    static void initializeIfNeeded(ServerLevel level, LevelChunk chunk, AuraData data) {
        if (data.isInitialized()) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        boolean pristine = data.getVis() == 0.0F && data.getFlux() == 0.0F;
        short base = generateBase(level, pos);
        data.setBase(base);
        data.setChunkPos(pos);
        if (pristine) {
            data.setVis(base);
        }
        chunk.markUnsaved();
    }

    static short generateBase(ServerLevel level, ChunkPos pos) {
        double modifierSum = 0.0;
        for (int[] offset : SAMPLE_OFFSETS) {
            modifierSum += modifierAt(level, pos.x() + offset[0], pos.z() + offset[1]);
        }
        double mean = modifierSum / SAMPLE_OFFSETS.length;
        long key = (pos.x() & UNSIGNED_INT_MASK) | ((pos.z() & UNSIGNED_INT_MASK) << KEY_Z_SHIFT);
        double gaussian = new Random(level.getSeed() ^ key).nextGaussian();
        int base = (int) (mean * AuraManager.AURA_CEILING * (1.0 + VARIANCE_FACTOR * gaussian));
        return (short) Mth.clamp(base, 0, AuraManager.AURA_CEILING);
    }

    static Optional<UUID> loadedBiomeRevision(ServerLevel level, ChunkPos pos) {
        StringBuilder samples = new StringBuilder();
        for (int[] offset : SAMPLE_OFFSETS) {
            int chunkX = pos.x() + offset[0];
            int chunkZ = pos.z() + offset[1];
            if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) {
                return Optional.empty();
            }
            int blockX = chunkX * CHUNK_WIDTH + CHUNK_CENTER_OFFSET;
            int blockZ = chunkZ * CHUNK_WIDTH + CHUNK_CENTER_OFFSET;
            Holder<Biome> biome = level.getNoiseBiome(QuartPos.fromBlock(blockX), QuartPos.fromBlock(SAMPLE_HEIGHT), QuartPos.fromBlock(blockZ));
            if (biome.unwrapKey().isEmpty()) {
                return Optional.empty();
            }
            samples.append(biome.unwrapKey().orElseThrow().identifier()).append(':').append(Float.floatToIntBits(modifierAt(level, chunkX, chunkZ))).append(';');
        }
        return Optional.of(UUID.nameUUIDFromBytes(samples.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private static float modifierAt(ServerLevel level, int chunkX, int chunkZ) {
        int blockX = chunkX * CHUNK_WIDTH + CHUNK_CENTER_OFFSET;
        int blockZ = chunkZ * CHUNK_WIDTH + CHUNK_CENTER_OFFSET;
        Holder<Biome> biome = level.getNoiseBiome(QuartPos.fromBlock(blockX), QuartPos.fromBlock(SAMPLE_HEIGHT), QuartPos.fromBlock(blockZ));
        BiomeAuraModifier modifier = biome.getData(TTDataMaps.BIOME_AURA_MODIFIER);
        return modifier == null ? DEFAULT_MODIFIER : modifier.value();
    }
}
