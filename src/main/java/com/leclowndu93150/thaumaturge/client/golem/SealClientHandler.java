package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.seals.ClientSealHolder;
import com.leclowndu93150.thaumaturge.network.ClientboundSealPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class SealClientHandler {
    private SealClientHandler() {}

    public static void handle(ClientboundSealPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.seal().isPresent()) {
                ClientSealHolder.put(payload.seal().get());
            } else {
                ClientSealHolder.remove(payload.pos());
            }
        });
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSealHolder.clear();
    }
}
