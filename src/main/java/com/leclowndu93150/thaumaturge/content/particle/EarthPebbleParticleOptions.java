package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record EarthPebbleParticleOptions(float scale) implements ParticleOptions {

    public static final MapCodec<EarthPebbleParticleOptions> CODEC = Codec.FLOAT.xmap(EarthPebbleParticleOptions::new, EarthPebbleParticleOptions::scale).fieldOf("scale");

    public static final StreamCodec<ByteBuf, EarthPebbleParticleOptions> STREAM_CODEC = ByteBufCodecs.FLOAT.map(EarthPebbleParticleOptions::new, EarthPebbleParticleOptions::scale);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.EARTH_PEBBLE.get();
    }
}
