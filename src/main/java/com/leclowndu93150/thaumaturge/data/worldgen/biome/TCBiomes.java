package com.leclowndu93150.thaumaturge.data.worldgen.biome;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TCPlacedFeatures;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
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
    private static final int DEFAULT_FOG_COLOR = 12638463;
    private static final int DEFAULT_WATER_FOG_COLOR = 329011;

    private TCBiomes() {}

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, TCIds.rl(path));
    }

    private static int calculateSkyColor(float temperature) {
        float shifted = Mth.clamp(temperature / 3.0F, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - shifted * 0.05F, 0.5F + shifted * 0.1F, 1.0F);
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

    private static Biome magicalForest(
            HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(mobs);
        BiomeDefaultFeatures.commonSpawns(mobs);
        mobs.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 2, 1, 3));
        mobs.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 2, 1, 3));
        mobs.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 3, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 3, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.VEX, 1, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(TCEntities.WISP.get(), 20, 1, 2));

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        globalGeneration(generation);
        generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiscOverworldPlacements.FOREST_ROCK);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, TCPlacedFeatures.TREES_MAGIC_FOREST);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, TCPlacedFeatures.MAGIC_FOREST_FLORA);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, TCPlacedFeatures.MANA_PODS);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
        BiomeDefaultFeatures.addDefaultExtraVegetation(generation);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(MAGICAL_FOREST_TEMPERATURE)
                .downfall(MAGICAL_FOREST_DOWNFALL)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .skyColor(calculateSkyColor(MAGICAL_FOREST_TEMPERATURE))
                        .fogColor(DEFAULT_FOG_COLOR)
                        .waterColor(MAGICAL_FOREST_WATER)
                        .waterFogColor(DEFAULT_WATER_FOG_COLOR)
                        .grassColorOverride(MAGICAL_FOREST_GRASS)
                        .foliageColorOverride(MAGICAL_FOREST_FOLIAGE)
                        .build())
                .mobSpawnSettings(mobs.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome eerie(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(mobs);
        mobs.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 3, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 8, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 4, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(TCEntities.BRAINY_ZOMBIE.get(), 32, 1, 1));
        mobs.addSpawn(
                MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(TCEntities.GIANT_BRAINY_ZOMBIE.get(), 8, 1, 1));
        mobs.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(TCEntities.WISP.get(), 3, 1, 1));
        mobs.addSpawn(
                MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(TCEntities.ELDRITCH_GUARDIAN.get(), 1, 1, 1));

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        globalGeneration(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_BIRCH_AND_OAK);
        BiomeDefaultFeatures.addDefaultFlowers(generation);
        generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
        BiomeDefaultFeatures.addDefaultMushrooms(generation);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(EERIE_TEMPERATURE)
                .downfall(0.0F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .skyColor(EERIE_SKY)
                        .fogColor(DEFAULT_FOG_COLOR)
                        .waterColor(EERIE_WATER)
                        .waterFogColor(DEFAULT_WATER_FOG_COLOR)
                        .grassColorOverride(EERIE_GRASS)
                        .foliageColorOverride(EERIE_GRASS)
                        .build())
                .mobSpawnSettings(mobs.build())
                .generationSettings(generation.build())
                .build();
    }

    private static Biome eldritch(HolderGetter<PlacedFeature> placed, HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        mobs.addSpawn(
                MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(TCEntities.INHABITED_ZOMBIE.get(), 1, 1, 1));
        mobs.addSpawn(
                MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(TCEntities.ELDRITCH_GUARDIAN.get(), 1, 1, 1));
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed, carvers);
        globalGeneration(generation);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(ELDRITCH_TEMPERATURE)
                .downfall(ELDRITCH_DOWNFALL)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .skyColor(calculateSkyColor(ELDRITCH_TEMPERATURE))
                        .fogColor(DEFAULT_FOG_COLOR)
                        .waterColor(NORMAL_WATER_COLOR)
                        .waterFogColor(DEFAULT_WATER_FOG_COLOR)
                        .build())
                .mobSpawnSettings(mobs.build())
                .generationSettings(generation.build())
                .build();
    }
}
