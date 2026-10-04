package com.leclowndu93150.thaumaturge.content.entity.ai;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface ItemCollector {
    boolean wantsToCollect(ItemEntity item);

    ItemStack collect(ItemStack stack);
}
