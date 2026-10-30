package com.leclowndu93150.thaumaturge.api.items.trim;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

/**
 * Public addon API for the armor trim materials Thaumaturge registers. Constants are {@link ResourceKey}s into the
 * datapack trim material registry. Each material is provided by the matching ingot through the
 * {@code minecraft:provides_trim_material} component, and its palette is named {@code thaumaturge_<path>}.
 *
 * <p>Gameplay bonuses for a material live in the {@code thaumaturge:trim_bonus} data map, see
 * {@link TrimMaterialBonus}.
 *
 * @since 1.1.0
 */
public final class TTTrimMaterials {
    public static final ResourceKey<TrimMaterial> BRASS = key("brass");
    public static final ResourceKey<TrimMaterial> THAUMIUM = key("thaumium");
    public static final ResourceKey<TrimMaterial> VOID_METAL = key("void_metal");

    private TTTrimMaterials() {}

    private static ResourceKey<TrimMaterial> key(String path) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, TTIds.rl(path));
    }
}
