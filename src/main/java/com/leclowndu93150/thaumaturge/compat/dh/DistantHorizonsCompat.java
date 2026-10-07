package com.leclowndu93150.thaumaturge.compat.dh;

import com.leclowndu93150.thaumaturge.TTIds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class DistantHorizonsCompat {
    private DistantHorizonsCompat() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (!ModList.get().isLoaded(TTIds.DISTANT_HORIZONS)) {
            return;
        }
        event.enqueueWork(() -> WarpMistFogEvent.register());
    }
}
