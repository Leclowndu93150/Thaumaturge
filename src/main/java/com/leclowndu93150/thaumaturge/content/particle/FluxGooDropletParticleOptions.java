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

public record FluxGooDropletParticleOptions(int color, float alpha, int lifetime) implements ParticleOptions {
    public static final MapCodec<FluxGooDropletParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(Codec.INT.fieldOf("color").forGetter(FluxGooDropletParticleOptions::color), Codec.FLOAT.fieldOf("alpha").forGetter(FluxGooDropletParticleOptions::alpha),
                    Codec.INT.fieldOf("lifetime").forGetter(FluxGooDropletParticleOptions::lifetime)).apply(inst, FluxGooDropletParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluxGooDropletParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, FluxGooDropletParticleOptions::color,
            ByteBufCodecs.FLOAT, FluxGooDropletParticleOptions::alpha, ByteBufCodecs.VAR_INT, FluxGooDropletParticleOptions::lifetime, FluxGooDropletParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.FLUX_GOO_DROPLET.get();
    }
}
