package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TubeCapabilities {
    private TubeCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE_VALVE.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE_RESTRICT.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE_FILTER.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE_ONEWAY.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TTBlockEntities.TUBE_BUFFER.get(), (be, side) -> be);
        event.registerBlockEntity(
                EssentiaCapabilities.ASPECT_QUERY, TTBlockEntities.TUBE_FILTER.get(), (be, side) -> be);
    }
}
