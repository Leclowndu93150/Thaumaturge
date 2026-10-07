package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TTPlacedFeatures;
import com.leclowndu93150.thaumaturge.registry.TTBiomeModifierSerializers;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public final class DynamicTreesWorldgenBiomeModifier implements BiomeModifier {
    public static final DynamicTreesWorldgenBiomeModifier INSTANCE = new DynamicTreesWorldgenBiomeModifier();
    public static final MapCodec<DynamicTreesWorldgenBiomeModifier> CODEC = MapCodec.unit(INSTANCE);

    private static final Set<ResourceKey<PlacedFeature>> NATIVE_TREE_FEATURES = Set.of(TTPlacedFeatures.TREES_MAGIC_FOREST, TTPlacedFeatures.GREATWOOD_NATURAL, TTPlacedFeatures.GREATWOOD_NATURAL_RARE,
            TTPlacedFeatures.SILVERWOOD_NATURAL);

    private DynamicTreesWorldgenBiomeModifier() {}

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.REMOVE || !isDynamicTreesWorldgenEnabled()) {
            return;
        }
        builder.getGenerationSettings().getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION).removeIf(feature -> feature.unwrapKey().map(NATIVE_TREE_FEATURES::contains).orElse(false));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return TTBiomeModifierSerializers.DYNAMIC_TREES_WORLDGEN.get();
    }

    private static boolean isDynamicTreesWorldgenEnabled() {
        return ModList.get().isLoaded(TTIds.DYNAMIC_TREES) && DTConfigs.SERVER.worldGen.get();
    }
}
