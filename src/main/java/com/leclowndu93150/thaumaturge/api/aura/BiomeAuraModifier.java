package com.leclowndu93150.thaumaturge.api.aura;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Per-biome aura generation modifier. Data-map value attached to entries of
 * {@link net.minecraft.core.registries.Registries#BIOME}.
 *
 * <p>The {@link #value()} is multiplied into the base aura sample at chunk generation time. The
 * default value when a biome has no entry is {@code 0.5}.
 *
 * @param value the per-biome multiplier; sensible range is {@code [0, 1]}
 * @since 1.0.0
 */
public record BiomeAuraModifier(float value) {
    /**
     * The codec used to deserialize the data-map value from JSON.
     */
    public static final Codec<BiomeAuraModifier> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(Codec.FLOAT.fieldOf("value").forGetter(BiomeAuraModifier::value)).apply(instance, BiomeAuraModifier::new));
}
