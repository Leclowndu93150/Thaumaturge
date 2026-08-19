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

public record FluxSwirlParticleOptions(int color, float scale, float endScale) implements ParticleOptions {

    public static final MapCodec<FluxSwirlParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(Codec.INT.fieldOf("color").forGetter(FluxSwirlParticleOptions::color), Codec.FLOAT.fieldOf("scale").forGetter(FluxSwirlParticleOptions::scale),
                    Codec.FLOAT.fieldOf("end_scale").forGetter(FluxSwirlParticleOptions::endScale)).apply(inst, FluxSwirlParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluxSwirlParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, FluxSwirlParticleOptions::color, ByteBufCodecs.FLOAT,
            FluxSwirlParticleOptions::scale, ByteBufCodecs.FLOAT, FluxSwirlParticleOptions::endScale, FluxSwirlParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.FLUX_SWIRL.get();
    }
}
