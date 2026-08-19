package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record EssentiaDropParticleOptions(int color, float alpha) implements ParticleOptions {
    public static final MapCodec<EssentiaDropParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Codec.INT.fieldOf("color").forGetter(EssentiaDropParticleOptions::color), Codec.FLOAT.fieldOf("alpha").forGetter(EssentiaDropParticleOptions::alpha))
                    .apply(instance, EssentiaDropParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EssentiaDropParticleOptions> STREAM_CODEC = StreamCodec.of((buf, data) -> {
        buf.writeInt(data.color);
        buf.writeFloat(data.alpha);
    }, buf -> new EssentiaDropParticleOptions(buf.readInt(), buf.readFloat()));

    @Override
    public ParticleType<?> getType() {
        return TCParticles.ESSENTIA_DROP.get();
    }
}
