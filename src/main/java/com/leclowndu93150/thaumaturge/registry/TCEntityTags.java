package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class TCEntityTags {
    public static final TagKey<EntityType<?>> TAINT_CONVERSION_IMMUNE = key("taint_conversion/immune");
    public static final TagKey<EntityType<?>> TAINT_OVERLAY = key("taint_overlay");

    private TCEntityTags() {}

    private static TagKey<EntityType<?>> key(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(TCIds.MODID, path));
    }
}
