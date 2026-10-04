package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.screen.research.ThaumonomiconBrowserScreen;
import com.leclowndu93150.thaumaturge.network.ClientboundOpenThaumonomiconPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class OpenThaumonomiconHandler {
    private OpenThaumonomiconHandler() {}

    public static void handle(ClientboundOpenThaumonomiconPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(ThaumonomiconBrowserScreen.reopen()));
    }
}
