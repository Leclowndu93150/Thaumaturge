package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundSpawnParticlePayload(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz) implements CustomPacketPayload {
    public static final Type<ClientboundSpawnParticlePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "spawn_particle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSpawnParticlePayload> STREAM_CODEC = StreamCodec.composite(ParticleTypes.STREAM_CODEC, ClientboundSpawnParticlePayload::options,
            ByteBufCodecs.DOUBLE, ClientboundSpawnParticlePayload::x, ByteBufCodecs.DOUBLE, ClientboundSpawnParticlePayload::y, ByteBufCodecs.DOUBLE, ClientboundSpawnParticlePayload::z,
            ByteBufCodecs.DOUBLE, ClientboundSpawnParticlePayload::vx, ByteBufCodecs.DOUBLE, ClientboundSpawnParticlePayload::vy, ByteBufCodecs.DOUBLE, ClientboundSpawnParticlePayload::vz,
            ClientboundSpawnParticlePayload::new);

    public ClientboundSpawnParticlePayload(ParticleOptions options, double x, double y, double z) {
        this(options, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
