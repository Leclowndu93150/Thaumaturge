package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FrostFlakeParticleOptions(float scale) implements ParticleOptions {

    public static final MapCodec<FrostFlakeParticleOptions> CODEC = Codec.FLOAT.xmap(FrostFlakeParticleOptions::new, FrostFlakeParticleOptions::scale).fieldOf("scale");

    public static final StreamCodec<ByteBuf, FrostFlakeParticleOptions> STREAM_CODEC = ByteBufCodecs.FLOAT.map(FrostFlakeParticleOptions::new, FrostFlakeParticleOptions::scale);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.FROST_FLAKE.get();
    }
}
