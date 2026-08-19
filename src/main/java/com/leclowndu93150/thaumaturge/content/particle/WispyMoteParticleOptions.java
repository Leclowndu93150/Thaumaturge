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

public record WispyMoteParticleOptions(int color, int age, float gravity, int targetEntityId) implements ParticleOptions {

    public static final int NO_ENTITY = -1;

    public static final MapCodec<WispyMoteParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
            .group(Codec.INT.fieldOf("color").forGetter(WispyMoteParticleOptions::color), Codec.INT.fieldOf("age").forGetter(WispyMoteParticleOptions::age),
                    Codec.FLOAT.fieldOf("gravity").forGetter(WispyMoteParticleOptions::gravity), Codec.INT.optionalFieldOf("target", NO_ENTITY).forGetter(WispyMoteParticleOptions::targetEntityId))
            .apply(inst, WispyMoteParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WispyMoteParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, WispyMoteParticleOptions::color, ByteBufCodecs.VAR_INT,
            WispyMoteParticleOptions::age, ByteBufCodecs.FLOAT, WispyMoteParticleOptions::gravity, ByteBufCodecs.VAR_INT, WispyMoteParticleOptions::targetEntityId, WispyMoteParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.WISPY_MOTE.get();
    }
}
