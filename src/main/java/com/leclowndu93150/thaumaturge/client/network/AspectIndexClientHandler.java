package com.leclowndu93150.thaumaturge.client.network;

import net.neoforged.fml.ModList;
import com.leclowndu93150.thaumaturge.compat.jei.AspectJeiSync;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumaturge.network.ClientboundAspectIndexPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AspectIndexClientHandler {
    private static final String JEI_MOD_ID = "jei";

    private AspectIndexClientHandler() {}

    public static void handle(ClientboundAspectIndexPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!Minecraft.getInstance().hasSingleplayerServer()) {
                AspectIndexHolder.set(payload.index());
                if (ModList.get().isLoaded(JEI_MOD_ID)) {
                    AspectJeiSync.rebuildAspectStackPages();
                }
            }
        });
    }
}
