package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.casters.RadialFocusOverlay;
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
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TCIds.rl("left_hud_stack"), new LeftHudStack(List.of(CasterHudOverlay.dialGauge(), new AuraHudOverlay(), new SanityHudOverlay())));
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TCIds.rl("knowledge_gain"), new KnowledgeGainOverlay());
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TCIds.rl("caster_hud"), new CasterHudOverlay());
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TCIds.rl("recharge_hud"), new RechargeHudOverlay());
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, TCIds.rl("radial_focus"), new RadialFocusOverlay());
    }
}
