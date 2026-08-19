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

public record FlameFanParticleOptions(float scale, float lift, float alpha) implements ParticleOptions {

    public static final MapCodec<FlameFanParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(Codec.FLOAT.fieldOf("scale").forGetter(FlameFanParticleOptions::scale),
            Codec.FLOAT.fieldOf("lift").forGetter(FlameFanParticleOptions::lift), Codec.FLOAT.fieldOf("alpha").forGetter(FlameFanParticleOptions::alpha)).apply(inst, FlameFanParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlameFanParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, FlameFanParticleOptions::scale, ByteBufCodecs.FLOAT,
            FlameFanParticleOptions::lift, ByteBufCodecs.FLOAT, FlameFanParticleOptions::alpha, FlameFanParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.FLAME_FAN.get();
    }
}
