package com.leclowndu93150.thaumaturge.client.eldritch;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.network.ClientboundReliquaryViewPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ReliquaryViewClientHandler {
    private ReliquaryViewClientHandler() {}

    public static void handle(ClientboundReliquaryViewPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ReliquaryViewHolder.put(payload.pos(), payload.view()));
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ReliquaryViewHolder.clear();
    }

    @SubscribeEvent
    public static void onRespawn(ClientPlayerNetworkEvent.Clone event) {
        ReliquaryViewHolder.clear();
    }
}
