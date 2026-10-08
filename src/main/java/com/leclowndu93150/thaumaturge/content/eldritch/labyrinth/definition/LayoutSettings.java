package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;

public record LayoutSettings(
        float coverage,
        float loopRatio,
        int minLoopLength,
        IntProvider branchLength,
        float straightness,
        float keyDistance,
        float costNoise) {
    public static final LayoutSettings DEFAULT =
            new LayoutSettings(0.72F, 0.08F, 8, UniformInt.of(2, 6), 0.6F, 0.6F, 0.5F);

    private static final Codec<Float> UNIT = Codec.floatRange(0.0F, 1.0F);
    private static final int MAX_LOOP_LENGTH = 64;

    public static final Codec<LayoutSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    UNIT.optionalFieldOf("coverage", DEFAULT.coverage).forGetter(LayoutSettings::coverage),
                    UNIT.optionalFieldOf("loop_ratio", DEFAULT.loopRatio).forGetter(LayoutSettings::loopRatio),
                    Codec.intRange(3, MAX_LOOP_LENGTH)
                            .optionalFieldOf("min_loop_length", DEFAULT.minLoopLength)
                            .forGetter(LayoutSettings::minLoopLength),
                    IntProvider.POSITIVE_CODEC
                            .optionalFieldOf("branch_length", DEFAULT.branchLength)
                            .forGetter(LayoutSettings::branchLength),
                    UNIT.optionalFieldOf("straightness", DEFAULT.straightness).forGetter(LayoutSettings::straightness),
                    UNIT.optionalFieldOf("key_distance", DEFAULT.keyDistance).forGetter(LayoutSettings::keyDistance),
                    UNIT.optionalFieldOf("cost_noise", DEFAULT.costNoise).forGetter(LayoutSettings::costNoise))
            .apply(instance, LayoutSettings::new));
}
