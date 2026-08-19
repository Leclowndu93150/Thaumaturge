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

public record BoreSparkleParticleOptions(int targetEntityId, double tx, double ty, double tz, float r, float g, float b) implements ParticleOptions {
    public static final int NO_ENTITY = -1;

    public static final MapCodec<BoreSparkleParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(Codec.INT.fieldOf("target_entity_id").forGetter(BoreSparkleParticleOptions::targetEntityId),
                    Codec.DOUBLE.fieldOf("tx").forGetter(BoreSparkleParticleOptions::tx), Codec.DOUBLE.fieldOf("ty").forGetter(BoreSparkleParticleOptions::ty),
                    Codec.DOUBLE.fieldOf("tz").forGetter(BoreSparkleParticleOptions::tz), Codec.FLOAT.fieldOf("r").forGetter(BoreSparkleParticleOptions::r),
                    Codec.FLOAT.fieldOf("g").forGetter(BoreSparkleParticleOptions::g), Codec.FLOAT.fieldOf("b").forGetter(BoreSparkleParticleOptions::b)).apply(inst, BoreSparkleParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoreSparkleParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, BoreSparkleParticleOptions::targetEntityId,
            ByteBufCodecs.DOUBLE, BoreSparkleParticleOptions::tx, ByteBufCodecs.DOUBLE, BoreSparkleParticleOptions::ty, ByteBufCodecs.DOUBLE, BoreSparkleParticleOptions::tz, ByteBufCodecs.FLOAT,
            BoreSparkleParticleOptions::r, ByteBufCodecs.FLOAT, BoreSparkleParticleOptions::g, ByteBufCodecs.FLOAT, BoreSparkleParticleOptions::b, BoreSparkleParticleOptions::new);

    public BoreSparkleParticleOptions(double tx, double ty, double tz, float r, float g, float b) {
        this(NO_ENTITY, tx, ty, tz, r, g, b);
    }

    @Override
    public ParticleType<?> getType() {
        return TCParticles.BORE_SPARKLE.get();
    }
}
