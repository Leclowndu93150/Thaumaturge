/*
 * Thaumaturge rewrite for modern Minecraft.
 */
package com.leclowndu93150.thaumaturge.api.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * Entity type tags that opt mobs into Thaumaturge mechanics. Addons add their own entity types to these tags through datapacks.
 *
 * @since 1.0.0
 */
public final class ThaumaturgeEntityTypeTags {
    /**
     * Mobs aligned with the eldritch powers. They treat each other as allies, regenerate near eldritch obelisks, are left alone by
     * effect sap, and play the eldritch shield sound when struck.
     */
    public static final TagKey<EntityType<?>> ELDRITCH = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("thaumaturge", "eldritch"));

    /**
     * Mobs born of taint. They count as tainted for champion traits, taint immunity and every check that asks whether a mob is
     * tainted.
     */
    public static final TagKey<EntityType<?>> TAINTED = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("thaumaturge", "tainted"));

    private ThaumaturgeEntityTypeTags() {}
}
