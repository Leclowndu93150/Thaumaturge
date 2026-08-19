package com.leclowndu93150.thaumaturge.data.worldgen.feature;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.world.objects.ConfigNodeSpawnFilter;
import com.leclowndu93150.thaumaturge.content.world.objects.ConfigRarityFilter;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public final class TCPlacedFeatures {
    private static final int MANA_POD_ATTEMPTS = 10;
    public static final ResourceKey<PlacedFeature> GREATWOOD_CHECKED = key("greatwood_checked");
    public static final ResourceKey<PlacedFeature> SILVERWOOD_CHECKED = key("silverwood_checked");
    public static final ResourceKey<PlacedFeature> BIG_MAGIC_CHECKED = key("big_magic_checked");
    public static final ResourceKey<PlacedFeature> TREES_MAGIC_FOREST = key("trees_magic_forest");
    public static final ResourceKey<PlacedFeature> GREATWOOD_NATURAL = key("greatwood_natural");
    public static final ResourceKey<PlacedFeature> GREATWOOD_NATURAL_RARE = key("greatwood_natural_rare");
    public static final ResourceKey<PlacedFeature> SILVERWOOD_NATURAL = key("silverwood_natural");
    public static final ResourceKey<PlacedFeature> MAGIC_FOREST_FLORA = key("magic_forest_flora");
    public static final ResourceKey<PlacedFeature> MANA_PODS = key("mana_pods");
    public static final ResourceKey<PlacedFeature> CRYSTALS = key("crystals");
    public static final ResourceKey<PlacedFeature> NODES_WILD = key("nodes_wild");
    public static final ResourceKey<PlacedFeature> NODES_MAGICAL = key("nodes_magical");
    public static final ResourceKey<PlacedFeature> NODES_EERIE = key("nodes_eerie");
    public static final ResourceKey<PlacedFeature> NODES_NETHER = key("nodes_nether");
    public static final ResourceKey<PlacedFeature> OBSIDIAN_TOTEM = key("obsidian_totem");
    public static final ResourceKey<PlacedFeature> CRIMSON_PORTAL = key("crimson_portal");
    public static final ResourceKey<PlacedFeature> HILLTOP_STONES = key("hilltop_stones");
    public static final ResourceKey<PlacedFeature> ORE_CINNABAR = key("ore_cinnabar");
    public static final ResourceKey<PlacedFeature> ORE_QUARTZ = key("ore_quartz");
    public static final ResourceKey<PlacedFeature> ORE_AMBER = key("ore_amber");
    public static final ResourceKey<PlacedFeature> CINDERPEARL = key("cinderpearl");

    private static final int MAGIC_FOREST_TREE_COUNT = 2;
    private static final float MAGIC_FOREST_EXTRA_TREE_CHANCE = 0.1F;
    private static final int MAGIC_FOREST_EXTRA_TREE_COUNT = 1;
    private static final int GREATWOOD_RARITY = 25;
    private static final int GREATWOOD_RARE_RARITY = 125;
    private static final int SILVERWOOD_RARITY = 80;
    private static final int CINDERPEARL_RARITY = 30;
    private static final int CINDERPEARL_TRIES = 18;
    private static final int CINDERPEARL_XZ_SPREAD = 8;
    private static final int OBSIDIAN_TOTEM_RARITY = 1440;
    private static final int HILLTOP_STONES_RARITY = 720;
    private static final int NODE_NETHER_MIN_Y = 32;
    private static final int NODE_NETHER_MAX_Y = 100;
    private static final int CINDERPEARL_Y_SPREAD = 4;
    private static final int ORE_CINNABAR_COUNT = 18;
    private static final int ORE_QUARTZ_COUNT = 18;
    private static final int ORE_AMBER_COUNT = 20;
    private static final int CINNABAR_MAX_Y = 12;
    private static final int QUARTZ_MAX_Y = 32;
    private static final int AMBER_MAX_DEPTH_BELOW_SURFACE = 16;

    private TCPlacedFeatures() {}

    private static ResourceKey<PlacedFeature> key(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, TCIds.rl(path));
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(
                GREATWOOD_CHECKED,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.GREATWOOD_TREE),
                        List.of(PlacementUtils.filteredByBlockSurvival(TCBlocks.SAPLING_GREATWOOD.get()))));
        context.register(
                SILVERWOOD_CHECKED,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.SILVERWOOD_TREE),
                        List.of(PlacementUtils.filteredByBlockSurvival(TCBlocks.SAPLING_SILVERWOOD.get()))));
        context.register(
                BIG_MAGIC_CHECKED,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.BIG_MAGIC_TREE),
                        List.of(PlacementUtils.filteredByBlockSurvival(TCBlocks.SAPLING_GREATWOOD.get()))));

        context.register(
                TREES_MAGIC_FOREST,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.MAGIC_FOREST_TREES),
                        List.of(
                                PlacementUtils.countExtra(
                                        MAGIC_FOREST_TREE_COUNT,
                                        MAGIC_FOREST_EXTRA_TREE_CHANCE,
                                        MAGIC_FOREST_EXTRA_TREE_COUNT),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP,
                                PlacementUtils.filteredByBlockSurvival(TCBlocks.SAPLING_GREATWOOD.get()),
                                BiomeFilter.biome())));

        context.register(
                GREATWOOD_NATURAL,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.GREATWOOD_TREE),
                        List.of(
                                RarityFilter.onAverageOnceEvery(GREATWOOD_RARITY),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                                PlacementUtils.filteredByBlockSurvival(TCBlocks.SAPLING_GREATWOOD.get()),
                                BiomeFilter.biome())));
        context.register(
                GREATWOOD_NATURAL_RARE,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.GREATWOOD_TREE),
                        List.of(
                                RarityFilter.onAverageOnceEvery(GREATWOOD_RARE_RARITY),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                                PlacementUtils.filteredByBlockSurvival(TCBlocks.SAPLING_GREATWOOD.get()),
                                BiomeFilter.biome())));
        context.register(
                SILVERWOOD_NATURAL,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.SILVERWOOD_TREE),
                        List.of(
                                RarityFilter.onAverageOnceEvery(SILVERWOOD_RARITY),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                                PlacementUtils.filteredByBlockSurvival(TCBlocks.SAPLING_SILVERWOOD.get()),
                                BiomeFilter.biome())));

        context.register(
                MAGIC_FOREST_FLORA,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.MAGIC_FOREST_FLORA), List.of(BiomeFilter.biome())));

        context.register(
                MANA_PODS,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.MANA_PODS),
                        List.of(
                                CountPlacement.of(MANA_POD_ATTEMPTS),
                                InSquarePlacement.spread(),
                                BiomeFilter.biome())));

        context.register(
                CRYSTALS,
                new PlacedFeature(configured.getOrThrow(TCConfiguredFeatures.CRYSTALS), List.of(BiomeFilter.biome())));

        context.register(
                NODES_WILD,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.NODES_WILD),
                        List.of(
                                ConfigNodeSpawnFilter.WILD,
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                BiomeFilter.biome())));
        context.register(
                NODES_MAGICAL,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.NODES_WILD),
                        List.of(
                                ConfigNodeSpawnFilter.MAGICAL,
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                BiomeFilter.biome())));
        context.register(
                NODES_EERIE,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.NODES_EERIE),
                        List.of(
                                ConfigNodeSpawnFilter.EERIE,
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                BiomeFilter.biome())));
        context.register(
                NODES_NETHER,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.NODES_WILD),
                        List.of(
                                ConfigNodeSpawnFilter.NETHER,
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(NODE_NETHER_MIN_Y),
                                        VerticalAnchor.absolute(NODE_NETHER_MAX_Y)),
                                BiomeFilter.biome())));

        context.register(
                OBSIDIAN_TOTEM,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.OBSIDIAN_TOTEM),
                        List.of(
                                RarityFilter.onAverageOnceEvery(OBSIDIAN_TOTEM_RARITY), InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                        BiomeFilter.biome())));
        context.register(
                CRIMSON_PORTAL,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.CRIMSON_PORTAL),
                        List.of(
                                ConfigRarityFilter.CRIMSON_PORTAL,
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                BiomeFilter.biome())));
        context.register(
                HILLTOP_STONES,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.HILLTOP_STONES),
                        List.of(
                                RarityFilter.onAverageOnceEvery(HILLTOP_STONES_RARITY), InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                        BiomeFilter.biome())));

        context.register(
                ORE_CINNABAR,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.ORE_CINNABAR),
                        List.of(
                                CountPlacement.of(ORE_CINNABAR_COUNT),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.bottom(), VerticalAnchor.absolute(CINNABAR_MAX_Y)),
                                BiomeFilter.biome())));
        context.register(
                ORE_QUARTZ,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.ORE_QUARTZ),
                        List.of(
                                CountPlacement.of(ORE_QUARTZ_COUNT),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.bottom(), VerticalAnchor.absolute(QUARTZ_MAX_Y)),
                                BiomeFilter.biome())));
        context.register(
                ORE_AMBER,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.ORE_AMBER),
                        List.of(
                                CountPlacement.of(ORE_AMBER_COUNT),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP,
                                RandomOffsetPlacement.vertical(UniformInt.of(-AMBER_MAX_DEPTH_BELOW_SURFACE, 0)),
                                BiomeFilter.biome())));

        context.register(
                CINDERPEARL,
                new PlacedFeature(
                        configured.getOrThrow(TCConfiguredFeatures.CINDERPEARL_PATCH),
                        List.of(
                                RarityFilter.onAverageOnceEvery(CINDERPEARL_RARITY),
                                InSquarePlacement.spread(),
                                PlacementUtils.HEIGHTMAP,
                                BiomeFilter.biome(),
                                CountPlacement.of(CINDERPEARL_TRIES),
                                RandomOffsetPlacement.of(
                                        UniformInt.of(-CINDERPEARL_XZ_SPREAD, CINDERPEARL_XZ_SPREAD),
                                        UniformInt.of(-CINDERPEARL_Y_SPREAD, CINDERPEARL_Y_SPREAD)),
                                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE))));
    }
}
