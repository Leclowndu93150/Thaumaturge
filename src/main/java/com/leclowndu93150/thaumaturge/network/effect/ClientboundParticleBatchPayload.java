package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundParticleBatchPayload(List<Spawn> particles, boolean followsSubLevel)
        implements CustomPacketPayload {
    public static final int MAX_PARTICLES = 2048;

    public static final Type<ClientboundParticleBatchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "particle_batch"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Spawn> SPAWN_CODEC = StreamCodec.of(
            (buf, spawn) -> {
                ParticleTypes.STREAM_CODEC.encode(buf, spawn.options());
                buf.writeDouble(spawn.x());
                buf.writeDouble(spawn.y());
                buf.writeDouble(spawn.z());
                buf.writeDouble(spawn.vx());
                buf.writeDouble(spawn.vy());
                buf.writeDouble(spawn.vz());
            },
            buf -> new Spawn(
                    ParticleTypes.STREAM_CODEC.decode(buf),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundParticleBatchPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeBoolean(payload.followsSubLevel());
                        buf.writeVarInt(payload.particles().size());
                        payload.particles().forEach(spawn -> SPAWN_CODEC.encode(buf, spawn));
                    },
                    buf -> {
                        boolean followsSubLevel = buf.readBoolean();
                        int count = buf.readVarInt();
                        if (count < 0 || count > MAX_PARTICLES) {
                            throw new DecoderException("Invalid particle batch size: " + count);
                        }
                        List<Spawn> particles = new ArrayList<>(count);
                        for (int i = 0; i < count; i++) {
                            particles.add(SPAWN_CODEC.decode(buf));
                        }
                        return new ClientboundParticleBatchPayload(particles, followsSubLevel);
                    });

    public ClientboundParticleBatchPayload {
        particles = List.copyOf(particles);
        if (particles.size() > MAX_PARTICLES) {
            throw new IllegalArgumentException("Particle batch exceeds " + MAX_PARTICLES);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Spawn(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz) {}
}
