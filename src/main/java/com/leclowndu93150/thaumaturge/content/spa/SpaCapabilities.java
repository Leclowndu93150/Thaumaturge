package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class SpaCapabilities {
    private SpaCapabilities() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK, TTBlockEntities.SPA.get(), (be, side) -> be.getTank());
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TTBlockEntities.SPA.get(),
                (be, side) -> side == Direction.UP ? null : be.getItems());
    }
}
