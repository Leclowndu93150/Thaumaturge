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

public record SmokeSpiralParticleOptions(float radius, int start, int minY, float r, float g, float b) implements ParticleOptions {

    public static final MapCodec<SmokeSpiralParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst
                    .group(Codec.FLOAT.fieldOf("radius").forGetter(SmokeSpiralParticleOptions::radius), Codec.INT.fieldOf("start").forGetter(SmokeSpiralParticleOptions::start),
                            Codec.INT.fieldOf("min_y").forGetter(SmokeSpiralParticleOptions::minY), Codec.FLOAT.fieldOf("r").forGetter(SmokeSpiralParticleOptions::r),
                            Codec.FLOAT.fieldOf("g").forGetter(SmokeSpiralParticleOptions::g), Codec.FLOAT.fieldOf("b").forGetter(SmokeSpiralParticleOptions::b))
                    .apply(inst, SmokeSpiralParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SmokeSpiralParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, SmokeSpiralParticleOptions::radius,
            ByteBufCodecs.VAR_INT, SmokeSpiralParticleOptions::start, ByteBufCodecs.VAR_INT, SmokeSpiralParticleOptions::minY, ByteBufCodecs.FLOAT, SmokeSpiralParticleOptions::r, ByteBufCodecs.FLOAT,
            SmokeSpiralParticleOptions::g, ByteBufCodecs.FLOAT, SmokeSpiralParticleOptions::b, SmokeSpiralParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.SMOKE_SPIRAL.get();
    }
}
