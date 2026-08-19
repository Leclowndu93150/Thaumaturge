package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BurstParticleOptions(float scale) implements ParticleOptions {

    public static final MapCodec<BurstParticleOptions> CODEC = Codec.FLOAT.xmap(BurstParticleOptions::new, BurstParticleOptions::scale).fieldOf("scale");

    public static final StreamCodec<ByteBuf, BurstParticleOptions> STREAM_CODEC = ByteBufCodecs.FLOAT.map(BurstParticleOptions::new, BurstParticleOptions::scale);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.BURST.get();
    }
}
