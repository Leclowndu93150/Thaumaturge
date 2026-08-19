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

public record SparkleParticleOptions(int color, float scale, int delay, float decay, float gravity, int baseAge, boolean flicker) implements ParticleOptions {

    public static final MapCodec<SparkleParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(Codec.INT.fieldOf("color").forGetter(SparkleParticleOptions::color), Codec.FLOAT.fieldOf("scale").forGetter(SparkleParticleOptions::scale),
                    Codec.INT.fieldOf("delay").forGetter(SparkleParticleOptions::delay), Codec.FLOAT.fieldOf("decay").forGetter(SparkleParticleOptions::decay),
                    Codec.FLOAT.fieldOf("gravity").forGetter(SparkleParticleOptions::gravity), Codec.INT.fieldOf("base_age").forGetter(SparkleParticleOptions::baseAge),
                    Codec.BOOL.fieldOf("flicker").forGetter(SparkleParticleOptions::flicker)).apply(inst, SparkleParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SparkleParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, SparkleParticleOptions::color, ByteBufCodecs.FLOAT,
            SparkleParticleOptions::scale, ByteBufCodecs.VAR_INT, SparkleParticleOptions::delay, ByteBufCodecs.FLOAT, SparkleParticleOptions::decay, ByteBufCodecs.FLOAT,
            SparkleParticleOptions::gravity, ByteBufCodecs.VAR_INT, SparkleParticleOptions::baseAge, ByteBufCodecs.BOOL, SparkleParticleOptions::flicker, SparkleParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.SPARKLE.get();
    }
}
