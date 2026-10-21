package com.leclowndu93150.thaumaturge.api.rifts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** The supported, non-collapsing state carried by a rift vessel. */
public record RiftContents(int size, int seed, float stability, long revision) {
    public static final Codec<RiftContents> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(Codec.intRange(1, 100).fieldOf("size").forGetter(RiftContents::size), Codec.INT.fieldOf("seed").forGetter(RiftContents::seed),
                    Codec.FLOAT.validate(value -> Float.isFinite(value) && value >= -100 && value <= 100 ? DataResult.success(value) : DataResult.error(() -> "Invalid rift stability"))
                            .fieldOf("stability").forGetter(RiftContents::stability),
                    Codec.LONG.validate(value -> value >= 0 && value < Long.MAX_VALUE ? DataResult.success(value) : DataResult.error(() -> "Invalid rift revision")).fieldOf("revision")
                            .forGetter(RiftContents::revision))
                    .apply(instance, RiftContents::new));

    public RiftContents(int size, int seed, float stability) {
        this(size, seed, stability, 0);
    }
}
