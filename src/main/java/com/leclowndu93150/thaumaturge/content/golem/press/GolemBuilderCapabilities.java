package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class GolemBuilderCapabilities {
    private GolemBuilderCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                EssentiaCapabilities.TRANSPORT,
                TTBlockEntities.GOLEM_BUILDER.get(),
                (be, side) -> side == null || be.isConnectable(side) ? be : null);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, TTBlockEntities.GOLEM_BUILDER.get(), (be, side) -> be.output());
    }
}
