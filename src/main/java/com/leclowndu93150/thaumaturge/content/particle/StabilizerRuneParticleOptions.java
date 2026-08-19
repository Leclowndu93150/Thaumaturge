package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StabilizerRuneParticleOptions(int life) implements ParticleOptions {

    public static final MapCodec<StabilizerRuneParticleOptions> CODEC = Codec.INT.xmap(StabilizerRuneParticleOptions::new, StabilizerRuneParticleOptions::life).fieldOf("life");

    public static final StreamCodec<ByteBuf, StabilizerRuneParticleOptions> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(StabilizerRuneParticleOptions::new, StabilizerRuneParticleOptions::life);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.STABILIZER_RUNE.get();
    }
}
