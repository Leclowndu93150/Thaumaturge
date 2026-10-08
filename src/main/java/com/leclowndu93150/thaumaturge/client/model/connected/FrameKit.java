package com.leclowndu93150.thaumaturge.client.model.connected;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public record FrameKit(ResourceLocation texture, int top, int bottom, int left, int right) {
    public static final int MAX_FRAME = 4;
    public static final Codec<FrameKit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(FrameKit::texture),
                    ExtraCodecs.intRange(0, MAX_FRAME).optionalFieldOf("top", 1).forGetter(FrameKit::top),
                    ExtraCodecs.intRange(0, MAX_FRAME)
                            .optionalFieldOf("bottom", 1)
                            .forGetter(FrameKit::bottom),
                    ExtraCodecs.intRange(0, MAX_FRAME)
                            .optionalFieldOf("left", 1)
                            .forGetter(FrameKit::left),
                    ExtraCodecs.intRange(0, MAX_FRAME)
                            .optionalFieldOf("right", 1)
                            .forGetter(FrameKit::right))
            .apply(instance, FrameKit::new));

    public static FrameKit uniform(ResourceLocation texture, int width) {
        return new FrameKit(texture, width, width, width, width);
    }
}
