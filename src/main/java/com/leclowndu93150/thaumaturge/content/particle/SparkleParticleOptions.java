package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SparkleParticleOptions(
        int color, float scale, int delay, float decay, float gravity, int baseAge, boolean flicker)
        implements ParticleOptions {

    public static final MapCodec<SparkleParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("color").forGetter(SparkleParticleOptions::color),
                    Codec.FLOAT.fieldOf("scale").forGetter(SparkleParticleOptions::scale),
                    Codec.INT.fieldOf("delay").forGetter(SparkleParticleOptions::delay),
                    Codec.FLOAT.fieldOf("decay").forGetter(SparkleParticleOptions::decay),
                    Codec.FLOAT.fieldOf("gravity").forGetter(SparkleParticleOptions::gravity),
                    Codec.INT.fieldOf("base_age").forGetter(SparkleParticleOptions::baseAge),
                    Codec.BOOL.fieldOf("flicker").forGetter(SparkleParticleOptions::flicker))
            .apply(inst, SparkleParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SparkleParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, o) -> {
                buf.writeInt(o.color());
                buf.writeFloat(o.scale());
                buf.writeVarInt(o.delay());
                buf.writeFloat(o.decay());
                buf.writeFloat(o.gravity());
                buf.writeVarInt(o.baseAge());
                buf.writeBoolean(o.flicker());
            },
            buf -> new SparkleParticleOptions(
                    buf.readInt(),
                    buf.readFloat(),
                    buf.readVarInt(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readVarInt(),
                    buf.readBoolean()));

    @Override
    public ParticleType<?> getType() {
        return TTParticles.SPARKLE.get();
    }
}
