package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ThaumometerEntityInteractionEvents {
    private ThaumometerEntityInteractionEvents() {}

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!intercept(event.getEntity(), event.getHand(), event.getTarget())) {
            return;
        }
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!intercept(event.getEntity(), event.getHand(), event.getTarget())) {
            return;
        }
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    private static boolean intercept(Player player, InteractionHand hand, Entity target) {
        boolean isInteracting = !player.isShiftKeyDown() && player.getItemInHand(hand).getItem() instanceof ThaumometerItem;

        if (!isInteracting) {
            return false;
        }

        return ThaumometerItem.beginScanAt(player, hand, target).consumesAction();
    }

}
