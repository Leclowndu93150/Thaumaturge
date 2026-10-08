package com.leclowndu93150.thaumaturge.client;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.aura.ClientAuraCache;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.manager.FXManagerRegistry;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayCache;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ClientSessionReset {
    private ClientSessionReset() {}

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            FXManagerRegistry.clearAll();
            LateWorldRenderQueue.clear();
            ClientAuraCache.clear();
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FXManagerRegistry.clearAll();
        LateWorldRenderQueue.clear();
        ClientAuraCache.clear();
        RecipeDisplayCache.clear();
    }
}
