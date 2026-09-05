package com.leclowndu93150.thaumaturge.content.warding;

import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ArcaneAccess {
    private ArcaneAccess() {}

    public static boolean canAccess(ServerLevel level, BlockPos pos, Player player) {
        if (player.getUUID().equals(WardHandler.owner(level, pos))) {
            return true;
        }
        GlobalPos target = GlobalPos.of(level.dimension(), pos);
        for (ItemStack stack : player.getInventory().items) {
            if (matches(stack, target)) {
                return true;
            }
        }
        return matches(player.getOffhandItem(), target);
    }

    private static boolean matches(ItemStack stack, GlobalPos target) {
        return (stack.is(TCItems.ARCANE_KEY_IRON.get()) || stack.is(TCItems.ARCANE_KEY_GOLD.get()))
                && target.equals(stack.get(TCDataComponents.ARCANE_KEY_LINK.get()));
    }
}
