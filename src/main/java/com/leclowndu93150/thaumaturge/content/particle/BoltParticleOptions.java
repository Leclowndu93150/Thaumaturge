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

public record BoltParticleOptions(double targetX, double targetY, double targetZ, float r, float g, float b, float width) implements ParticleOptions {
    public static final MapCodec<BoltParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.DOUBLE.fieldOf("target_x").forGetter(BoltParticleOptions::targetX),
            Codec.DOUBLE.fieldOf("target_y").forGetter(BoltParticleOptions::targetY), Codec.DOUBLE.fieldOf("target_z").forGetter(BoltParticleOptions::targetZ),
            Codec.FLOAT.fieldOf("r").forGetter(BoltParticleOptions::r), Codec.FLOAT.fieldOf("g").forGetter(BoltParticleOptions::g), Codec.FLOAT.fieldOf("b").forGetter(BoltParticleOptions::b),
            Codec.FLOAT.fieldOf("width").forGetter(BoltParticleOptions::width)).apply(instance, BoltParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoltParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.DOUBLE, BoltParticleOptions::targetX, ByteBufCodecs.DOUBLE,
            BoltParticleOptions::targetY, ByteBufCodecs.DOUBLE, BoltParticleOptions::targetZ, ByteBufCodecs.FLOAT, BoltParticleOptions::r, ByteBufCodecs.FLOAT, BoltParticleOptions::g,
            ByteBufCodecs.FLOAT, BoltParticleOptions::b, ByteBufCodecs.FLOAT, BoltParticleOptions::width, BoltParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.BOLT.get();
    }
}
