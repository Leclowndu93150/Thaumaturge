package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FireMoteParticleOptions(double vx, double vy, double vz, float r, float g, float b, float alpha, float scale, boolean translucent) implements ParticleOptions {

    public static final MapCodec<FireMoteParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(com.mojang.serialization.Codec.DOUBLE.fieldOf("vx").forGetter(FireMoteParticleOptions::vx),
                    com.mojang.serialization.Codec.DOUBLE.fieldOf("vy").forGetter(FireMoteParticleOptions::vy),
                    com.mojang.serialization.Codec.DOUBLE.fieldOf("vz").forGetter(FireMoteParticleOptions::vz), com.mojang.serialization.Codec.FLOAT.fieldOf("r").forGetter(FireMoteParticleOptions::r),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("g").forGetter(FireMoteParticleOptions::g), com.mojang.serialization.Codec.FLOAT.fieldOf("b").forGetter(FireMoteParticleOptions::b),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("alpha").forGetter(FireMoteParticleOptions::alpha),
                    com.mojang.serialization.Codec.FLOAT.fieldOf("scale").forGetter(FireMoteParticleOptions::scale),
                    com.mojang.serialization.Codec.BOOL.fieldOf("translucent").forGetter(FireMoteParticleOptions::translucent)).apply(inst, FireMoteParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FireMoteParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.DOUBLE, FireMoteParticleOptions::vx, ByteBufCodecs.DOUBLE,
            FireMoteParticleOptions::vy, ByteBufCodecs.DOUBLE, FireMoteParticleOptions::vz, ByteBufCodecs.FLOAT, FireMoteParticleOptions::r, ByteBufCodecs.FLOAT, FireMoteParticleOptions::g,
            ByteBufCodecs.FLOAT, FireMoteParticleOptions::b, ByteBufCodecs.FLOAT, FireMoteParticleOptions::alpha, ByteBufCodecs.FLOAT, FireMoteParticleOptions::scale, ByteBufCodecs.BOOL,
            FireMoteParticleOptions::translucent, FireMoteParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.FIRE_MOTE.get();
    }
}
