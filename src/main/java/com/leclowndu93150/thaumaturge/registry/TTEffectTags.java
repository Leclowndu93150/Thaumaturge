package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;

public final class TTEffectTags {
    public static final TagKey<MobEffect> MANA_BEAN_EFFECTS = key("mana_bean_effects");

    private TTEffectTags() {}

    private static TagKey<MobEffect> key(String path) {
        return TagKey.create(Registries.MOB_EFFECT, TTIds.rl(path));
    }
}
