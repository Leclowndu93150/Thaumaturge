package com.leclowndu93150.thaumaturge.client.warp;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.FogPlanes;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = TTIds.MODID)
public final class WarpFogEvents {
    private static final float MIST_FAR_PLANE = 12.0F;
    private static final float MIST_NEAR_PLANE = 2.0F;

    private WarpFogEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof LocalPlayer) {
            WarpFogState.tick();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        WarpFogState.reset();
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!WarpFogState.active()) {
            return;
        }
        FogPlanes.pullToward(event.getFogData(), WarpFogState.intensity(), MIST_NEAR_PLANE, MIST_FAR_PLANE);
    }
}
