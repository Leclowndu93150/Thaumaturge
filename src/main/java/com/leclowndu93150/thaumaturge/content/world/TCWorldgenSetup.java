package com.leclowndu93150.thaumaturge.content.world;

import com.leclowndu93150.thaumaturge.TCIds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import terrablender.api.Regions;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCWorldgenSetup {
    private TCWorldgenSetup() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Regions.register(new TCOverworldRegion(TCIds.rl("overworld")));
            Regions.register(new TCTaintedLandsRegion(TCIds.rl("tainted_lands")));
        });
    }
}
