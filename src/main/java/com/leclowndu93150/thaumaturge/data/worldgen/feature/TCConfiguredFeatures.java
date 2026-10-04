package com.leclowndu93150.thaumaturge.data.worldgen.feature;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeFeatureConfig;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalClusterConfig;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicForestFloraConfig;
import com.leclowndu93150.thaumaturge.content.world.taint.TaintBiomeConfig;
import com.leclowndu93150.thaumaturge.content.world.tree.TCTreeGrowers;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownFoliagePlacer;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownRule;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownShape;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownTrunkPlacer;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.SpiderNestDecorator;
import com.leclowndu93150.thaumaturge.content.world.tree.silverwood.DetachedLeafPruner;
import com.leclowndu93150.thaumaturge.content.world.tree.silverwood.LeafCellFoliagePlacer;
import com.leclowndu93150.thaumaturge.content.world.tree.silverwood.ScatteredFlowersDecorator;
import com.leclowndu93150.thaumaturge.content.world.tree.silverwood.SilverwoodTrunkPlacer;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCFeatures;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public final class TCConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGIC_FOREST_BROWN_MUSHROOM = key("magic_forest_brown_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGIC_FOREST_RED_MUSHROOM = key("magic_forest_red_mushroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE = key("greatwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE_GROWN = TCTreeGrowers.GREATWOOD_TREE_GROWN;
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE = key("silverwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE_GROWN = TCTreeGrowers.SILVERWOOD_TREE_GROWN;
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE_CAVE = key("silverwood_tree_cave");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIG_MAGIC_TREE = key("big_magic_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGIC_FOREST_TREES = key("magic_forest_trees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TAINTED_LANDS_TREES = key("tainted_lands_trees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TAINT_BIOME = key("taint_biome");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGIC_FOREST_FLORA = key("magic_forest_flora");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_GRASS = key("magical_cave_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_AMBIENT_GRASS = key("magical_cave_ambient_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_POND = key("magical_cave_pond");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_TREES = key("magical_cave_trees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_GREATWOOD_TREE = key("magical_cave_greatwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_BUSH = key("magical_cave_bush");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_MUSHROOMS = key("magical_cave_mushrooms");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_FLORA = key("magical_cave_flora");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_VISHROOM = key("magical_cave_vishroom");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_SHIMMERLEAF = key("magical_cave_shimmerleaf");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGICAL_CAVE_CRYSTALS = key("magical_cave_crystals");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MANA_PODS = key("mana_pods");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYSTALS = key("crystals");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NODES_WILD = key("nodes_wild");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NODES_EERIE = key("nodes_eerie");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OBSIDIAN_TOTEM = key("obsidian_totem");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRIMSON_PORTAL = key("crimson_portal");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HILLTOP_STONES = key("hilltop_stones");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_CINNABAR = key("ore_cinnabar");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_QUARTZ = key("ore_quartz");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_AMBER = key("ore_amber");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CINDERPEARL_PATCH = key("cinderpearl_patch");

    private static final int CROWN_TREE_BASE_HEIGHT = 11;
    private static final int CROWN_TREE_HEIGHT_SPREAD = 10;
    private static final int GREATWOOD_TRUNK_WIDTH = 2;
    private static final double GREATWOOD_HEIGHT_ATTENUATION = 0.618;
    private static final double GREATWOOD_BRANCH_SLOPE = 0.38;
    private static final double GREATWOOD_SCALE_WIDTH = 1.2;
    private static final float GREATWOOD_SPIDER_CHANCE = 0.125F;
    private static final double MAGIC_OAK_TRUNK_SHARE = 0.6618;
    private static final double MAGIC_OAK_BRANCH_SLOPE = 0.381;
    private static final double MAGIC_OAK_CROWN_WIDTH = 1.25;
    private static final int SILVERWOOD_NATURAL_MIN_HEIGHT = 8;
    private static final int SILVERWOOD_NATURAL_EXTRA_HEIGHT = 5;
    private static final int SILVERWOOD_GROWN_MIN_HEIGHT = 7;
    private static final int SILVERWOOD_GROWN_EXTRA_HEIGHT = 4;
    private static final float MAGIC_FOREST_SILVERWOOD_CHANCE = 1.0F / 18.0F;
    private static final float MAGIC_FOREST_GREATWOOD_CHANCE = 1.0F / 12.0F;
    private static final float TAINTED_LANDS_BIG_TREE_CHANCE = 1.0F / 8.0F;
    private static final float MAGICAL_CAVE_OAK_TREE_CHANCE = 0.70F;
    private static final float MAGICAL_CAVE_SILVERWOOD_CHANCE = 1.3F / 12.0F;
    private static final float MAGICAL_CAVE_GREATWOOD_CHANCE = MAGICAL_CAVE_SILVERWOOD_CHANCE;
    private static final float MAGICAL_CAVE_BROWN_MUSHROOM_CHANCE = 0.03F;
    private static final float MAGICAL_CAVE_RED_MUSHROOM_CHANCE = 0.025F;
    private static final float MAGICAL_CAVE_FLOWER_CHANCE = 0.12F;
    private static final int MAGICAL_CAVE_SILVERWOOD_BASE_HEIGHT = 6;
    private static final int MAGICAL_CAVE_SILVERWOOD_EXTRA_HEIGHT = 3;
    private static final int MAGICAL_CAVE_GREATWOOD_BASE_HEIGHT = 4;
    private static final int MAGICAL_CAVE_GREATWOOD_EXTRA_HEIGHT = 2;
    private static final int MAGICAL_CAVE_GREATWOOD_FOLIAGE_RADIUS = 2;
    private static final int MAGICAL_CAVE_GREATWOOD_FOLIAGE_HEIGHT = 3;
    private static final int MAGICAL_CAVE_CRYSTAL_ATTEMPTS = 1;
    private static final int MAGICAL_CAVE_CRYSTAL_MAX_TOTAL = 8;
    private static final int CAVE_GROUND_MIN_DEPTH = 1;
    private static final int CAVE_GROUND_MAX_DEPTH = 2;
    private static final int CAVE_GROUND_VERTICAL_RANGE = 5;
    private static final int CAVE_GROUND_MIN_RADIUS = 3;
    private static final int CAVE_GROUND_MAX_RADIUS = 6;
    private static final float CAVE_GROUND_EXTRA_EDGE_CHANCE = 0.3F;
    private static final int PATCH_XZ_SPREAD = 7;
    private static final int PATCH_Y_SPREAD = 3;
    private static final int MUSHROOM_PATCH_TRIES = 96;
    private static final int GRASS_PATCH_TRIES = 32;
    private static final int FLOWER_PATCH_TRIES = 64;
    private static final int TAINT_BIOME_MAX_CRUST_BLOBS = 2;
    private static final int TAINT_BIOME_MIN_CRUST_RADIUS = 1;
    private static final int TAINT_BIOME_MAX_CRUST_RADIUS = 2;
    private static final int TAINT_BIOME_GRASS_FIBRE_ATTEMPTS = 10;
    private static final int TAINT_BIOME_GENERAL_FIBRE_ATTEMPTS = 8;
    private static final int TAINT_BIOME_GROUND_SEARCH_DEPTH = 32;
    private static final int TAINT_BIOME_LANDMARK_RADIUS_CHUNKS = 4;
    private static final int TAINT_BIOME_LANDMARK_ATTEMPTS = 48;
    private static final int CRYSTAL_ATTEMPTS = 8;
    private static final int CRYSTAL_MAX_TOTAL = 64;
    private static final int CRYSTAL_BIOME_ASPECT_CHANCE = 3;
    private static final int FLORA_GRASS_ATTEMPTS = 3;
    private static final int FLORA_VISHROOM_ATTEMPTS = 5;

    private TCConfiguredFeatures() {}

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, TCIds.rl(path));
    }

    private static VegetationPatchConfiguration caveGround(Block ground) {
        return new VegetationPatchConfiguration(TCBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE, BlockStateProvider.simple(ground),
                PlacementUtils.inlinePlaced(Feature.NO_OP, NoneFeatureConfiguration.INSTANCE), CaveSurface.FLOOR, UniformInt.of(CAVE_GROUND_MIN_DEPTH, CAVE_GROUND_MAX_DEPTH), 0.0F,
                CAVE_GROUND_VERTICAL_RANGE, 0.0F, UniformInt.of(CAVE_GROUND_MIN_RADIUS, CAVE_GROUND_MAX_RADIUS), CAVE_GROUND_EXTRA_EDGE_CHANCE);
    }

    private static TreeConfiguration crownTree(Block log, Block leaves, CrownShape shape, CrownRule rule, boolean absorbForeignLeaves, List<TreeDecorator> decorators) {
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(log), new CrownTrunkPlacer(CROWN_TREE_BASE_HEIGHT, CROWN_TREE_HEIGHT_SPREAD, 0, shape, rule),
                BlockStateProvider.simple(leaves), new CrownFoliagePlacer(ConstantInt.ZERO, ConstantInt.ZERO, absorbForeignLeaves), new TwoLayersFeatureSize(1, 0, 0)).ignoreVines()
                .decorators(decorators).build();
    }

    private static TreeConfiguration silverwoodTree(int minHeight, int extraHeight, boolean growNodes, boolean keepApart, Optional<Block> flower) {
        List<TreeDecorator> decorators = new ArrayList<>();
        decorators.add(DetachedLeafPruner.INSTANCE);
        flower.ifPresent(block -> decorators.add(new ScatteredFlowersDecorator(block)));
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(TCBlocks.LOG_SILVERWOOD.get()), new SilverwoodTrunkPlacer(minHeight, extraHeight - 1, 0, growNodes, keepApart),
                BlockStateProvider.simple(TCBlocks.LEAVES_SILVERWOOD.get()), new LeafCellFoliagePlacer(ConstantInt.ZERO, ConstantInt.ZERO), new TwoLayersFeatureSize(1, 0, 0)).ignoreVines()
                .decorators(List.copyOf(decorators)).build();
    }

    private static Holder<PlacedFeature> patch(Holder<ConfiguredFeature<?, ?>> feature, int tries) {
        return PlacementUtils.inlinePlaced(feature, CountPlacement.of(tries), RandomOffsetPlacement.ofTriangle(PATCH_XZ_SPREAD, PATCH_Y_SPREAD),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);

        CrownShape greatwoodShape = new CrownShape(GREATWOOD_TRUNK_WIDTH, GREATWOOD_HEIGHT_ATTENUATION, GREATWOOD_BRANCH_SLOPE, GREATWOOD_SCALE_WIDTH, true);
        List<TreeDecorator> greatwoodNest = List.of(new SpiderNestDecorator(GREATWOOD_SPIDER_CHANCE, TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LEAVES_GREATWOOD.get()));
        context.register(GREATWOOD_TREE,
                new ConfiguredFeature<>(Feature.TREE, crownTree(TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LEAVES_GREATWOOD.get(), greatwoodShape, CrownRule.OPEN_AIR, false, greatwoodNest)));
        context.register(GREATWOOD_TREE_GROWN,
                new ConfiguredFeature<>(Feature.TREE, crownTree(TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LEAVES_GREATWOOD.get(), greatwoodShape, CrownRule.OPEN_AIR, false, List.of())));
        CrownShape magicOakShape = new CrownShape(1, MAGIC_OAK_TRUNK_SHARE, MAGIC_OAK_BRANCH_SLOPE, MAGIC_OAK_CROWN_WIDTH, false);
        context.register(BIG_MAGIC_TREE, new ConfiguredFeature<>(Feature.TREE, crownTree(Blocks.OAK_LOG, Blocks.OAK_LEAVES, magicOakShape, CrownRule.REPLACEABLE, true, List.of())));
        context.register(SILVERWOOD_TREE,
                new ConfiguredFeature<>(Feature.TREE, silverwoodTree(SILVERWOOD_NATURAL_MIN_HEIGHT, SILVERWOOD_NATURAL_EXTRA_HEIGHT, true, true, Optional.of(TCBlocks.PLANT_SHIMMERLEAF.get()))));
        context.register(SILVERWOOD_TREE_GROWN, new ConfiguredFeature<>(Feature.TREE, silverwoodTree(SILVERWOOD_GROWN_MIN_HEIGHT, SILVERWOOD_GROWN_EXTRA_HEIGHT, true, false, Optional.empty())));

        context.register(SILVERWOOD_TREE_CAVE, new ConfiguredFeature<>(Feature.TREE,
                silverwoodTree(MAGICAL_CAVE_SILVERWOOD_BASE_HEIGHT, MAGICAL_CAVE_SILVERWOOD_EXTRA_HEIGHT, false, false, Optional.of(TCBlocks.PLANT_SHIMMERLEAF.get()))));
        context.register(MAGICAL_CAVE_GREATWOOD_TREE, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(TCBlocks.LOG_GREATWOOD.get()),
                        new StraightTrunkPlacer(MAGICAL_CAVE_GREATWOOD_BASE_HEIGHT, MAGICAL_CAVE_GREATWOOD_EXTRA_HEIGHT, 0), BlockStateProvider.simple(TCBlocks.LEAVES_GREATWOOD.get()),
                        new BlobFoliagePlacer(ConstantInt.of(MAGICAL_CAVE_GREATWOOD_FOLIAGE_RADIUS), ConstantInt.of(0), MAGICAL_CAVE_GREATWOOD_FOLIAGE_HEIGHT), new TwoLayersFeatureSize(1, 0, 1))
                        .build()));

        context.register(MAGIC_FOREST_TREES,
                new ConfiguredFeature<>(Feature.RANDOM_SELECTOR,
                        new RandomFeatureConfiguration(
                                List.of(new WeightedPlacedFeature(placed.getOrThrow(TCPlacedFeatures.SILVERWOOD_CHECKED), MAGIC_FOREST_SILVERWOOD_CHANCE),
                                        new WeightedPlacedFeature(placed.getOrThrow(TCPlacedFeatures.GREATWOOD_CHECKED), MAGIC_FOREST_GREATWOOD_CHANCE)),
                                placed.getOrThrow(TCPlacedFeatures.BIG_MAGIC_CHECKED))));

        context.register(TAINTED_LANDS_TREES, new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(
                List.of(new WeightedPlacedFeature(placed.getOrThrow(TCPlacedFeatures.BIG_MAGIC_CHECKED), TAINTED_LANDS_BIG_TREE_CHANCE)), placed.getOrThrow(TreePlacements.OAK_CHECKED))));

        context.register(TAINT_BIOME,
                new ConfiguredFeature<>(TCFeatures.TAINT_BIOME.get(),
                        new TaintBiomeConfig(TCBlocks.TAINT_CRUST.get(), TAINT_BIOME_MAX_CRUST_BLOBS, UniformInt.of(TAINT_BIOME_MIN_CRUST_RADIUS, TAINT_BIOME_MAX_CRUST_RADIUS),
                                TAINT_BIOME_GRASS_FIBRE_ATTEMPTS, TAINT_BIOME_GENERAL_FIBRE_ATTEMPTS, TAINT_BIOME_GROUND_SEARCH_DEPTH, true, TAINT_BIOME_LANDMARK_RADIUS_CHUNKS,
                                TAINT_BIOME_LANDMARK_ATTEMPTS)));

        context.register(MAGIC_FOREST_BROWN_MUSHROOM,
                new ConfiguredFeature<>(Feature.HUGE_BROWN_MUSHROOM,
                        new HugeMushroomFeatureConfiguration(BlockStateProvider.simple(Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState().setValue(HugeMushroomBlock.DOWN, false)),
                                BlockStateProvider.simple(Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false).setValue(HugeMushroomBlock.DOWN, false)), 3,
                                BlockPredicate.matchesTag(BlockTags.HUGE_BROWN_MUSHROOM_CAN_PLACE_ON))));
        context.register(MAGIC_FOREST_RED_MUSHROOM,
                new ConfiguredFeature<>(Feature.HUGE_RED_MUSHROOM,
                        new HugeMushroomFeatureConfiguration(BlockStateProvider.simple(Blocks.RED_MUSHROOM_BLOCK.defaultBlockState().setValue(HugeMushroomBlock.DOWN, false)),
                                BlockStateProvider.simple(Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false).setValue(HugeMushroomBlock.DOWN, false)), 3,
                                BlockPredicate.matchesTag(BlockTags.HUGE_RED_MUSHROOM_CAN_PLACE_ON))));
        context.register(MAGIC_FOREST_FLORA,
                new ConfiguredFeature<>(TCFeatures.MAGIC_FOREST_FLORA.get(),
                        new MagicForestFloraConfig(TCBlocks.GRASS_AMBIENT.get(), TCBlocks.PLANT_VISHROOM.get(), FLORA_GRASS_ATTEMPTS, FLORA_VISHROOM_ATTEMPTS,
                                context.lookup(Registries.BLOCK).getOrThrow(TCBlockTags.MAGICAL_FOREST_FLOWERS), 10,
                                List.of(new MagicForestFloraConfig.PlantPatch(BlockStateProvider.simple(Blocks.TALL_GRASS), 12, 1),
                                        new MagicForestFloraConfig.PlantPatch(BlockStateProvider.simple(Blocks.SHORT_GRASS), 10, 1),
                                        new MagicForestFloraConfig.PlantPatch(BlockStateProvider.simple(Blocks.FERN), 6, 1),
                                        new MagicForestFloraConfig.PlantPatch(BlockStateProvider.simple(Blocks.BROWN_MUSHROOM), 6, 4),
                                        new MagicForestFloraConfig.PlantPatch(BlockStateProvider.simple(Blocks.RED_MUSHROOM), 6, 8)),
                                HolderSet.direct(configured.getOrThrow(MAGIC_FOREST_BROWN_MUSHROOM), configured.getOrThrow(MAGIC_FOREST_RED_MUSHROOM)), 40)));

        context.register(MAGICAL_CAVE_GRASS, new ConfiguredFeature<>(Feature.VEGETATION_PATCH, caveGround(Blocks.GRASS_BLOCK)));
        context.register(MAGICAL_CAVE_AMBIENT_GRASS, new ConfiguredFeature<>(Feature.VEGETATION_PATCH, caveGround(TCBlocks.GRASS_AMBIENT.get())));
        context.register(MAGICAL_CAVE_POND, new ConfiguredFeature<>(TCFeatures.MAGICAL_CAVE_POND.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(MAGICAL_CAVE_MUSHROOMS,
                new ConfiguredFeature<>(Feature.RANDOM_BOOLEAN_SELECTOR, new RandomBooleanFeatureConfiguration(PlacementUtils.inlinePlaced(configured.getOrThrow(TreeFeatures.HUGE_BROWN_MUSHROOM)),
                        PlacementUtils.inlinePlaced(configured.getOrThrow(TreeFeatures.HUGE_RED_MUSHROOM)))));
        context.register(MAGICAL_CAVE_TREES,
                new ConfiguredFeature<>(Feature.RANDOM_SELECTOR,
                        new RandomFeatureConfiguration(
                                List.of(new WeightedPlacedFeature(PlacementUtils.inlinePlaced(configured.getOrThrow(SILVERWOOD_TREE_CAVE)), MAGICAL_CAVE_SILVERWOOD_CHANCE),
                                        new WeightedPlacedFeature(PlacementUtils.inlinePlaced(configured.getOrThrow(MAGICAL_CAVE_GREATWOOD_TREE)), MAGICAL_CAVE_GREATWOOD_CHANCE),
                                        new WeightedPlacedFeature(PlacementUtils.inlinePlaced(configured.getOrThrow(TreeFeatures.OAK)), MAGICAL_CAVE_OAK_TREE_CHANCE)),
                                PlacementUtils.inlinePlaced(configured.getOrThrow(MAGICAL_CAVE_BUSH)))));
        context.register(MAGICAL_CAVE_BUSH, new ConfiguredFeature<>(TCFeatures.MAGICAL_CAVE_BUSH.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(MAGICAL_CAVE_FLORA,
                new ConfiguredFeature<>(Feature.RANDOM_SELECTOR,
                        new RandomFeatureConfiguration(
                                List.of(new WeightedPlacedFeature(patch(configured.getOrThrow(VegetationFeatures.BROWN_MUSHROOM), MUSHROOM_PATCH_TRIES), MAGICAL_CAVE_BROWN_MUSHROOM_CHANCE),
                                        new WeightedPlacedFeature(patch(configured.getOrThrow(VegetationFeatures.RED_MUSHROOM), MUSHROOM_PATCH_TRIES), MAGICAL_CAVE_RED_MUSHROOM_CHANCE),
                                        new WeightedPlacedFeature(patch(configured.getOrThrow(VegetationFeatures.FLOWER_DEFAULT), FLOWER_PATCH_TRIES), MAGICAL_CAVE_FLOWER_CHANCE)),
                                patch(configured.getOrThrow(VegetationFeatures.GRASS), GRASS_PATCH_TRIES))));
        context.register(MAGICAL_CAVE_VISHROOM, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(TCBlocks.PLANT_VISHROOM.get()))));
        context.register(MAGICAL_CAVE_SHIMMERLEAF, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(TCBlocks.PLANT_SHIMMERLEAF.get()))));

        context.register(MANA_PODS, new ConfiguredFeature<>(TCFeatures.MANA_PODS.get(), NoneFeatureConfiguration.INSTANCE));

        context.register(NODES_WILD, new ConfiguredFeature<>(TCFeatures.NODE.get(), new NodeFeatureConfig(false, false, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA)));
        context.register(NODES_EERIE, new ConfiguredFeature<>(TCFeatures.NODE.get(), new NodeFeatureConfig(false, true, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA)));
        context.register(OBSIDIAN_TOTEM, new ConfiguredFeature<>(TCFeatures.OBSIDIAN_TOTEM.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(CRIMSON_PORTAL, new ConfiguredFeature<>(TCFeatures.CRIMSON_PORTAL.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(HILLTOP_STONES, new ConfiguredFeature<>(TCFeatures.HILLTOP_STONES.get(), NoneFeatureConfiguration.INSTANCE));

        List<CrystalClusterConfig.Entry> crystals = List.of(new CrystalClusterConfig.Entry(TCAspects.AER, TCBlocks.CRYSTAL_AER.get()),
                new CrystalClusterConfig.Entry(TCAspects.IGNIS, TCBlocks.CRYSTAL_IGNIS.get()), new CrystalClusterConfig.Entry(TCAspects.AQUA, TCBlocks.CRYSTAL_AQUA.get()),
                new CrystalClusterConfig.Entry(TCAspects.TERRA, TCBlocks.CRYSTAL_TERRA.get()), new CrystalClusterConfig.Entry(TCAspects.ORDO, TCBlocks.CRYSTAL_ORDO.get()),
                new CrystalClusterConfig.Entry(TCAspects.PERDITIO, TCBlocks.CRYSTAL_PERDITIO.get()));
        context.register(CRYSTALS, new ConfiguredFeature<>(TCFeatures.CRYSTAL_CLUSTER.get(), new CrystalClusterConfig(crystals, CRYSTAL_ATTEMPTS, CRYSTAL_MAX_TOTAL, CRYSTAL_BIOME_ASPECT_CHANCE)));
        context.register(MAGICAL_CAVE_CRYSTALS, new ConfiguredFeature<>(TCFeatures.CRYSTAL_CLUSTER.get(),
                new CrystalClusterConfig(crystals, MAGICAL_CAVE_CRYSTAL_ATTEMPTS, MAGICAL_CAVE_CRYSTAL_MAX_TOTAL, CRYSTAL_BIOME_ASPECT_CHANCE, true)));

        TagMatchTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        TagMatchTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        context.register(ORE_CINNABAR, new ConfiguredFeature<>(Feature.REPLACE_SINGLE_BLOCK, new ReplaceBlockConfiguration(List
                .of(OreConfiguration.target(stone, TCBlocks.ORE_CINNABAR.get().defaultBlockState()), OreConfiguration.target(deepslate, TCBlocks.DEEPSLATE_ORE_CINNABAR.get().defaultBlockState())))));
        context.register(ORE_QUARTZ, new ConfiguredFeature<>(Feature.REPLACE_SINGLE_BLOCK, new ReplaceBlockConfiguration(
                List.of(OreConfiguration.target(stone, TCBlocks.ORE_QUARTZ.get().defaultBlockState()), OreConfiguration.target(deepslate, TCBlocks.DEEPSLATE_ORE_QUARTZ.get().defaultBlockState())))));
        context.register(ORE_AMBER, new ConfiguredFeature<>(Feature.REPLACE_SINGLE_BLOCK, new ReplaceBlockConfiguration(
                List.of(OreConfiguration.target(stone, TCBlocks.ORE_AMBER.get().defaultBlockState()), OreConfiguration.target(deepslate, TCBlocks.DEEPSLATE_ORE_AMBER.get().defaultBlockState())))));

        context.register(CINDERPEARL_PATCH, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(TCBlocks.PLANT_CINDERPEARL.get()))));
    }
}
