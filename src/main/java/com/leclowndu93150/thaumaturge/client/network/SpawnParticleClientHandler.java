package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundParticleBatchPayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SpawnParticleClientHandler {
    private SpawnParticleClientHandler() {}

    public static void handle(ClientboundSpawnParticlePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;
            Vec3 at = new Vec3(payload.x(), payload.y(), payload.z());
            var options = payload.options();
            var type = options.getType();
            // Sable needs the plot coordinates to attach these particles at spawn.
            boolean staysOnDeck = type == TTParticles.PUFF.get()
                    || type == TTParticles.FLASH.get()
                    || type == TTParticles.CURLY_WISP.get()
                    || options instanceof WispyMoteParticleOptions mote
                            && mote.targetEntityId() == WispyMoteParticleOptions.NO_ENTITY;
            if (!staysOnDeck) {
                at = ClientSpaces.renderPosition(level, at);
            }
            level.addParticle(options, at.x, at.y, at.z, payload.vx(), payload.vy(), payload.vz());
        });
    }

    public static void handle(ClientboundParticleBatchPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;
            for (ClientboundParticleBatchPayload.Spawn spawn : payload.particles()) {
                Vec3 at = new Vec3(spawn.x(), spawn.y(), spawn.z());
                if (!payload.followsSubLevel()) {
                    at = ClientSpaces.renderPosition(level, at);
                }
                level.addParticle(spawn.options(), at.x, at.y, at.z, spawn.vx(), spawn.vy(), spawn.vz());
            }
        });
    }
}
