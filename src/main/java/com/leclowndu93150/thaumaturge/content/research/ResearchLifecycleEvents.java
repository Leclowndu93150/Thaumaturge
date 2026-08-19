package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ResearchLifecycleEvents {
    private ResearchLifecycleEvents() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResearchManager.applyAutoUnlock(player);
            AspectPools.seedIfNew(player);
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
            if (event.getTo().equals(OuterLands.DIMENSION) && !ResearchManager.of(player).isResearchComplete(ENTER_OUTER_LANDS)) {
                ResearchManager.complete(player, ENTER_OUTER_LANDS);
            }
            ResearchManager.of(player).sync(player);
        }
    }

    private static final Identifier ENTER_OUTER_LANDS = TCIds.rl("enter_outer_lands");
}
