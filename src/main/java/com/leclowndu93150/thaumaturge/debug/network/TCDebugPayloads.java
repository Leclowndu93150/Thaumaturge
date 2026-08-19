package com.leclowndu93150.thaumaturge.debug.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCDebugPayloads {

    private TCDebugPayloads() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(TCIds.MODID + "_debug");
        // Debug Packets
        registrar.playToClient(ClientboundToggleRaycastDebugPayload.TYPE, ClientboundToggleRaycastDebugPayload.STREAM_CODEC, (payload, context) -> ToggleRaycastDebugHandler.handle(payload, context));
        registrar.playToClient(ClientboundRaycastDebugPayload.TYPE, ClientboundRaycastDebugPayload.STREAM_CODEC, (payload, context) -> ToggleRaycastDebugHandler.handleServerRaycast(payload, context));
    }
}
