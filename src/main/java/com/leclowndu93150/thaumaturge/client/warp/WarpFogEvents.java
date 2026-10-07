package com.leclowndu93150.thaumaturge.client.warp;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
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
    private static final float MAX_SANE_FOG_PLANE = 4096.0F;
    private static final float FALLBACK_FAR_PLANE = 512.0F;
    private static final float FALLBACK_NEAR_PLANE = 256.0F;

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
        float intensity = WarpFogState.intensity();
        event.setFarPlaneDistance(
                Mth.lerp(intensity, usablePlane(event.getFarPlaneDistance(), FALLBACK_FAR_PLANE), MIST_FAR_PLANE));
        event.setNearPlaneDistance(
                Mth.lerp(intensity, usablePlane(event.getNearPlaneDistance(), FALLBACK_NEAR_PLANE), MIST_NEAR_PLANE));
        event.setCanceled(true);
    }

    private static float usablePlane(float plane, float fallback) {
        return plane > MAX_SANE_FOG_PLANE ? fallback : plane;
    }
}
