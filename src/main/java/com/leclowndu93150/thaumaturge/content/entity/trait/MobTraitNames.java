package com.leclowndu93150.thaumaturge.content.entity.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class MobTraitNames {
    private static final String PREFIX = "mob_trait.";
    private static final String SEPARATOR = ".";

    private MobTraitNames() {}

    public static Component of(Holder<MobTrait> trait) {
        return Component.translatable(key(trait.unwrapKey().orElseThrow().location()));
    }

    public static String key(ResourceLocation id) {
        return PREFIX + id.getNamespace() + SEPARATOR + id.getPath();
    }
}
