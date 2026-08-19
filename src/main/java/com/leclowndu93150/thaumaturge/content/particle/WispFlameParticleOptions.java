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

public record WispFlameParticleOptions(int color, float alpha, float scale, float endScale, int delay) implements ParticleOptions {

    public static final MapCodec<WispFlameParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(Codec.INT.fieldOf("color").forGetter(WispFlameParticleOptions::color), Codec.FLOAT.fieldOf("alpha").forGetter(WispFlameParticleOptions::alpha),
                    Codec.FLOAT.fieldOf("scale").forGetter(WispFlameParticleOptions::scale), Codec.FLOAT.fieldOf("end_scale").forGetter(WispFlameParticleOptions::endScale),
                    Codec.INT.fieldOf("delay").forGetter(WispFlameParticleOptions::delay)).apply(inst, WispFlameParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WispFlameParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, WispFlameParticleOptions::color, ByteBufCodecs.FLOAT,
            WispFlameParticleOptions::alpha, ByteBufCodecs.FLOAT, WispFlameParticleOptions::scale, ByteBufCodecs.FLOAT, WispFlameParticleOptions::endScale, ByteBufCodecs.VAR_INT,
            WispFlameParticleOptions::delay, WispFlameParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.WISP_FLAME.get();
    }
}
