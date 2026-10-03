package com.leclowndu93150.thaumaturge.debug;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanRaycastHelper;
import com.leclowndu93150.thaumaturge.debug.network.ClientboundRaycastDebugPayload;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TCIds.MODID)
public class TCDebugEvents {
    private static final Set<UUID> RAYCAST_DEBUG_PLAYERS = ConcurrentHashMap.newKeySet();

    public static void toggleRaycastDebug(ServerPlayer player) {
        if (!RAYCAST_DEBUG_PLAYERS.remove(player.getUUID())) {
            RAYCAST_DEBUG_PLAYERS.add(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        RAYCAST_DEBUG_PLAYERS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        Player player = event.getEntity();
        if (!RAYCAST_DEBUG_PLAYERS.contains(player.getUUID())
                || !player.level()
                        .hasChunk(
                                player.blockPosition().getX() >> 4,
                                player.blockPosition().getZ() >> 4)) return;
        HitResult hitResult = ScanRaycastHelper.performRaycast(player);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new ClientboundRaycastDebugPayload(hitResult));
    }
}
