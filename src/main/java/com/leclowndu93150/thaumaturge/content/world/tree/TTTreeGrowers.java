package com.leclowndu93150.thaumaturge.content.world.tree;

import com.leclowndu93150.thaumaturge.TTIds;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class TTTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> GREATWOOD_TREE_GROWN =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, TTIds.rl("greatwood_tree_grown"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVERWOOD_TREE_GROWN =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, TTIds.rl("silverwood_tree_grown"));

    public static final TreeGrower GREATWOOD = new TreeGrower(
            "thaumaturge:greatwood", Optional.of(GREATWOOD_TREE_GROWN), Optional.empty(), Optional.empty());
    public static final TreeGrower SILVERWOOD = new TreeGrower(
            "thaumaturge:silverwood", Optional.empty(), Optional.of(SILVERWOOD_TREE_GROWN), Optional.empty());

    private TTTreeGrowers() {}

    public static void touch() {}
}
