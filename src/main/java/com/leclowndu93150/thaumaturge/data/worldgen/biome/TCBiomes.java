package com.leclowndu93150.thaumaturge.data.worldgen.biome;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TCPlacedFeatures;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class TCBiomes {
    public static final ResourceKey<Biome> MAGICAL_FOREST = key("magical_forest");
    public static final ResourceKey<Biome> EERIE = key("eerie");
    public static final ResourceKey<Biome> ELDRITCH = key("eldritch");

    private static final float MAGICAL_FOREST_TEMPERATURE = 0.8F;
    private static final float MAGICAL_FOREST_DOWNFALL = 0.4F;
    private static final int MAGICAL_FOREST_GRASS = 5635969;
    private static final int MAGICAL_FOREST_FOLIAGE = 6750149;
    private static final int MAGICAL_FOREST_WATER = 30702;
    private static final float EERIE_TEMPERATURE = 0.8F;
    private static final int EERIE_GRASS = 4212800;
    private static final int EERIE_SKY = 2237081;
    private static final int EERIE_WATER = 3035999;
    private static final float ELDRITCH_TEMPERATURE = 0.8F;
    private static final int NORMAL_WATER_COLOR = 4159204;
    private static final float ELDRITCH_DOWNFALL = 0.2F;

    private TCBiomes() {}

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, TCIds.rl(path));
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);
        context.register(MAGICAL_FOREST, magicalForest(placed, carvers));
        context.register(EERIE, eerie(placed, carvers));
        context.register(ELDRITCH, eldritch(placed, carvers));
    }

    private static void globalGeneration(BiomeGenerationSettings.Builder generation) {
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
    }

    private static Biome magicalForest(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(mobs);
        BiomeDefaultFeatures.commonSpawns(mobs);
        mobs.addSpawn(MobCategory.CREATURE, 2, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 1, 3));
        mobs.addSpawn(MobCategory.CREATURE, 2, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 1, 3));
        mobs.addSpawn(MobCategory.MONSTER, 3, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, 3, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, 1, new MobSpawnSettings.SpawnerData(EntityType.VEX, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, 20, new MobSpawnSettings.SpawnerData(TCEntities.WISP.get(), 1, 2));

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        globalGeneration(generation);
        generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.FOREST_ROCK);
        BiomeDefaultFeatures.addForestFlowers(generation);
        BiomeDefaultFeatures.addFerns(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, TCPlacedFeatures.TREES_MAGIC_FOREST);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, TCPlacedFeatures.MAGIC_FOREST_FLORA);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, TCPlacedFeatures.MANA_PODS);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        BiomeDefaultFeatures.addForestGrass(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation, false);

        return new Biome.BiomeBuilder().hasPrecipitation(true).temperature(MAGICAL_FOREST_TEMPERATURE).downfall(MAGICAL_FOREST_DOWNFALL)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, OverworldBiomes.calculateSkyColor(MAGICAL_FOREST_TEMPERATURE))
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(MAGICAL_FOREST_WATER).grassColorOverride(MAGICAL_FOREST_GRASS).foliageColorOverride(MAGICAL_FOREST_FOLIAGE).build())
                .mobSpawnSettings(mobs.build()).generationSettings(generation.build()).build();
    }

    private static Biome eerie(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(mobs);
        mobs.addSpawn(MobCategory.AMBIENT, 3, new MobSpawnSettings.SpawnerData(EntityType.BAT, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, 8, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, 4, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, 32, new MobSpawnSettings.SpawnerData(TCEntities.BRAINY_ZOMBIE.get(), 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, 8, new MobSpawnSettings.SpawnerData(TCEntities.GIANT_BRAINY_ZOMBIE.get(), 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, 3, new MobSpawnSettings.SpawnerData(TCEntities.WISP.get(), 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, 1, new MobSpawnSettings.SpawnerData(TCEntities.ELDRITCH_GUARDIAN.get(), 1, 1));

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        globalGeneration(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BIRCH_AND_OAK_LEAF_LITTER);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);

        return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(EERIE_TEMPERATURE).downfall(0.0F).setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.opaque(EERIE_SKY))
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(EERIE_WATER).grassColorOverride(EERIE_GRASS).foliageColorOverride(EERIE_GRASS).build()).mobSpawnSettings(mobs.build())
                .generationSettings(generation.build()).build();
    }

    private static Biome eldritch(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        mobs.addSpawn(MobCategory.MONSTER, 1, new MobSpawnSettings.SpawnerData(TCEntities.INHABITED_ZOMBIE.get(), 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, 1, new MobSpawnSettings.SpawnerData(TCEntities.ELDRITCH_GUARDIAN.get(), 1, 1));
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        globalGeneration(generation);

        return new Biome.BiomeBuilder().hasPrecipitation(true).temperature(ELDRITCH_TEMPERATURE).downfall(ELDRITCH_DOWNFALL)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, OverworldBiomes.calculateSkyColor(ELDRITCH_TEMPERATURE))
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(NORMAL_WATER_COLOR).build()).mobSpawnSettings(mobs.build()).generationSettings(generation.build()).build();
    }
}
