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

public record ShieldSparkParticleOptions(int color, float alpha, float scale, int age, int delay, boolean additive)
        implements ParticleOptions {

    public static final MapCodec<ShieldSparkParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(ShieldSparkParticleOptions::color),
                    Codec.FLOAT.fieldOf("alpha").forGetter(ShieldSparkParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(ShieldSparkParticleOptions::scale),
                    Codec.INT.fieldOf("age").forGetter(ShieldSparkParticleOptions::age),
                    Codec.INT.optionalFieldOf("delay", 0).forGetter(ShieldSparkParticleOptions::delay),
                    Codec.BOOL.fieldOf("additive").forGetter(ShieldSparkParticleOptions::additive))
            .apply(inst, ShieldSparkParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShieldSparkParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    ShieldSparkParticleOptions::color,
                    ByteBufCodecs.FLOAT,
                    ShieldSparkParticleOptions::alpha,
                    ByteBufCodecs.FLOAT,
                    ShieldSparkParticleOptions::scale,
                    ByteBufCodecs.VAR_INT,
                    ShieldSparkParticleOptions::age,
                    ByteBufCodecs.VAR_INT,
                    ShieldSparkParticleOptions::delay,
                    ByteBufCodecs.BOOL,
                    ShieldSparkParticleOptions::additive,
                    ShieldSparkParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.SHIELD_SPARK.get();
    }
}
