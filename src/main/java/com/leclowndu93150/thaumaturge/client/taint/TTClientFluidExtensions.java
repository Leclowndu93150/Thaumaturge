package com.leclowndu93150.thaumaturge.client.taint;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTFluidTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTClientFluidExtensions {
    private TTClientFluidExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new FluxGooClientExtensions(), TTFluidTypes.FLUX_GOO.get());
        event.registerFluidType(new PurifyingClientExtensions(), TTFluidTypes.PURIFYING.get());
        event.registerFluidType(new LiquidDeathClientExtensions(), TTFluidTypes.LIQUID_DEATH.get());
    }
}
