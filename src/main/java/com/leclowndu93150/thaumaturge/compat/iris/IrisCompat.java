package com.leclowndu93150.thaumaturge.compat.iris;

import com.leclowndu93150.thaumaturge.TCIds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class IrisCompat {
    private IrisCompat() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (!ModList.get().isLoaded(TCIds.IRIS)) {
            return;
        }
        event.enqueueWork(() -> IrisPipelineBinding.bindModPipelines());
    }
}
