package com.leclowndu93150.thaumaturge.compat.iris;

import com.leclowndu93150.thaumaturge.TTIds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class IrisCompat {
    private IrisCompat() {}

    public static boolean shaderPackInUse() {
        return ModList.get().isLoaded(TTIds.IRIS) && IrisPipelineBinding.shaderPackInUse();
    }

    public static boolean isSolidHandPass() {
        return ModList.get().isLoaded(TTIds.IRIS) && IrisHandPass.isSolidHandPass();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (!ModList.get().isLoaded(TTIds.IRIS)) {
            return;
        }
        event.enqueueWork(() -> IrisPipelineBinding.bindModPipelines());
    }
}
