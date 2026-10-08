package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SmelterStats(int smeltInterval, float efficiency) {
    public static final SmelterStats DEFAULT = new SmelterStats(15, 0.8F);

    public static final Codec<SmelterStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(1, Integer.MAX_VALUE)
                            .fieldOf("smelt_interval")
                            .forGetter(SmelterStats::smeltInterval),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("efficiency").forGetter(SmelterStats::efficiency))
            .apply(instance, SmelterStats::new));
}
