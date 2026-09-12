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

/** Client-local particle bound to the entity it visually represents. */
public record TaintSwarmParticleOptions(int entityId) implements ParticleOptions {
    public static final MapCodec<TaintSwarmParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(Codec.INT.fieldOf("entity_id").forGetter(TaintSwarmParticleOptions::entityId))
                    .apply(inst, TaintSwarmParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TaintSwarmParticleOptions> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, TaintSwarmParticleOptions::entityId, TaintSwarmParticleOptions::new);

    @Override
    public ParticleType<?> getType() {
        return TCParticles.TAINT_SWARM.get();
    }
}
