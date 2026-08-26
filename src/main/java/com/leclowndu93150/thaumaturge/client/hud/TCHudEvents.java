package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.casters.RadialFocusOverlay;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(value = Dist.CLIENT, modid = TCIds.MODID)
public final class TCHudEvents {
    private TCHudEvents() {}

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.EXPERIENCE_LEVEL,
                TCIds.rl("left_hud_stack"),
                GuiBlend.alphaBlendedLayer(new LeftHudStack(
                        List.of(CasterHudOverlay.dialGauge(), new AuraHudOverlay(), new SanityHudOverlay()))));
        event.registerAbove(
                VanillaGuiLayers.EXPERIENCE_LEVEL,
                TCIds.rl("knowledge_gain"),
                GuiBlend.alphaBlendedLayer(new KnowledgeGainOverlay()));
        event.registerAbove(
                VanillaGuiLayers.EXPERIENCE_LEVEL,
                TCIds.rl("caster_hud"),
                GuiBlend.alphaBlendedLayer(new CasterHudOverlay()));
        event.registerAbove(
                VanillaGuiLayers.EXPERIENCE_LEVEL,
                TCIds.rl("recharge_hud"),
                GuiBlend.alphaBlendedLayer(new RechargeHudOverlay()));
        event.registerAbove(
                VanillaGuiLayers.EXPERIENCE_LEVEL,
                TCIds.rl("radial_focus"),
                GuiBlend.alphaBlendedLayer(new RadialFocusOverlay()));
    }
}
