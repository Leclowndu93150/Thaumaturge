package com.leclowndu93150.thaumaturge.client.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public final class MobTraitVisuals {
    private static final int NO_TINT = -1;
    private static final Map<ResourceKey<MobTrait>, MobTraitParticles> PARTICLES = new HashMap<>();
    private static final Map<ResourceKey<MobTrait>, Integer> TINTS = new HashMap<>();

    private MobTraitVisuals() {}

    public static void registerParticles(ResourceKey<MobTrait> trait, MobTraitParticles particles) {
        PARTICLES.put(trait, particles);
    }

    public static void registerTint(ResourceKey<MobTrait> trait, int argb) {
        TINTS.put(trait, argb);
    }

    public static @Nullable MobTraitParticles particles(Holder<MobTrait> trait) {
        return trait.unwrapKey().map(PARTICLES::get).orElse(null);
    }

    public static int tint(Holder<MobTrait> trait) {
        return trait.unwrapKey().map(TINTS::get).orElse(NO_TINT);
    }
}
