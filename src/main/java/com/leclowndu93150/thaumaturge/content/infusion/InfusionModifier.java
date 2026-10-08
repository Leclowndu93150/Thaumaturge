package com.leclowndu93150.thaumaturge.content.infusion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record InfusionModifier(int cycleTime, float cost, float stability) {
    public static final Codec<InfusionModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("cycle_time", 0).forGetter(InfusionModifier::cycleTime),
                    Codec.FLOAT.optionalFieldOf("cost", 0.0F).forGetter(InfusionModifier::cost),
                    Codec.FLOAT.optionalFieldOf("stability", 0.0F).forGetter(InfusionModifier::stability))
            .apply(instance, InfusionModifier::new));
}
