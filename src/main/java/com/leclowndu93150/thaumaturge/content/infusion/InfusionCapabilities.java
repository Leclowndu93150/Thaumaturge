package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class InfusionCapabilities {
    private InfusionCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AspectCapabilities.CONTAINER, TCBlockEntities.INFUSION_MATRIX.get(), (be, side) -> be);
    }
}
