package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record AirGustParticleOptions(float scale) implements ParticleOptions {

    public static final MapCodec<AirGustParticleOptions> CODEC = Codec.FLOAT.xmap(AirGustParticleOptions::new, AirGustParticleOptions::scale).fieldOf("scale");

    public static final StreamCodec<ByteBuf, AirGustParticleOptions> STREAM_CODEC = ByteBufCodecs.FLOAT.map(AirGustParticleOptions::new, AirGustParticleOptions::scale);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.AIR_GUST.get();
    }
}
