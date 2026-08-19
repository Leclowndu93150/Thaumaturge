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

public record NitorCoreParticleOptions(float r, float g, float b) implements ParticleOptions {
    public static final MapCodec<NitorCoreParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(Codec.FLOAT.fieldOf("r").forGetter(NitorCoreParticleOptions::r),
            Codec.FLOAT.fieldOf("g").forGetter(NitorCoreParticleOptions::g), Codec.FLOAT.fieldOf("b").forGetter(NitorCoreParticleOptions::b)).apply(inst, NitorCoreParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NitorCoreParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, NitorCoreParticleOptions::r, ByteBufCodecs.FLOAT,
            NitorCoreParticleOptions::g, ByteBufCodecs.FLOAT, NitorCoreParticleOptions::b, NitorCoreParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.NITOR_CORE.get();
    }
}
