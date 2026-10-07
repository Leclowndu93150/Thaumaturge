package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record BoreSparkleParticleOptions(int targetEntityId, double tx, double ty, double tz, float r, float g, float b)
        implements ParticleOptions {
    public static final int NO_ENTITY = -1;

    public static final MapCodec<BoreSparkleParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.INT.fieldOf("target_entity_id").forGetter(BoreSparkleParticleOptions::targetEntityId),
                    Codec.DOUBLE.fieldOf("tx").forGetter(BoreSparkleParticleOptions::tx),
                    Codec.DOUBLE.fieldOf("ty").forGetter(BoreSparkleParticleOptions::ty),
                    Codec.DOUBLE.fieldOf("tz").forGetter(BoreSparkleParticleOptions::tz),
                    Codec.FLOAT.fieldOf("r").forGetter(BoreSparkleParticleOptions::r),
                    Codec.FLOAT.fieldOf("g").forGetter(BoreSparkleParticleOptions::g),
                    Codec.FLOAT.fieldOf("b").forGetter(BoreSparkleParticleOptions::b))
            .apply(inst, BoreSparkleParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoreSparkleParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buf, o) -> {
                buf.writeVarInt(o.targetEntityId());
                buf.writeDouble(o.tx());
                buf.writeDouble(o.ty());
                buf.writeDouble(o.tz());
                buf.writeFloat(o.r());
                buf.writeFloat(o.g());
                buf.writeFloat(o.b());
            },
            buf -> new BoreSparkleParticleOptions(
                    buf.readVarInt(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat()));

    public BoreSparkleParticleOptions(double tx, double ty, double tz, float r, float g, float b) {
        this(NO_ENTITY, tx, ty, tz, r, g, b);
    }

    @Override
    public ParticleType<?> getType() {
        return TTParticles.BORE_SPARKLE.get();
    }
}
