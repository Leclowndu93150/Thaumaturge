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

public record CrackShardParticleOptions(int color, int variant, float scale, int age) implements ParticleOptions {

    public static final MapCodec<CrackShardParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
            .group(Codec.INT.fieldOf("color").forGetter(CrackShardParticleOptions::color), Codec.INT.fieldOf("variant").forGetter(CrackShardParticleOptions::variant),
                    Codec.FLOAT.fieldOf("scale").forGetter(CrackShardParticleOptions::scale), Codec.INT.fieldOf("age").forGetter(CrackShardParticleOptions::age))
            .apply(inst, CrackShardParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrackShardParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, CrackShardParticleOptions::color, ByteBufCodecs.VAR_INT,
            CrackShardParticleOptions::variant, ByteBufCodecs.FLOAT, CrackShardParticleOptions::scale, ByteBufCodecs.VAR_INT, CrackShardParticleOptions::age, CrackShardParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.CRACK_SHARD.get();
    }
}
