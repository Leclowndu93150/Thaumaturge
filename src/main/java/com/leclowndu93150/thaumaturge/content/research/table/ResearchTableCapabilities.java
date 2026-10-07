package com.leclowndu93150.thaumaturge.content.research.table;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ResearchTableCapabilities {
    private ResearchTableCapabilities() {}

    @SubscribeEvent
    public static void onRegister(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                TTBlockEntities.RESEARCH_TABLE.get(),
                (blockEntity, side) -> blockEntity.items());
    }
}
