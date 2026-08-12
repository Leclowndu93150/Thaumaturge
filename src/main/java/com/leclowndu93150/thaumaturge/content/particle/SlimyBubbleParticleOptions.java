package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SlimyBubbleParticleOptions(int color, float alpha, float scale, int age) implements ParticleOptions {

    public static final MapCodec<SlimyBubbleParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(SlimyBubbleParticleOptions::color),
                    Codec.FLOAT.fieldOf("alpha").forGetter(SlimyBubbleParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(SlimyBubbleParticleOptions::scale),
                    Codec.INT.fieldOf("age").forGetter(SlimyBubbleParticleOptions::age))
            .apply(inst, SlimyBubbleParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlimyBubbleParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    SlimyBubbleParticleOptions::color,
                    ByteBufCodecs.FLOAT,
                    SlimyBubbleParticleOptions::alpha,
                    ByteBufCodecs.FLOAT,
                    SlimyBubbleParticleOptions::scale,
                    ByteBufCodecs.VAR_INT,
                    SlimyBubbleParticleOptions::age,
                    SlimyBubbleParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.SLIMY_BUBBLE.get();
    }
}
