package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public record InstabilityEffectType<T extends InstabilityEffect>(MapCodec<T> codec) {
    public static final ResourceKey<Registry<InstabilityEffectType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("instability_effect_type"));
}
