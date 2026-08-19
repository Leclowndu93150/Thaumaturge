package com.leclowndu93150.thaumaturge.content.particle;

import com.leclowndu93150.thaumaturge.registry.TCParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record RiftShardParticleOptions(float scale) implements ParticleOptions {

    public static final MapCodec<RiftShardParticleOptions> CODEC = Codec.FLOAT.xmap(RiftShardParticleOptions::new, RiftShardParticleOptions::scale).fieldOf("scale");

    public static final StreamCodec<ByteBuf, RiftShardParticleOptions> STREAM_CODEC = ByteBufCodecs.FLOAT.map(RiftShardParticleOptions::new, RiftShardParticleOptions::scale);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.RIFT_SHARD.get();
    }
}
