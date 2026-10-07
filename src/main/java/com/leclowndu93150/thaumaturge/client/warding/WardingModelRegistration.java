package com.leclowndu93150.thaumaturge.client.warding;

import com.leclowndu93150.thaumaturge.TTIds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class WardingModelRegistration {
    private WardingModelRegistration() {}

    @SubscribeEvent
    public static void onRegisterBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(WardedGlassUnbakedModel.MODEL_TYPE, WardedGlassUnbakedModel.CODEC);
    }
}
