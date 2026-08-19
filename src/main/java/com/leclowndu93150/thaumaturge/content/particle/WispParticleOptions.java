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

public record WispParticleOptions(int entityId) implements ParticleOptions {
    public static final int NO_ENTITY = -1;

    public static final MapCodec<WispParticleOptions> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Codec.INT.fieldOf("entity_id").forGetter(WispParticleOptions::entityId)).apply(instance, WispParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WispParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, WispParticleOptions::entityId, WispParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.WISP.get();
    }
}
