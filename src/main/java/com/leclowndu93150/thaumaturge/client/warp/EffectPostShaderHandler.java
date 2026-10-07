package com.leclowndu93150.thaumaturge.client.warp;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class EffectPostShaderHandler {
    private static final ResourceLocation DEATH_GAZE_SHADER = TTIds.rl("shaders/post/death_gaze.json");
    private static final ResourceLocation BLURRED_VISION_SHADER = TTIds.rl("shaders/post/blurred_vision.json");
    private static final ResourceLocation UNNATURAL_HUNGER_SHADER = TTIds.rl("shaders/post/unnatural_hunger.json");
    private static final ResourceLocation SUN_SCORNED_SHADER = TTIds.rl("shaders/post/sun_scorned.json");

    private static @Nullable ResourceLocation active;

    private EffectPostShaderHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.getCameraEntity() != player) {
            active = null;
            return;
        }
        ResourceLocation desired = desiredShader(player);
        PostChain current = mc.gameRenderer.currentEffect();
        if (desired == active
                && (desired == null || (current != null && desired.toString().equals(current.getName())))) {
            return;
        }
        if (desired == null) {
            active = null;
            mc.gameRenderer.checkEntityPostEffect(mc.getCameraEntity());
            return;
        }
        active = desired;
        mc.gameRenderer.loadEffect(desired);
    }

    private static @Nullable ResourceLocation desiredShader(LocalPlayer player) {
        if (player.hasEffect(TTMobEffects.DEATH_GAZE)) {
            return DEATH_GAZE_SHADER;
        }
        if (player.hasEffect(TTMobEffects.BLURRED_VISION)) {
            return BLURRED_VISION_SHADER;
        }
        if (player.hasEffect(TTMobEffects.UNNATURAL_HUNGER)) {
            return UNNATURAL_HUNGER_SHADER;
        }
        if (player.hasEffect(TTMobEffects.SUN_SCORNED)) {
            return SUN_SCORNED_SHADER;
        }
        return null;
    }
}
