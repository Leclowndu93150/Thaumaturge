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

public record SlashParticleOptions(int duration, float yaw, float pitch, float roll) implements ParticleOptions {

    public static final MapCodec<SlashParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst
                    .group(Codec.INT.fieldOf("duration").forGetter(SlashParticleOptions::duration), Codec.FLOAT.fieldOf("yaw").forGetter(SlashParticleOptions::yaw),
                            Codec.FLOAT.fieldOf("pitch").forGetter(SlashParticleOptions::pitch), Codec.FLOAT.fieldOf("roll").forGetter(SlashParticleOptions::roll))
                    .apply(inst, SlashParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlashParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, SlashParticleOptions::duration, ByteBufCodecs.FLOAT,
            SlashParticleOptions::yaw, ByteBufCodecs.FLOAT, SlashParticleOptions::pitch, ByteBufCodecs.FLOAT, SlashParticleOptions::roll, SlashParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.SLASH.get();
    }
}
