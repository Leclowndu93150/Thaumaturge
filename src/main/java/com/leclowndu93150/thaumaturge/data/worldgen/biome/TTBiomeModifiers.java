package com.leclowndu93150.thaumaturge.data.worldgen.biome;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TTPlacedFeatures;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TTBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_ORES = key("add_ores");
    public static final ResourceKey<BiomeModifier> ADD_CRYSTALS = key("add_crystals");
    public static final ResourceKey<BiomeModifier> ADD_WILD_NODES = key("add_wild_nodes");
    public static final ResourceKey<BiomeModifier> ADD_MAGICAL_NODES = key("add_magical_nodes");
    public static final ResourceKey<BiomeModifier> ADD_EERIE_NODES = key("add_eerie_nodes");
    public static final ResourceKey<BiomeModifier> ADD_NETHER_NODES = key("add_nether_nodes");
    public static final ResourceKey<BiomeModifier> ADD_OBSIDIAN_TOTEMS = key("add_obsidian_totems");
    public static final ResourceKey<BiomeModifier> ADD_CRIMSON_PORTALS = key("add_crimson_portals");
    public static final ResourceKey<BiomeModifier> ADD_HILLTOP_STONES = key("add_hilltop_stones");
    public static final ResourceKey<BiomeModifier> ADD_GREATWOOD = key("add_greatwood");
    public static final ResourceKey<BiomeModifier> ADD_GREATWOOD_RARE = key("add_greatwood_rare");
    public static final ResourceKey<BiomeModifier> ADD_SILVERWOOD = key("add_silverwood");
    public static final ResourceKey<BiomeModifier> ADD_CINDERPEARL = key("add_cinderpearl");
    public static final ResourceKey<BiomeModifier> ADD_NETHER_WISPS = key("add_nether_wisps");
    public static final ResourceKey<BiomeModifier> ADD_NETHER_FIREBATS = key("add_nether_firebats");
    public static final ResourceKey<BiomeModifier> ADD_OVERWORLD_BRAINY_ZOMBIES = key("add_overworld_brainy_zombies");
    public static final ResourceKey<BiomeModifier> ADD_DESERT_BRAINY_HUSKS = key("add_desert_brainy_husks");
    public static final ResourceKey<BiomeModifier> ADD_PECHS = key("add_pechs");

    private static final int NETHER_WISP_WEIGHT = 5;
    private static final int NETHER_FIREBAT_WEIGHT = 10;
    private static final int OVERWORLD_BRAINY_ZOMBIE_WEIGHT = 10;
    private static final int DESERT_BRAINY_HUSK_WEIGHT = 2;
    private static final int PECH_WEIGHT = 10;

    private TTBiomeModifiers() {}

    private static ResourceKey<BiomeModifier> key(String path) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, TTIds.rl(path));
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);

        context.register(
                ADD_ORES,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(
                                features.getOrThrow(TTPlacedFeatures.ORE_CINNABAR),
                                features.getOrThrow(TTPlacedFeatures.ORE_QUARTZ),
                                features.getOrThrow(TTPlacedFeatures.ORE_AMBER)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(
                ADD_CRYSTALS,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.CRYSTALS)),
                        GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(
                ADD_WILD_NODES,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.NODES_WILD)),
                        GenerationStep.Decoration.TOP_LAYER_MODIFICATION));

        context.register(
                ADD_MAGICAL_NODES,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(biomes.getOrThrow(TTBiomes.MAGICAL_FOREST)),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.NODES_MAGICAL)),
                        GenerationStep.Decoration.TOP_LAYER_MODIFICATION));

        context.register(
                ADD_EERIE_NODES,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(biomes.getOrThrow(TTBiomes.EERIE)),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.NODES_EERIE)),
                        GenerationStep.Decoration.TOP_LAYER_MODIFICATION));

        context.register(
                ADD_NETHER_NODES,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_NETHER),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.NODES_NETHER)),
                        GenerationStep.Decoration.TOP_LAYER_MODIFICATION));

        context.register(
                ADD_OBSIDIAN_TOTEMS,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.OBSIDIAN_TOTEM)),
                        GenerationStep.Decoration.SURFACE_STRUCTURES));

        context.register(
                ADD_CRIMSON_PORTALS,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.CRIMSON_PORTAL)),
                        GenerationStep.Decoration.SURFACE_STRUCTURES));

        context.register(
                ADD_HILLTOP_STONES,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.HILLTOP_STONES)),
                        GenerationStep.Decoration.SURFACE_STRUCTURES));

        context.register(
                ADD_GREATWOOD,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(TTBiomeTags.HAS_GREATWOOD),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.GREATWOOD_NATURAL)),
                        GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(
                ADD_GREATWOOD_RARE,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(TTBiomeTags.HAS_GREATWOOD_RARE),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.GREATWOOD_NATURAL_RARE)),
                        GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(
                ADD_SILVERWOOD,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(TTBiomeTags.HAS_SILVERWOOD),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.SILVERWOOD_NATURAL)),
                        GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(
                ADD_CINDERPEARL,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(TTBiomeTags.HAS_CINDERPEARL),
                        HolderSet.direct(features.getOrThrow(TTPlacedFeatures.CINDERPEARL)),
                        GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(
                ADD_NETHER_WISPS,
                new BiomeModifiers.AddSpawnsBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_NETHER),
                        List.of(new MobSpawnSettings.SpawnerData(TTEntities.WISP.get(), NETHER_WISP_WEIGHT, 1, 1))));

        context.register(
                ADD_NETHER_FIREBATS,
                new BiomeModifiers.AddSpawnsBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_NETHER),
                        List.of(new MobSpawnSettings.SpawnerData(
                                TTEntities.FIRE_BAT.get(), NETHER_FIREBAT_WEIGHT, 1, 2))));

        context.register(
                ADD_OVERWORLD_BRAINY_ZOMBIES,
                new BiomeModifiers.AddSpawnsBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        List.of(new MobSpawnSettings.SpawnerData(
                                TTEntities.BRAINY_ZOMBIE.get(), OVERWORLD_BRAINY_ZOMBIE_WEIGHT, 1, 1))));

        context.register(
                ADD_DESERT_BRAINY_HUSKS,
                new BiomeModifiers.AddSpawnsBiomeModifier(
                        biomes.getOrThrow(TTBiomeTags.HAS_BRAINY_HUSK),
                        List.of(new MobSpawnSettings.SpawnerData(
                                TTEntities.BRAINY_HUSK.get(), DESERT_BRAINY_HUSK_WEIGHT, 1, 1))));

        context.register(
                ADD_PECHS,
                new BiomeModifiers.AddSpawnsBiomeModifier(
                        biomes.getOrThrow(TTBiomeTags.HAS_PECH),
                        List.of(new MobSpawnSettings.SpawnerData(TTEntities.PECH.get(), PECH_WEIGHT, 1, 1))));
    }
}
