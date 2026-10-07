package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundSpawnParticlePayload(
        ParticleOptions options, double x, double y, double z, double vx, double vy, double vz)
        implements CustomPacketPayload {
    public static final Type<ClientboundSpawnParticlePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "spawn_particle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSpawnParticlePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        ParticleTypes.STREAM_CODEC.encode(buf, p.options());
                        buf.writeDouble(p.x());
                        buf.writeDouble(p.y());
                        buf.writeDouble(p.z());
                        buf.writeDouble(p.vx());
                        buf.writeDouble(p.vy());
                        buf.writeDouble(p.vz());
                    },
                    buf -> new ClientboundSpawnParticlePayload(
                            ParticleTypes.STREAM_CODEC.decode(buf),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble()));

    public ClientboundSpawnParticlePayload(ParticleOptions options, double x, double y, double z) {
        this(options, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
