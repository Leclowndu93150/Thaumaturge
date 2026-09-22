package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class TCEntityTags {
    public static final TagKey<EntityType<?>> TAINT_CONVERSION_IMMUNE = key("taint_conversion/immune");
    public static final TagKey<EntityType<?>> TAINT_LEGACY_CREEPER = key("taint_conversion/legacy_creeper");
    public static final TagKey<EntityType<?>> TAINT_LEGACY_COW = key("taint_conversion/legacy_cow");
    public static final TagKey<EntityType<?>> TAINT_LEGACY_PIG = key("taint_conversion/legacy_pig");
    public static final TagKey<EntityType<?>> TAINT_LEGACY_CHICKEN = key("taint_conversion/legacy_chicken");
    public static final TagKey<EntityType<?>> TAINT_LEGACY_SHEEP = key("taint_conversion/legacy_sheep");
    public static final TagKey<EntityType<?>> TAINT_LEGACY_VILLAGER = key("taint_conversion/legacy_villager");

    private TCEntityTags() {}

    private static TagKey<EntityType<?>> key(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(TCIds.MODID, path));
    }
}
