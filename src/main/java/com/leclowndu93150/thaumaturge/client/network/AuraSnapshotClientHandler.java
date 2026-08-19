package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.aura.ClientAuraCache;
import com.leclowndu93150.thaumaturge.network.ClientboundAuraSnapshotPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AuraSnapshotClientHandler {
    private AuraSnapshotClientHandler() {}

    public static void handle(ClientboundAuraSnapshotPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientAuraCache.put(new ChunkPos(payload.chunkX(), payload.chunkZ()), payload.base(), payload.vis(), payload.flux()));
    }
}
