package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.leclowndu93150.thaumaturge.data.worldgen.feature.TCPlacedFeatures;
import com.leclowndu93150.thaumaturge.registry.TCBiomeModifierSerializers;
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

/**
 * Replaces only Thaumaturge's native tree placed features when Dynamic Trees has its own world generation enabled.
 *
 * <p>Dynamic Trees is an optional compile-only dependency. If it is absent or has world generation disabled, this
 * modifier does nothing and Thaumaturge's normal tree generation remains intact.</p>
 */
public final class DynamicTreesWorldgenBiomeModifier implements BiomeModifier {
    private static final Set<ResourceKey<PlacedFeature>> NATIVE_TREE_FEATURES = Set.of(
            TCPlacedFeatures.TREES_MAGIC_FOREST,
            TCPlacedFeatures.GREATWOOD_NATURAL,
            TCPlacedFeatures.GREATWOOD_NATURAL_RARE,
            TCPlacedFeatures.SILVERWOOD_NATURAL);

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.REMOVE || !isDynamicTreesWorldgenEnabled()) return;

        builder.getGenerationSettings()
                .getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION)
                .removeIf(feature ->
                        feature.unwrapKey().map(NATIVE_TREE_FEATURES::contains).orElse(false));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return TCBiomeModifierSerializers.DYNAMIC_TREES_WORLDGEN.get();
    }

    private static boolean isDynamicTreesWorldgenEnabled() {
        return ModList.get().isLoaded("dynamictrees") && DTConfigs.SERVER.worldGen.get();
    }
}
