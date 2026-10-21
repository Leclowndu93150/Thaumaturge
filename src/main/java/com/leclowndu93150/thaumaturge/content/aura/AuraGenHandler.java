package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class AuraGenHandler {
    private static final float DEFAULT_MODIFIER = 0.5F;
    private static final int MAX_BASE = 500;
    private static final double BASE_SPREAD = 0.1;
    private static final int PROBE_HEIGHT = 64;
    private static final int BASE_SEED_SALT = 0x6A75;
    private static final int[][] NEIGHBOURHOOD = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

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
        Holder<Biome> centre = biomeAtCentre(level, pos);
        short base = rollBase(level, pos, centre);
        data.setBase(base);
        data.setChunkPos(pos);
        if (pristine) {
            data.setVis(base);
            if (centre.is(TTBiomeTags.IS_TAINTED)) {
                data.setFlux(base
                        * ThaumaturgeCommonConfig.TAINTED_LANDS_STARTING_FLUX
                                .get()
                                .floatValue());
            }
        }
        chunk.setUnsaved(true);
    }

    private static short rollBase(ServerLevel level, ChunkPos pos, Holder<Biome> centre) {
        float average = neighbourhoodModifier(level, pos, centre);
        RandomSource seeded = RandomSource.create(level.getSeed() + Mth.getSeed(pos.x, BASE_SEED_SALT, pos.z));
        double scaled = average * MAX_BASE * (1.0 + seeded.nextGaussian() * BASE_SPREAD);
        return (short) Mth.clamp(Mth.floor(scaled), 0, MAX_BASE);
    }

    private static float neighbourhoodModifier(ServerLevel level, ChunkPos pos, Holder<Biome> centre) {
        float sum = 0.0F;
        for (int[] step : NEIGHBOURHOOD) {
            Holder<Biome> biome = step[0] == 0 && step[1] == 0
                    ? centre
                    : biomeAtCentre(level, new ChunkPos(pos.x + step[0], pos.z + step[1]));
            sum += modifier(biome);
        }
        return sum / NEIGHBOURHOOD.length;
    }

    private static Holder<Biome> biomeAtCentre(ServerLevel level, ChunkPos pos) {
        return level.getNoiseBiome(
                QuartPos.fromBlock(pos.getMiddleBlockX()),
                QuartPos.fromBlock(PROBE_HEIGHT),
                QuartPos.fromBlock(pos.getMiddleBlockZ()));
    }

    private static float modifier(Holder<Biome> biome) {
        BiomeAuraModifier modifier = biome.getData(TTDataMaps.BIOME_AURA_MODIFIER);
        return modifier == null ? DEFAULT_MODIFIER : modifier.value();
    }
}
