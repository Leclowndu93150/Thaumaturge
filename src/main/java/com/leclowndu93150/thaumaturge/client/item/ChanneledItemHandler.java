package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.items.IChanneledItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ChanneledItemHandler {
    private ChanneledItemHandler() {}

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.gameMode == null || !player.isUsingItem()) {
            return;
        }
        ItemStack stack = player.getUseItem();
        if (stack.getItem() instanceof IChanneledItem channeled && channeled.releasesOnScreenOpen(stack)) {
            minecraft.gameMode.releaseUsingItem(player);
        }
    }
}
