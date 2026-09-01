package com.leclowndu93150.thaumaturge.content.world;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import com.mojang.datafixers.util.Pair;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ModifiedVanillaOverworldBuilder;
import terrablender.api.Region;
import terrablender.api.RegionType;

/**
 * Rare TC4-style natural Tainted Lands region.
 *
 * <p>TC4 inserted Tainted Lands into the warm/cool biome pools with weight 2. TerraBlender region
 * weights are not comparable to the old per-biome pool weights: reserving an entire modified
 * vanilla region made natural Tainted Lands dramatically larger than their TC4 counterparts. Keep
 * the minimum region weight and replace only every eighth eligible vanilla climate point. This
 * breaks the region into ordinary-biome-sized taint pockets instead of continent-scale blankets.
 * River and frozen-river parameter points are never candidates. Dynamic taint spread remains
 * independent of worldgen.
 */
public final class TCTaintedLandsRegion extends Region {
    public static final int WEIGHT = 1;
    private static final int CLIMATE_SLICE_STRIDE = 7;
    private static final Set<ResourceKey<Biome>> ELIGIBLE_LAND_BIOMES = Set.of(
            Biomes.PLAINS,
            Biomes.SUNFLOWER_PLAINS,
            Biomes.FOREST,
            Biomes.FLOWER_FOREST,
            Biomes.BIRCH_FOREST,
            Biomes.OLD_GROWTH_BIRCH_FOREST,
            Biomes.DARK_FOREST,
            Biomes.SWAMP,
            Biomes.OLD_GROWTH_PINE_TAIGA,
            Biomes.OLD_GROWTH_SPRUCE_TAIGA);

    public TCTaintedLandsRegion(ResourceLocation name) {
        super(name, RegionType.OVERWORLD, WEIGHT);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        ModifiedVanillaOverworldBuilder vanilla = new ModifiedVanillaOverworldBuilder();
        int eligibleIndex = 0;
        boolean generate = ThaumaturgeCommonConfig.GENERATE_TAINTED_LANDS.get();
        for (Pair<Climate.ParameterPoint, ResourceKey<Biome>> entry : vanilla.build()) {
            ResourceKey<Biome> biome = entry.getSecond();
            if (generate && ELIGIBLE_LAND_BIOMES.contains(biome)) {
                if (eligibleIndex++ % CLIMATE_SLICE_STRIDE == 0) {
                    biome = TCBiomes.TAINTED_LANDS;
                }
            }
            mapper.accept(Pair.of(entry.getFirst(), biome));
        }
    }
}
