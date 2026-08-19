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

public record BubbleParticleOptions(int color, float alpha, float scale, int age, float buoyancy, boolean droplet) implements ParticleOptions {

    public static final MapCodec<BubbleParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
            .group(Codec.INT.fieldOf("color").forGetter(BubbleParticleOptions::color), Codec.FLOAT.fieldOf("alpha").forGetter(BubbleParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(BubbleParticleOptions::scale), Codec.INT.fieldOf("age").forGetter(BubbleParticleOptions::age),
                    Codec.FLOAT.fieldOf("buoyancy").forGetter(BubbleParticleOptions::buoyancy), Codec.BOOL.fieldOf("droplet").forGetter(BubbleParticleOptions::droplet))
            .apply(inst, BubbleParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BubbleParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, BubbleParticleOptions::color, ByteBufCodecs.FLOAT,
            BubbleParticleOptions::alpha, ByteBufCodecs.FLOAT, BubbleParticleOptions::scale, ByteBufCodecs.VAR_INT, BubbleParticleOptions::age, ByteBufCodecs.FLOAT, BubbleParticleOptions::buoyancy,
            ByteBufCodecs.BOOL, BubbleParticleOptions::droplet, BubbleParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.BUBBLE.get();
    }
}
