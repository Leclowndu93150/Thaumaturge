package com.leclowndu93150.thaumaturge.api.items;

import net.minecraft.world.item.ItemStack;

public interface IChanneledItem {
    default boolean releasesOnScreenOpen(ItemStack stack) {
        return true;
    }
}
