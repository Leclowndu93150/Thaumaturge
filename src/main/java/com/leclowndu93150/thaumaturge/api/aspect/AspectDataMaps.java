package com.leclowndu93150.thaumaturge.api.aspect;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * Public handles for the aspect data maps that assign base aspects to items and entities.
 *
 * <p>{@link #BASE_ASPECTS} maps each {@link Item} to its declared {@link AspectList}, keyed under
 * {@code thaumaturge:base_aspects}. Addons declare item aspects by shipping a data map entry file at
 * {@code data/<namespace>/data_maps/item/base_aspects.json}. Entries feed the aspect index; items
 * with no declared entry fall through to recipe-derived aspects.
 *
 * <p>{@link #ENTITY_ASPECTS} maps each {@link EntityType} to its {@link AspectList}, keyed under
 * {@code thaumaturge:entity_aspects}, consumed by the scanning system.
 *
 * <p>Both maps are synced to clients. Their {@link AspectList} values serialize through
 * {@link AspectList#CODEC}.
 *
 * @since 1.0.0
 */
public final class AspectDataMaps {
    /** Item-to-aspect base assignments, keyed {@code thaumaturge:base_aspects}. */
    public static final DataMapType<Item, AspectList> BASE_ASPECTS = DataMapType.builder(Identifier.fromNamespaceAndPath("thaumaturge", "base_aspects"), Registries.ITEM, AspectList.CODEC)
            .synced(AspectList.CODEC, false).build();

    /** Entity-to-aspect assignments, keyed {@code thaumaturge:entity_aspects}. */
    public static final DataMapType<EntityType<?>, AspectList> ENTITY_ASPECTS = DataMapType
            .builder(Identifier.fromNamespaceAndPath("thaumaturge", "entity_aspects"), Registries.ENTITY_TYPE, AspectList.CODEC).synced(AspectList.CODEC, false).build();

    private AspectDataMaps() {}
}
