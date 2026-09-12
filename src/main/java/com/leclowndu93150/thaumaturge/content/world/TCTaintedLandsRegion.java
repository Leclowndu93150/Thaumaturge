package com.leclowndu93150.thaumaturge.content.world;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

/**
 * Rare TC4-style natural Tainted Lands region.
 *
 * <p>The low region weight makes these much rarer than Magical Forests. Replacing the same forest
 * climate points gives each occurrence a coherent vanilla-biome-sized footprint rather than
 * scattering narrow climate slices through the landscape. Dynamic taint spread remains independent
 * of worldgen.
 */
public final class TCTaintedLandsRegion extends Region {
    public static final int WEIGHT = 1;

    public TCTaintedLandsRegion(ResourceLocation name) {
        super(name, RegionType.OVERWORLD, WEIGHT);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        if (!ThaumaturgeCommonConfig.GENERATE_TAINTED_LANDS.get()) return;
        this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
            builder.replaceBiome(Biomes.FOREST, TCBiomes.TAINTED_LANDS);
            builder.replaceBiome(Biomes.FLOWER_FOREST, TCBiomes.TAINTED_LANDS);
            builder.replaceBiome(Biomes.BEACH, TCBiomes.TAINTED_LANDS);
        });
    }
}
