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

public record LightningFlashParticleOptions(int color, float alpha, float scale) implements ParticleOptions {

    public static final MapCodec<LightningFlashParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(Codec.INT.fieldOf("color").forGetter(LightningFlashParticleOptions::color), Codec.FLOAT.fieldOf("alpha").forGetter(LightningFlashParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(LightningFlashParticleOptions::scale)).apply(inst, LightningFlashParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LightningFlashParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, LightningFlashParticleOptions::color,
            ByteBufCodecs.FLOAT, LightningFlashParticleOptions::alpha, ByteBufCodecs.FLOAT, LightningFlashParticleOptions::scale, LightningFlashParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.LIGHTNING_FLASH.get();
    }
}
