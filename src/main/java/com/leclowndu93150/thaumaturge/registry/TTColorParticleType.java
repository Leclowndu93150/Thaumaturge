package com.leclowndu93150.thaumaturge.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class TTColorParticleType extends ParticleType<ColorParticleOption> {

    public TTColorParticleType() {
        super(false);
    }

    @Override
    public MapCodec<ColorParticleOption> codec() {
        return ColorParticleOption.codec(this);
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
        return ColorParticleOption.streamCodec(this);
    }
}
