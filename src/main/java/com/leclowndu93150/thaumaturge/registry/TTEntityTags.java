package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class TTEntityTags {
    public static final TagKey<EntityType<?>> TAINT_CONVERSION_IMMUNE = key("taint_conversion/immune");

    private TTEntityTags() {}

    private static TagKey<EntityType<?>> key(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, TTIds.rl(path));
    }
}
