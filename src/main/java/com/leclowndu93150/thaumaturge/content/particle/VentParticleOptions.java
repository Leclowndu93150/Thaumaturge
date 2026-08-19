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

public record VentParticleOptions(double vx, double vy, double vz, int color, float scale, boolean variant) implements ParticleOptions {

    public static final MapCodec<VentParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst
                    .group(Codec.DOUBLE.fieldOf("vx").forGetter(VentParticleOptions::vx), Codec.DOUBLE.fieldOf("vy").forGetter(VentParticleOptions::vy),
                            Codec.DOUBLE.fieldOf("vz").forGetter(VentParticleOptions::vz), Codec.INT.fieldOf("color").forGetter(VentParticleOptions::color),
                            Codec.FLOAT.fieldOf("scale").forGetter(VentParticleOptions::scale), Codec.BOOL.fieldOf("variant").forGetter(VentParticleOptions::variant))
                    .apply(inst, VentParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VentParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.DOUBLE, VentParticleOptions::vx, ByteBufCodecs.DOUBLE,
            VentParticleOptions::vy, ByteBufCodecs.DOUBLE, VentParticleOptions::vz, ByteBufCodecs.INT, VentParticleOptions::color, ByteBufCodecs.FLOAT, VentParticleOptions::scale, ByteBufCodecs.BOOL,
            VentParticleOptions::variant, VentParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.VENT.get();
    }
}
