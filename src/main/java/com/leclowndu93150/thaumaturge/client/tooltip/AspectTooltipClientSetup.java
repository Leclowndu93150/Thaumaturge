package com.leclowndu93150.thaumaturge.client.tooltip;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectChipsTooltip;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class AspectTooltipClientSetup {
    private AspectTooltipClientSetup() {}

    @SubscribeEvent
    public static void onRegisterTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AspectChipsTooltip.class, AspectChipsClientTooltip::new);
    }
}
