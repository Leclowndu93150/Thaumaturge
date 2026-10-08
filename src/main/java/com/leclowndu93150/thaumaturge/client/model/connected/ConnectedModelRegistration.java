package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ConnectedModelRegistration {
    private ConnectedModelRegistration() {}

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ConnectedSheetModel.TYPE, new ConnectedModelLoader<>(ConnectedSheetModel.CODEC));
        event.register(ConnectedCornersModel.TYPE, new ConnectedModelLoader<>(ConnectedCornersModel.CODEC));
        event.register(ConnectedStairsModel.TYPE, new ConnectedModelLoader<>(ConnectedStairsModel.CODEC));
    }
}
