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

public record BlockRunesParticleOptions(float r, float g, float b, int duration, float gravity, boolean variant) implements ParticleOptions {

    public static final MapCodec<BlockRunesParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
            .group(Codec.FLOAT.fieldOf("r").forGetter(BlockRunesParticleOptions::r), Codec.FLOAT.fieldOf("g").forGetter(BlockRunesParticleOptions::g),
                    Codec.FLOAT.fieldOf("b").forGetter(BlockRunesParticleOptions::b), Codec.INT.fieldOf("duration").forGetter(BlockRunesParticleOptions::duration),
                    Codec.FLOAT.fieldOf("gravity").forGetter(BlockRunesParticleOptions::gravity), Codec.BOOL.fieldOf("variant").forGetter(BlockRunesParticleOptions::variant))
            .apply(inst, BlockRunesParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockRunesParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, BlockRunesParticleOptions::r, ByteBufCodecs.FLOAT,
            BlockRunesParticleOptions::g, ByteBufCodecs.FLOAT, BlockRunesParticleOptions::b, ByteBufCodecs.VAR_INT, BlockRunesParticleOptions::duration, ByteBufCodecs.FLOAT,
            BlockRunesParticleOptions::gravity, ByteBufCodecs.BOOL, BlockRunesParticleOptions::variant, BlockRunesParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.BLOCK_RUNES.get();
    }
}
