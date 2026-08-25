package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record WardFlashParticleOptions(Direction face, float hitX, float hitY, float hitZ) implements ParticleOptions {

    public static final MapCodec<WardFlashParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Direction.CODEC.fieldOf("face").forGetter(WardFlashParticleOptions::face),
                    Codec.FLOAT.fieldOf("hit_x").forGetter(WardFlashParticleOptions::hitX),
                    Codec.FLOAT.fieldOf("hit_y").forGetter(WardFlashParticleOptions::hitY),
                    Codec.FLOAT.fieldOf("hit_z").forGetter(WardFlashParticleOptions::hitZ))
            .apply(inst, WardFlashParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WardFlashParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    Direction.STREAM_CODEC,
                    WardFlashParticleOptions::face,
                    ByteBufCodecs.FLOAT,
                    WardFlashParticleOptions::hitX,
                    ByteBufCodecs.FLOAT,
                    WardFlashParticleOptions::hitY,
                    ByteBufCodecs.FLOAT,
                    WardFlashParticleOptions::hitZ,
                    WardFlashParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.WARD_FLASH.get();
    }
}
