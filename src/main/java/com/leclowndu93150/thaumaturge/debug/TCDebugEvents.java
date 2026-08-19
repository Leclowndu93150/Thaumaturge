package com.leclowndu93150.thaumaturge.debug;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanRaycastHelper;
import com.leclowndu93150.thaumaturge.debug.network.ClientboundRaycastDebugPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TCIds.MODID)
public class TCDebugEvents {

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide())
            return;
        Player player = event.getEntity();
        HitResult hitResult = ScanRaycastHelper.performRaycast(player);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new ClientboundRaycastDebugPayload(hitResult));
    }
}
