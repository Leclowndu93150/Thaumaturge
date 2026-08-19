package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SpawnParticleClientHandler {
    private SpawnParticleClientHandler() {}

    public static void handle(ClientboundSpawnParticlePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null)
                return;
            level.addParticle(payload.options(), payload.x(), payload.y(), payload.z(), payload.vx(), payload.vy(), payload.vz());
        });
    }
}
