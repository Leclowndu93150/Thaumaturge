package com.leclowndu93150.thaumaturge.client.taint;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = TCIds.MODID)
public final class TaintEnvironmentClientEvents {
    private static final int FUME_COLOR = ARGB32.color(0xD0, 0x75, 0x18, 0x91);

    private TaintEnvironmentClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        TaintEnvironmentClientState.tick();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()) {
            return;
        }
        float pressure = TaintEnvironmentClientState.pressure();
        if (pressure < 0.3F || minecraft.level.random.nextFloat() > pressure * 0.12F) {
            return;
        }
        double x = minecraft.player.getX() + minecraft.level.random.nextInt(17) - 8;
        double y = minecraft.player.getY() + minecraft.level.random.nextInt(7) - 2;
        double z = minecraft.player.getZ() + minecraft.level.random.nextInt(17) - 8;
        minecraft.level.addParticle(new TaintFumeParticleOptions(FUME_COLOR, 0.7F), x, y, z, 0.0, 0.01, 0.0);
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.RenderFog event) {
        float pressure = TaintEnvironmentClientState.pressure();
        if (pressure < 0.3F) {
            return;
        }
        float intensity = Mth.clamp((pressure - 0.3F) / 0.7F, 0.0F, 0.75F);
        event.setFarPlaneDistance(Mth.lerp(intensity, event.getFarPlaneDistance(), 28.0F));
        event.setNearPlaneDistance(Mth.lerp(intensity, event.getNearPlaneDistance(), 2.0F));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        TaintEnvironmentClientState.reset();
    }
}
