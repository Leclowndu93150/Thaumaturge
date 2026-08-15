package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ThaumometerEntityInteractionEvents {
    private ThaumometerEntityInteractionEvents() {}

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        InteractionResult result = tryBeginScan(event.getEntity(), event.getHand(), event.getTarget());
        if (result == InteractionResult.PASS) {
            return;
        }
        event.setCancellationResult(result);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = tryBeginScan(event.getEntity(), event.getHand(), event.getTarget());
        if (result == InteractionResult.PASS) {
            return;
        }
        event.setCancellationResult(result);
        event.setCanceled(true);
    }

    private static InteractionResult tryBeginScan(Player player, InteractionHand hand, Entity target) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ThaumometerItem)) {
            return InteractionResult.PASS;
        }

        return ThaumometerItem.beginScanAt(player, hand, target);
    }
}
