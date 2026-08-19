package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record VisSparkleParticleOptions(double tx, double ty, double tz, int color) implements ParticleOptions {
    public static final int DEFAULT_COLOR = -1;

    public VisSparkleParticleOptions(double tx, double ty, double tz) {
        this(tx, ty, tz, DEFAULT_COLOR);
    }

    public static final MapCodec<VisSparkleParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.DOUBLE.fieldOf("tx").forGetter(VisSparkleParticleOptions::tx), Codec.DOUBLE.fieldOf("ty").forGetter(VisSparkleParticleOptions::ty),
                    Codec.DOUBLE.fieldOf("tz").forGetter(VisSparkleParticleOptions::tz), Codec.INT.optionalFieldOf("color", DEFAULT_COLOR).forGetter(VisSparkleParticleOptions::color))
            .apply(instance, VisSparkleParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VisSparkleParticleOptions> STREAM_CODEC = StreamCodec.of((buf, data) -> {
        buf.writeDouble(data.tx);
        buf.writeDouble(data.ty);
        buf.writeDouble(data.tz);
        buf.writeInt(data.color);
    }, buf -> new VisSparkleParticleOptions(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt()));

    @Override
    public ParticleType<?> getType() {
        return TCParticles.VIS_SPARKLE.get();
    }
}
