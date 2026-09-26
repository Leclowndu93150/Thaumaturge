package com.leclowndu93150.thaumaturge.compat.apothicenchanting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EnchantingStats(float maxEterna, float eterna, float quanta, float arcana, int clues) {
    public static final float DEFAULT_MAX_ETERNA = 30.0F;

    public static final Codec<EnchantingStats> CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(Codec.FLOAT.optionalFieldOf("maxEterna", DEFAULT_MAX_ETERNA).forGetter(EnchantingStats::maxEterna),
                            Codec.FLOAT.optionalFieldOf("eterna", 0.0F).forGetter(EnchantingStats::eterna), Codec.FLOAT.optionalFieldOf("quanta", 0.0F).forGetter(EnchantingStats::quanta),
                            Codec.FLOAT.optionalFieldOf("arcana", 0.0F).forGetter(EnchantingStats::arcana), Codec.INT.optionalFieldOf("clues", 0).forGetter(EnchantingStats::clues))
                    .apply(instance, EnchantingStats::new));
}
