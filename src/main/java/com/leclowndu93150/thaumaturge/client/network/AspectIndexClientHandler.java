package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumaturge.network.ClientboundAspectIndexPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AspectIndexClientHandler {
    private AspectIndexClientHandler() {}

    public static void handle(ClientboundAspectIndexPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!Minecraft.getInstance().hasSingleplayerServer()) {
                AspectIndexHolder.set(payload.index());
            }
        });
    }
}
