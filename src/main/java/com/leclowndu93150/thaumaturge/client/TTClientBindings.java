package com.leclowndu93150.thaumaturge.client;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.client.AspectRendering;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectRenderingBindings;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTClientBindings {
    private TTClientBindings() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        AspectRendering.bind(new AspectRenderingBindings());
        AspectKnowledgeAccess.bind(aspect -> {
            Player player = Minecraft.getInstance().player;
            if (player == null || AspectPools.isDiscovered(player, aspect)) {
                return AspectKnowledge.KNOWN;
            }
            boolean derivable = !aspect.value().isPrimal() && AspectPools.hasDiscoveredComponents(player, aspect);
            return derivable ? AspectKnowledge.DEDUCIBLE : AspectKnowledge.UNKNOWN;
        });
    }
}
