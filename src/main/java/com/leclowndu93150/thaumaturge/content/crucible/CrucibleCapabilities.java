package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class CrucibleCapabilities {
    private CrucibleCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AspectCapabilities.CONTAINER, TTBlockEntities.CRUCIBLE.get(), (be, side) -> be);

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK, TTBlockEntities.CRUCIBLE.get(), (be, side) -> be.getTank());
    }
}
