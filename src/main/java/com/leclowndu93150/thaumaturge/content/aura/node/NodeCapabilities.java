package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayCapabilities;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class NodeCapabilities {
    private NodeCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.NODE.get(), (node, side) -> node);
        event.registerBlockEntity(
                VisRelayCapabilities.SOURCE, TTBlockEntities.NODE.get(), (node, context) -> node.relaySource());
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.JAR_NODE.get(), (node, side) -> node);
    }
}
