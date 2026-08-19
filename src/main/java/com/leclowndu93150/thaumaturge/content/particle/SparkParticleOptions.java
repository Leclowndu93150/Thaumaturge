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

public record SparkParticleOptions(int color, float alpha, float scale) implements ParticleOptions {

    public static final MapCodec<SparkParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(Codec.INT.fieldOf("color").forGetter(SparkParticleOptions::color),
            Codec.FLOAT.fieldOf("alpha").forGetter(SparkParticleOptions::alpha), Codec.FLOAT.fieldOf("scale").forGetter(SparkParticleOptions::scale)).apply(inst, SparkParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SparkParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, SparkParticleOptions::color, ByteBufCodecs.FLOAT,
            SparkParticleOptions::alpha, ByteBufCodecs.FLOAT, SparkParticleOptions::scale, SparkParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.SPARK.get();
    }
}
