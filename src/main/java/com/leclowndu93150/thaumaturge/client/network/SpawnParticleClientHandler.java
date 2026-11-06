package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
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
            Vec3 at = ClientSpaces.renderPosition(level, new Vec3(payload.x(), payload.y(), payload.z()));
            level.addParticle(payload.options(), at.x, at.y, at.z, payload.vx(), payload.vy(), payload.vz());
        });
    }
}
