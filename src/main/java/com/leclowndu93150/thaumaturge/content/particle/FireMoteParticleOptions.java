package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record FireMoteParticleOptions(
        double vx, double vy, double vz, float r, float g, float b, float alpha, float scale, boolean translucent)
        implements ParticleOptions {

    public static final MapCodec<FireMoteParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.DOUBLE.fieldOf("vx").forGetter(FireMoteParticleOptions::vx),
                    Codec.DOUBLE.fieldOf("vy").forGetter(FireMoteParticleOptions::vy),
                    Codec.DOUBLE.fieldOf("vz").forGetter(FireMoteParticleOptions::vz),
                    Codec.FLOAT.fieldOf("r").forGetter(FireMoteParticleOptions::r),
                    Codec.FLOAT.fieldOf("g").forGetter(FireMoteParticleOptions::g),
                    Codec.FLOAT.fieldOf("b").forGetter(FireMoteParticleOptions::b),
                    Codec.FLOAT.fieldOf("alpha").forGetter(FireMoteParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(FireMoteParticleOptions::scale),
                    Codec.BOOL.fieldOf("translucent").forGetter(FireMoteParticleOptions::translucent))
            .apply(inst, FireMoteParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FireMoteParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, o) -> {
                buf.writeDouble(o.vx());
                buf.writeDouble(o.vy());
                buf.writeDouble(o.vz());
                buf.writeFloat(o.r());
                buf.writeFloat(o.g());
                buf.writeFloat(o.b());
                buf.writeFloat(o.alpha());
                buf.writeFloat(o.scale());
                buf.writeBoolean(o.translucent());
            },
            buf -> new FireMoteParticleOptions(
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readBoolean()));

    @Override
    public ParticleType<?> getType() {
        return TCParticles.FIRE_MOTE.get();
    }
}
