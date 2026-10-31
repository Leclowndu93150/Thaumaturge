package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class TTEntityTags {
    public static final TagKey<EntityType<?>> TAINT_CONVERSION_IMMUNE = key("taint_conversion/immune");
    public static final TagKey<EntityType<?>> SABLE_RETAIN_IN_SUB_LEVEL = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(TTIds.SABLE, "retain_in_sub_level"));

    private TTEntityTags() {}

    private static TagKey<EntityType<?>> key(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(TTIds.MODID, path));
    }
}
