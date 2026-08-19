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

public record TaintFumeParticleOptions(int color, float scale) implements ParticleOptions {

    public static final int RANDOM_COLOR = -1;

    public static final MapCodec<TaintFumeParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(Codec.INT.fieldOf("color").forGetter(TaintFumeParticleOptions::color), Codec.FLOAT.fieldOf("scale").forGetter(TaintFumeParticleOptions::scale)).apply(inst,
                    TaintFumeParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TaintFumeParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, TaintFumeParticleOptions::color, ByteBufCodecs.FLOAT,
            TaintFumeParticleOptions::scale, TaintFumeParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.TAINT_FUME.get();
    }
}
