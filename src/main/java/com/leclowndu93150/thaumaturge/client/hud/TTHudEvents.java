package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.casters.RadialFocusOverlay;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(value = Dist.CLIENT, modid = TTIds.MODID)
public final class TTHudEvents {
    private TTHudEvents() {}

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TTIds.rl("left_hud_stack"), new LeftHudStack(List.of(CasterHudOverlay.dialGauge(), new AuraHudOverlay(), new SanityHudOverlay())));
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TTIds.rl("knowledge_gain"), new KnowledgeGainOverlay());
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TTIds.rl("caster_hud"), new CasterHudOverlay());
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TTIds.rl("recharge_hud"), new RechargeHudOverlay());
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TTIds.rl("hover_hud"), new HoverHudOverlay());
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TTIds.rl("radial_focus"), new RadialFocusOverlay());
    }
}
