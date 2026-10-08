package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ResearchLifecycleEvents {
    private ResearchLifecycleEvents() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResearchManager.applyAutoUnlock(player);
            AspectPools.seedIfNew(player);
            AspectPools.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResearchManager.applyAutoUnlock(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getTo().equals(OuterLands.DIMENSION)
                    && !ResearchManager.of(player).isResearchComplete(ENTER_OUTER_LANDS)) {
                ResearchManager.complete(player, ENTER_OUTER_LANDS);
            }
            ResearchManager.of(player).sync(player);
        }
    }

    private static final ResourceLocation ENTER_OUTER_LANDS = TTIds.rl("enter_outer_lands");
}
