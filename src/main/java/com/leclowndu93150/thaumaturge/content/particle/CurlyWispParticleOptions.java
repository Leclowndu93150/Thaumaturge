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

public record CurlyWispParticleOptions(int color, float alpha, float scale, int delay, int seed) implements ParticleOptions {

    public static final MapCodec<CurlyWispParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(Codec.INT.fieldOf("color").forGetter(CurlyWispParticleOptions::color),
            Codec.FLOAT.fieldOf("alpha").forGetter(CurlyWispParticleOptions::alpha), Codec.FLOAT.fieldOf("scale").forGetter(CurlyWispParticleOptions::scale),
            Codec.INT.fieldOf("delay").forGetter(CurlyWispParticleOptions::delay), Codec.INT.fieldOf("seed").forGetter(CurlyWispParticleOptions::seed)).apply(inst, CurlyWispParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CurlyWispParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, CurlyWispParticleOptions::color, ByteBufCodecs.FLOAT,
            CurlyWispParticleOptions::alpha, ByteBufCodecs.FLOAT, CurlyWispParticleOptions::scale, ByteBufCodecs.VAR_INT, CurlyWispParticleOptions::delay, ByteBufCodecs.VAR_INT,
            CurlyWispParticleOptions::seed, CurlyWispParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.CURLY_WISP.get();
    }
}
