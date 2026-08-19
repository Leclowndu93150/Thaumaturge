package com.leclowndu93150.thaumaturge.data.worldgen.feature;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeFeatureConfig;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalClusterConfig;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicForestFloraConfig;
import com.leclowndu93150.thaumaturge.content.world.tree.BigMagicTreeConfig;
import com.leclowndu93150.thaumaturge.content.world.tree.BigTreeConfig;
import com.leclowndu93150.thaumaturge.content.world.tree.SilverwoodTreeConfig;
import com.leclowndu93150.thaumaturge.content.world.tree.TCTreeGrowers;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCFeatures;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public final class TCConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE = key("greatwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE_GROWN = TCTreeGrowers.GREATWOOD_TREE_GROWN;
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE = key("silverwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE_GROWN = TCTreeGrowers.SILVERWOOD_TREE_GROWN;
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIG_MAGIC_TREE = key("big_magic_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGIC_FOREST_TREES = key("magic_forest_trees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAGIC_FOREST_FLORA = key("magic_forest_flora");
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

    private static final double GREATWOOD_HEIGHT_ATTENUATION = 0.618;
    private static final double GREATWOOD_BRANCH_SLOPE = 0.38;
    private static final double GREATWOOD_SCALE_WIDTH = 1.2;
    private static final float GREATWOOD_SPIDER_CHANCE = 0.125F;
    private static final int SILVERWOOD_NATURAL_MIN_HEIGHT = 8;
    private static final int SILVERWOOD_NATURAL_EXTRA_HEIGHT = 5;
    private static final int SILVERWOOD_GROWN_MIN_HEIGHT = 7;
    private static final int SILVERWOOD_GROWN_EXTRA_HEIGHT = 4;
    private static final float MAGIC_FOREST_SILVERWOOD_CHANCE = 1.0F / 18.0F;
    private static final float MAGIC_FOREST_GREATWOOD_CHANCE = 1.0F / 12.0F;
    private static final int CRYSTAL_ATTEMPTS = 8;
    private static final int CRYSTAL_MAX_TOTAL = 64;
    private static final int CRYSTAL_BIOME_ASPECT_CHANCE = 3;
    private static final int FLORA_GRASS_ATTEMPTS = 3;
    private static final int FLORA_VISHROOM_ATTEMPTS = 8;

    private TCConfiguredFeatures() {}

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, TCIds.rl(path));
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);

        BigTreeConfig greatwoodNatural = new BigTreeConfig(TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LEAVES_GREATWOOD.get(), 2, GREATWOOD_HEIGHT_ATTENUATION, GREATWOOD_BRANCH_SLOPE,
                GREATWOOD_SCALE_WIDTH, true, GREATWOOD_SPIDER_CHANCE);
        BigTreeConfig greatwoodGrown = new BigTreeConfig(TCBlocks.LOG_GREATWOOD.get(), TCBlocks.LEAVES_GREATWOOD.get(), 2, GREATWOOD_HEIGHT_ATTENUATION, GREATWOOD_BRANCH_SLOPE, GREATWOOD_SCALE_WIDTH,
                true, 0.0F);
        context.register(GREATWOOD_TREE, new ConfiguredFeature<>(TCFeatures.BIG_TREE.get(), greatwoodNatural));
        context.register(GREATWOOD_TREE_GROWN, new ConfiguredFeature<>(TCFeatures.BIG_TREE.get(), greatwoodGrown));
        context.register(BIG_MAGIC_TREE, new ConfiguredFeature<>(TCFeatures.BIG_MAGIC_TREE.get(), new BigMagicTreeConfig(Blocks.OAK_LOG, Blocks.OAK_LEAVES)));
        context.register(SILVERWOOD_TREE, new ConfiguredFeature<>(TCFeatures.SILVERWOOD_TREE.get(), new SilverwoodTreeConfig(TCBlocks.LOG_SILVERWOOD.get(), TCBlocks.LEAVES_SILVERWOOD.get(),
                SILVERWOOD_NATURAL_MIN_HEIGHT, SILVERWOOD_NATURAL_EXTRA_HEIGHT, Optional.of(TCBlocks.PLANT_SHIMMERLEAF.get()), true)));
        context.register(SILVERWOOD_TREE_GROWN, new ConfiguredFeature<>(TCFeatures.SILVERWOOD_TREE.get(),
                new SilverwoodTreeConfig(TCBlocks.LOG_SILVERWOOD.get(), TCBlocks.LEAVES_SILVERWOOD.get(), SILVERWOOD_GROWN_MIN_HEIGHT, SILVERWOOD_GROWN_EXTRA_HEIGHT, Optional.empty(), false)));

        context.register(MAGIC_FOREST_TREES,
                new ConfiguredFeature<>(Feature.RANDOM_SELECTOR,
                        new RandomFeatureConfiguration(
                                List.of(new WeightedPlacedFeature(placed.getOrThrow(TCPlacedFeatures.SILVERWOOD_CHECKED), MAGIC_FOREST_SILVERWOOD_CHANCE),
                                        new WeightedPlacedFeature(placed.getOrThrow(TCPlacedFeatures.GREATWOOD_CHECKED), MAGIC_FOREST_GREATWOOD_CHANCE)),
                                placed.getOrThrow(TCPlacedFeatures.BIG_MAGIC_CHECKED))));

        context.register(MAGIC_FOREST_FLORA, new ConfiguredFeature<>(TCFeatures.MAGIC_FOREST_FLORA.get(),
                new MagicForestFloraConfig(TCBlocks.GRASS_AMBIENT.get(), TCBlocks.PLANT_VISHROOM.get(), FLORA_GRASS_ATTEMPTS, FLORA_VISHROOM_ATTEMPTS)));

        context.register(MANA_PODS, new ConfiguredFeature<>(TCFeatures.MANA_PODS.get(), NoneFeatureConfiguration.INSTANCE));

        context.register(NODES_WILD, new ConfiguredFeature<>(TCFeatures.NODE.get(), new NodeFeatureConfig(false, false, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA)));
        context.register(NODES_EERIE, new ConfiguredFeature<>(TCFeatures.NODE.get(), new NodeFeatureConfig(false, true, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA)));
        context.register(OBSIDIAN_TOTEM, new ConfiguredFeature<>(TCFeatures.OBSIDIAN_TOTEM.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(CRIMSON_PORTAL, new ConfiguredFeature<>(TCFeatures.CRIMSON_PORTAL.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(HILLTOP_STONES, new ConfiguredFeature<>(TCFeatures.HILLTOP_STONES.get(), NoneFeatureConfiguration.INSTANCE));

        context.register(CRYSTALS,
                new ConfiguredFeature<>(TCFeatures.CRYSTAL_CLUSTER.get(), new CrystalClusterConfig(
                        List.of(new CrystalClusterConfig.Entry(TCAspects.AER, TCBlocks.CRYSTAL_AER.get()), new CrystalClusterConfig.Entry(TCAspects.IGNIS, TCBlocks.CRYSTAL_IGNIS.get()),
                                new CrystalClusterConfig.Entry(TCAspects.AQUA, TCBlocks.CRYSTAL_AQUA.get()), new CrystalClusterConfig.Entry(TCAspects.TERRA, TCBlocks.CRYSTAL_TERRA.get()),
                                new CrystalClusterConfig.Entry(TCAspects.ORDO, TCBlocks.CRYSTAL_ORDO.get()), new CrystalClusterConfig.Entry(TCAspects.PERDITIO, TCBlocks.CRYSTAL_PERDITIO.get())),
                        CRYSTAL_ATTEMPTS, CRYSTAL_MAX_TOTAL, CRYSTAL_BIOME_ASPECT_CHANCE)));

        TagMatchTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        TagMatchTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        context.register(ORE_CINNABAR, new ConfiguredFeature<>(Feature.REPLACE_SINGLE_BLOCK, new ReplaceBlockConfiguration(
                List.of(OreConfiguration.target(stone, TCBlocks.ORE_CINNABAR.get().defaultBlockState()), OreConfiguration.target(deepslate, TCBlocks.ORE_CINNABAR.get().defaultBlockState())))));
        context.register(ORE_QUARTZ, new ConfiguredFeature<>(Feature.REPLACE_SINGLE_BLOCK, new ReplaceBlockConfiguration(
                List.of(OreConfiguration.target(stone, TCBlocks.ORE_QUARTZ.get().defaultBlockState()), OreConfiguration.target(deepslate, TCBlocks.ORE_QUARTZ.get().defaultBlockState())))));
        context.register(ORE_AMBER, new ConfiguredFeature<>(Feature.REPLACE_SINGLE_BLOCK, new ReplaceBlockConfiguration(
                List.of(OreConfiguration.target(stone, TCBlocks.ORE_AMBER.get().defaultBlockState()), OreConfiguration.target(deepslate, TCBlocks.ORE_AMBER.get().defaultBlockState())))));

        context.register(CINDERPEARL_PATCH, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(TCBlocks.PLANT_CINDERPEARL.get()))));
    }
}
