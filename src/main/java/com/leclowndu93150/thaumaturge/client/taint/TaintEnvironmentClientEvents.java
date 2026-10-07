package com.leclowndu93150.thaumaturge.client.taint;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.FogPlanes;
import com.leclowndu93150.thaumaturge.network.ClientboundTaintEnvironmentPayload;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TaintEnvironmentClientEvents {
    private static final float FOG_FULL_PRESSURE = 0.4F;
    private static final float MAX_FOG_INTENSITY = 0.95F;
    private static final float FOG_NEAR_PLANE = 2.0F;
    private static final float FOG_FAR_PLANE = 28.0F;

    private TaintEnvironmentClientEvents() {}

    public static void handle(ClientboundTaintEnvironmentPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> TaintEnvironmentHolder.accept(payload.pressure()));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        TaintEnvironmentHolder.tick();
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float pressure = TaintEnvironmentHolder.pressure();
        if (pressure > 0.0F) {
            FogPlanes.pullToward(event.getFogData(), Mth.clamp(pressure / FOG_FULL_PRESSURE, 0.0F, MAX_FOG_INTENSITY), FOG_NEAR_PLANE, FOG_FAR_PLANE);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        TaintEnvironmentHolder.reset();
    }
}
