package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import com.leclowndu93150.thaumaturge.registry.TCDataMaps;
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
        Builder<BiomeAuraModifier, Biome> b = builder(TCDataMaps.BIOME_AURA_MODIFIER);

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
        add(b, Biomes.PALE_GARDEN, 0.6F);
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

        add(b, TCBiomes.MAGICAL_FOREST, 0.625F);
        add(b, TCBiomes.EERIE, 0.625F);
        add(b, TCBiomes.ELDRITCH, 0.458F);

        Builder<BiomeAspects, Biome> aspects = builder(TCDataMaps.BIOME_ASPECTS);

        addAspects(aspects, Biomes.PLAINS, TCAspects.AER);
        addAspects(aspects, Biomes.SUNFLOWER_PLAINS, TCAspects.AER);
        addAspects(aspects, Biomes.MEADOW, TCAspects.AER);
        addAspects(aspects, Biomes.SNOWY_PLAINS, TCAspects.ORDO);
        addAspects(aspects, Biomes.ICE_SPIKES, TCAspects.ORDO);
        addAspects(aspects, Biomes.GROVE, TCAspects.ORDO);
        addAspects(aspects, Biomes.SNOWY_SLOPES, TCAspects.ORDO);
        addAspects(aspects, Biomes.FROZEN_PEAKS, TCAspects.ORDO);
        addAspects(aspects, Biomes.JAGGED_PEAKS, TCAspects.AER);
        addAspects(aspects, Biomes.STONY_PEAKS, TCAspects.AER);
        addAspects(aspects, Biomes.WINDSWEPT_HILLS, TCAspects.AER);
        addAspects(aspects, Biomes.WINDSWEPT_GRAVELLY_HILLS, TCAspects.AER);
        addAspects(aspects, Biomes.WINDSWEPT_FOREST, TCAspects.AER, TCAspects.TERRA);
        addAspects(aspects, Biomes.WINDSWEPT_SAVANNA, TCAspects.AER, TCAspects.IGNIS);
        addAspects(aspects, Biomes.SAVANNA, TCAspects.AER, TCAspects.IGNIS);
        addAspects(aspects, Biomes.SAVANNA_PLATEAU, TCAspects.AER, TCAspects.IGNIS);
        addAspects(aspects, Biomes.DESERT, TCAspects.IGNIS, TCAspects.TERRA, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.BADLANDS, TCAspects.IGNIS, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.ERODED_BADLANDS, TCAspects.IGNIS, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.WOODED_BADLANDS, TCAspects.IGNIS, TCAspects.TERRA);
        addAspects(aspects, Biomes.SWAMP, TCAspects.PERDITIO, TCAspects.AQUA);
        addAspects(aspects, Biomes.MANGROVE_SWAMP, TCAspects.PERDITIO, TCAspects.AQUA);
        addAspects(aspects, Biomes.FOREST, TCAspects.TERRA);
        addAspects(aspects, Biomes.FLOWER_FOREST, TCAspects.TERRA);
        addAspects(aspects, Biomes.BIRCH_FOREST, TCAspects.TERRA);
        addAspects(aspects, Biomes.OLD_GROWTH_BIRCH_FOREST, TCAspects.TERRA);
        addAspects(aspects, Biomes.DARK_FOREST, TCAspects.TERRA);
        addAspects(aspects, Biomes.PALE_GARDEN, TCAspects.TERRA, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.CHERRY_GROVE, TCAspects.TERRA);
        addAspects(aspects, Biomes.TAIGA, TCAspects.TERRA, TCAspects.ORDO);
        addAspects(aspects, Biomes.SNOWY_TAIGA, TCAspects.TERRA, TCAspects.ORDO);
        addAspects(aspects, Biomes.OLD_GROWTH_PINE_TAIGA, TCAspects.TERRA, TCAspects.ORDO);
        addAspects(aspects, Biomes.OLD_GROWTH_SPRUCE_TAIGA, TCAspects.TERRA, TCAspects.ORDO);
        addAspects(aspects, Biomes.JUNGLE, TCAspects.TERRA, TCAspects.AQUA);
        addAspects(aspects, Biomes.SPARSE_JUNGLE, TCAspects.TERRA, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.BAMBOO_JUNGLE, TCAspects.TERRA, TCAspects.AQUA);
        addAspects(aspects, Biomes.RIVER, TCAspects.AQUA);
        addAspects(aspects, Biomes.FROZEN_RIVER, TCAspects.AQUA, TCAspects.ORDO);
        addAspects(aspects, Biomes.BEACH, TCAspects.TERRA, TCAspects.AQUA);
        addAspects(aspects, Biomes.SNOWY_BEACH, TCAspects.ORDO, TCAspects.AQUA);
        addAspects(aspects, Biomes.STONY_SHORE, TCAspects.TERRA, TCAspects.AQUA);
        addAspects(aspects, Biomes.OCEAN, TCAspects.AQUA);
        addAspects(aspects, Biomes.DEEP_OCEAN, TCAspects.AQUA);
        addAspects(aspects, Biomes.WARM_OCEAN, TCAspects.AQUA);
        addAspects(aspects, Biomes.LUKEWARM_OCEAN, TCAspects.AQUA);
        addAspects(aspects, Biomes.DEEP_LUKEWARM_OCEAN, TCAspects.AQUA);
        addAspects(aspects, Biomes.COLD_OCEAN, TCAspects.AQUA, TCAspects.ORDO);
        addAspects(aspects, Biomes.DEEP_COLD_OCEAN, TCAspects.AQUA, TCAspects.ORDO);
        addAspects(aspects, Biomes.FROZEN_OCEAN, TCAspects.AQUA, TCAspects.ORDO);
        addAspects(aspects, Biomes.DEEP_FROZEN_OCEAN, TCAspects.AQUA, TCAspects.ORDO);
        addAspects(aspects, Biomes.MUSHROOM_FIELDS, TCAspects.ORDO);
        addAspects(aspects, Biomes.DRIPSTONE_CAVES, TCAspects.TERRA);
        addAspects(aspects, Biomes.LUSH_CAVES, TCAspects.AQUA, TCAspects.TERRA);
        addAspects(aspects, Biomes.DEEP_DARK, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.NETHER_WASTES, TCAspects.IGNIS);
        addAspects(aspects, Biomes.WARPED_FOREST, TCAspects.IGNIS, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.CRIMSON_FOREST, TCAspects.IGNIS);
        addAspects(aspects, Biomes.SOUL_SAND_VALLEY, TCAspects.IGNIS, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.BASALT_DELTAS, TCAspects.IGNIS);
        addAspects(aspects, Biomes.THE_END, TCAspects.AER, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.END_HIGHLANDS, TCAspects.AER, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.END_MIDLANDS, TCAspects.AER, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.SMALL_END_ISLANDS, TCAspects.AER, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.END_BARRENS, TCAspects.AER, TCAspects.PERDITIO);
        addAspects(aspects, Biomes.THE_VOID, TCAspects.PERDITIO);

        addAspects(aspects, TCBiomes.MAGICAL_FOREST, TCAspects.ORDO, TCAspects.TERRA);
        addAspects(aspects, TCBiomes.EERIE, TCAspects.ORDO, TCAspects.IGNIS);
        addAspects(aspects, TCBiomes.ELDRITCH, TCAspects.ORDO, TCAspects.IGNIS, TCAspects.AER);
    }

    @SafeVarargs
    private static void addAspects(Builder<BiomeAspects, Biome> b, ResourceKey<Biome> key, ResourceKey<IAspect>... aspectKeys) {
        b.add(key, new BiomeAspects(List.of(aspectKeys)), false);
    }

    private static void add(Builder<BiomeAuraModifier, Biome> b, ResourceKey<Biome> key, float value) {
        b.add(key, new BiomeAuraModifier(value), false);
    }
}
