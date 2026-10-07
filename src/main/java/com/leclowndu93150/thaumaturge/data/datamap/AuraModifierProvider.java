package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class AuraModifierProvider extends DataMapProvider {
    public AuraModifierProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<BiomeAuraModifier, Biome> b = builder(TTDataMaps.BIOME_AURA_MODIFIER);

        add(b, Biomes.PLAINS, 0.3F);
        add(b, Biomes.SUNFLOWER_PLAINS, 0.3F);
        add(b, Biomes.SNOWY_PLAINS, 0.275F);
        add(b, Biomes.ICE_SPIKES, 0.25F);
        add(b, Biomes.DESERT, 0.25F);
        add(b, Biomes.SWAMP, 0.5F);
        add(b, Biomes.MANGROVE_SWAMP, 0.55F);
        add(b, Biomes.FOREST, 0.5F);
        add(b, Biomes.FLOWER_FOREST, 0.5F);
        add(b, Biomes.BIRCH_FOREST, 0.5F);
        add(b, Biomes.DARK_FOREST, 0.5F);
        add(b, Biomes.OLD_GROWTH_BIRCH_FOREST, 0.5F);
        add(b, Biomes.OLD_GROWTH_PINE_TAIGA, 0.4F);
        add(b, Biomes.OLD_GROWTH_SPRUCE_TAIGA, 0.4F);
        add(b, Biomes.TAIGA, 0.4F);
        add(b, Biomes.SNOWY_TAIGA, 0.35F);
        add(b, Biomes.SAVANNA, 0.25F);
        add(b, Biomes.SAVANNA_PLATEAU, 0.25F);
        add(b, Biomes.WINDSWEPT_HILLS, 0.3F);
        add(b, Biomes.WINDSWEPT_GRAVELLY_HILLS, 0.3F);
        add(b, Biomes.WINDSWEPT_FOREST, 0.4F);
        add(b, Biomes.WINDSWEPT_SAVANNA, 0.275F);
        add(b, Biomes.JUNGLE, 0.6F);
        add(b, Biomes.SPARSE_JUNGLE, 0.4F);
        add(b, Biomes.BAMBOO_JUNGLE, 0.65F);
        add(b, Biomes.BADLANDS, 0.33F);
        add(b, Biomes.ERODED_BADLANDS, 0.33F);
        add(b, Biomes.WOODED_BADLANDS, 0.4F);
        add(b, Biomes.MEADOW, 0.4F);
        add(b, Biomes.CHERRY_GROVE, 0.6F);
        add(b, Biomes.GROVE, 0.4F);
        add(b, Biomes.SNOWY_SLOPES, 0.275F);
        add(b, Biomes.FROZEN_PEAKS, 0.275F);
        add(b, Biomes.JAGGED_PEAKS, 0.3F);
        add(b, Biomes.STONY_PEAKS, 0.3F);
        add(b, Biomes.RIVER, 0.4F);
        add(b, Biomes.FROZEN_RIVER, 0.35F);
        add(b, Biomes.BEACH, 0.3F);
        add(b, Biomes.SNOWY_BEACH, 0.275F);
        add(b, Biomes.STONY_SHORE, 0.3F);
        add(b, Biomes.WARM_OCEAN, 0.33F);
        add(b, Biomes.LUKEWARM_OCEAN, 0.33F);
        add(b, Biomes.DEEP_LUKEWARM_OCEAN, 0.33F);
        add(b, Biomes.OCEAN, 0.33F);
        add(b, Biomes.DEEP_OCEAN, 0.33F);
        add(b, Biomes.COLD_OCEAN, 0.33F);
        add(b, Biomes.DEEP_COLD_OCEAN, 0.33F);
        add(b, Biomes.FROZEN_OCEAN, 0.3F);
        add(b, Biomes.DEEP_FROZEN_OCEAN, 0.3F);
        add(b, Biomes.MUSHROOM_FIELDS, 0.75F);
        add(b, Biomes.DRIPSTONE_CAVES, 0.3F);
        add(b, Biomes.LUSH_CAVES, 0.5F);
        add(b, Biomes.DEEP_DARK, 0.1F);
        add(b, Biomes.NETHER_WASTES, 0.125F);
        add(b, Biomes.WARPED_FOREST, 0.125F);
        add(b, Biomes.CRIMSON_FOREST, 0.125F);
        add(b, Biomes.SOUL_SAND_VALLEY, 0.125F);
        add(b, Biomes.BASALT_DELTAS, 0.125F);
        add(b, Biomes.THE_END, 0.125F);
        add(b, Biomes.END_HIGHLANDS, 0.125F);
        add(b, Biomes.END_MIDLANDS, 0.125F);
        add(b, Biomes.SMALL_END_ISLANDS, 0.125F);
        add(b, Biomes.END_BARRENS, 0.125F);
        add(b, Biomes.THE_VOID, 0.0F);

        add(b, TTBiomes.MAGICAL_FOREST, 0.625F);
        add(b, TTBiomes.MAGICAL_FOREST_CAVES, 0.625F);
        add(b, TTBiomes.EERIE, 0.625F);
        add(b, TTBiomes.ELDRITCH, 0.458F);
        add(b, TTBiomes.TAINTED_LANDS, 0.45F);

        Builder<BiomeAspects, Biome> aspects = builder(TTDataMaps.BIOME_ASPECTS);

        addAspects(aspects, Biomes.PLAINS, TTAspects.AER);
        addAspects(aspects, Biomes.SUNFLOWER_PLAINS, TTAspects.AER);
        addAspects(aspects, Biomes.MEADOW, TTAspects.AER);
        addAspects(aspects, Biomes.SNOWY_PLAINS, TTAspects.ORDO);
        addAspects(aspects, Biomes.ICE_SPIKES, TTAspects.ORDO);
        addAspects(aspects, Biomes.GROVE, TTAspects.ORDO);
        addAspects(aspects, Biomes.SNOWY_SLOPES, TTAspects.ORDO);
        addAspects(aspects, Biomes.FROZEN_PEAKS, TTAspects.ORDO);
        addAspects(aspects, Biomes.JAGGED_PEAKS, TTAspects.AER);
        addAspects(aspects, Biomes.STONY_PEAKS, TTAspects.AER);
        addAspects(aspects, Biomes.WINDSWEPT_HILLS, TTAspects.AER);
        addAspects(aspects, Biomes.WINDSWEPT_GRAVELLY_HILLS, TTAspects.AER);
        addAspects(aspects, Biomes.WINDSWEPT_FOREST, TTAspects.AER, TTAspects.TERRA);
        addAspects(aspects, Biomes.WINDSWEPT_SAVANNA, TTAspects.AER, TTAspects.IGNIS);
        addAspects(aspects, Biomes.SAVANNA, TTAspects.AER, TTAspects.IGNIS);
        addAspects(aspects, Biomes.SAVANNA_PLATEAU, TTAspects.AER, TTAspects.IGNIS);
        addAspects(aspects, Biomes.DESERT, TTAspects.IGNIS, TTAspects.TERRA, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.BADLANDS, TTAspects.IGNIS, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.ERODED_BADLANDS, TTAspects.IGNIS, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.WOODED_BADLANDS, TTAspects.IGNIS, TTAspects.TERRA);
        addAspects(aspects, Biomes.SWAMP, TTAspects.PERDITIO, TTAspects.AQUA);
        addAspects(aspects, Biomes.MANGROVE_SWAMP, TTAspects.PERDITIO, TTAspects.AQUA);
        addAspects(aspects, Biomes.FOREST, TTAspects.TERRA);
        addAspects(aspects, Biomes.FLOWER_FOREST, TTAspects.TERRA);
        addAspects(aspects, Biomes.BIRCH_FOREST, TTAspects.TERRA);
        addAspects(aspects, Biomes.OLD_GROWTH_BIRCH_FOREST, TTAspects.TERRA);
        addAspects(aspects, Biomes.DARK_FOREST, TTAspects.TERRA);
        addAspects(aspects, Biomes.CHERRY_GROVE, TTAspects.TERRA);
        addAspects(aspects, Biomes.TAIGA, TTAspects.TERRA, TTAspects.ORDO);
        addAspects(aspects, Biomes.SNOWY_TAIGA, TTAspects.TERRA, TTAspects.ORDO);
        addAspects(aspects, Biomes.OLD_GROWTH_PINE_TAIGA, TTAspects.TERRA, TTAspects.ORDO);
        addAspects(aspects, Biomes.OLD_GROWTH_SPRUCE_TAIGA, TTAspects.TERRA, TTAspects.ORDO);
        addAspects(aspects, Biomes.JUNGLE, TTAspects.TERRA, TTAspects.AQUA);
        addAspects(aspects, Biomes.SPARSE_JUNGLE, TTAspects.TERRA, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.BAMBOO_JUNGLE, TTAspects.TERRA, TTAspects.AQUA);
        addAspects(aspects, Biomes.RIVER, TTAspects.AQUA);
        addAspects(aspects, Biomes.FROZEN_RIVER, TTAspects.AQUA, TTAspects.ORDO);
        addAspects(aspects, Biomes.BEACH, TTAspects.TERRA, TTAspects.AQUA);
        addAspects(aspects, Biomes.SNOWY_BEACH, TTAspects.ORDO, TTAspects.AQUA);
        addAspects(aspects, Biomes.STONY_SHORE, TTAspects.TERRA, TTAspects.AQUA);
        addAspects(aspects, Biomes.OCEAN, TTAspects.AQUA);
        addAspects(aspects, Biomes.DEEP_OCEAN, TTAspects.AQUA);
        addAspects(aspects, Biomes.WARM_OCEAN, TTAspects.AQUA);
        addAspects(aspects, Biomes.LUKEWARM_OCEAN, TTAspects.AQUA);
        addAspects(aspects, Biomes.DEEP_LUKEWARM_OCEAN, TTAspects.AQUA);
        addAspects(aspects, Biomes.COLD_OCEAN, TTAspects.AQUA, TTAspects.ORDO);
        addAspects(aspects, Biomes.DEEP_COLD_OCEAN, TTAspects.AQUA, TTAspects.ORDO);
        addAspects(aspects, Biomes.FROZEN_OCEAN, TTAspects.AQUA, TTAspects.ORDO);
        addAspects(aspects, Biomes.DEEP_FROZEN_OCEAN, TTAspects.AQUA, TTAspects.ORDO);
        addAspects(aspects, Biomes.MUSHROOM_FIELDS, TTAspects.ORDO);
        addAspects(aspects, Biomes.DRIPSTONE_CAVES, TTAspects.TERRA);
        addAspects(aspects, Biomes.LUSH_CAVES, TTAspects.AQUA, TTAspects.TERRA);
        addAspects(aspects, Biomes.DEEP_DARK, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.NETHER_WASTES, TTAspects.IGNIS);
        addAspects(aspects, Biomes.WARPED_FOREST, TTAspects.IGNIS, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.CRIMSON_FOREST, TTAspects.IGNIS);
        addAspects(aspects, Biomes.SOUL_SAND_VALLEY, TTAspects.IGNIS, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.BASALT_DELTAS, TTAspects.IGNIS);
        addAspects(aspects, Biomes.THE_END, TTAspects.AER, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.END_HIGHLANDS, TTAspects.AER, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.END_MIDLANDS, TTAspects.AER, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.SMALL_END_ISLANDS, TTAspects.AER, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.END_BARRENS, TTAspects.AER, TTAspects.PERDITIO);
        addAspects(aspects, Biomes.THE_VOID, TTAspects.PERDITIO);

        addAspects(aspects, TTBiomes.MAGICAL_FOREST, TTAspects.ORDO, TTAspects.TERRA);
        addAspects(aspects, TTBiomes.MAGICAL_FOREST_CAVES, TTAspects.ORDO, TTAspects.TERRA);
        addAspects(aspects, TTBiomes.EERIE, TTAspects.ORDO, TTAspects.IGNIS);
        addAspects(aspects, TTBiomes.ELDRITCH, TTAspects.ORDO, TTAspects.IGNIS, TTAspects.AER);
        addAspects(aspects, TTBiomes.TAINTED_LANDS, TTAspects.PERDITIO);
    }

    @SafeVarargs
    private static void addAspects(
            Builder<BiomeAspects, Biome> b, ResourceKey<Biome> key, ResourceKey<IAspect>... aspectKeys) {
        b.add(key, new BiomeAspects(List.of(aspectKeys)), false);
    }

    private static void add(Builder<BiomeAuraModifier, Biome> b, ResourceKey<Biome> key, float value) {
        b.add(key, new BiomeAuraModifier(value), false);
    }
}
