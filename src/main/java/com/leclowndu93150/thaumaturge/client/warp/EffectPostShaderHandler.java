package com.leclowndu93150.thaumaturge.client.warp;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.mixin.client.renderer.GameRendererInvoker;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class EffectPostShaderHandler {
    private static final Identifier DEATH_GAZE_SHADER = TTIds.rl("death_gaze");
    private static final Identifier BLURRED_VISION_SHADER = TTIds.rl("blurred_vision");
    private static final Identifier UNNATURAL_HUNGER_SHADER = TTIds.rl("unnatural_hunger");
    private static final Identifier SUN_SCORNED_SHADER = TTIds.rl("sun_scorned");

    private static @Nullable Identifier active;

    private EffectPostShaderHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.getCameraEntity() != player) {
            active = null;
            return;
        }
        Identifier desired = desiredShader(player);
        if (desired == active && (desired == null || desired.equals(mc.gameRenderer.currentPostEffect()))) {
            return;
        }
        if (desired == null) {
            active = null;
            mc.gameRenderer.checkEntityPostEffect(mc.getCameraEntity());
            return;
        }
        active = desired;
        ((GameRendererInvoker) mc.gameRenderer).thaumaturge$setPostEffect(desired);
    }

    private static @Nullable Identifier desiredShader(LocalPlayer player) {
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
