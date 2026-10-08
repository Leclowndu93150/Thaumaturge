package com.leclowndu93150.thaumaturge.compat.ftblibrary;

import com.leclowndu93150.thaumaturge.TTIds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class FtbLibraryCompat {
    private FtbLibraryCompat() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (!ModList.get().isLoaded(TTIds.FTB_LIBRARY)) {
            return;
        }
        event.enqueueWork(FtbLibrarySidebar::hideOnFullScreenMenus);
    }
}
