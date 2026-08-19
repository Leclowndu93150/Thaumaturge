package com.leclowndu93150.thaumaturge.api.golems.seals;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;

/**
 * Registration entry for a seal kind: how to create fresh behavior instances plus the
 * placer item that represents the kind in inventories.
 *
 * @param factory    creates a fresh, unconfigured behavior instance per placement
 * @param placerItem the item that places this seal kind
 * @since 1.0.0
 */
public record SealType(Supplier<? extends ISeal> factory, Supplier<? extends ItemLike> placerItem) {
    /** The registry key for seal kinds. */
    public static final ResourceKey<Registry<SealType>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "seal"));
}
