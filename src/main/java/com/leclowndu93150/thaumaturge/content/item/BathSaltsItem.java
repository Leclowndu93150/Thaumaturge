package com.leclowndu93150.thaumaturge.content.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BathSaltsItem extends Item {
    public BathSaltsItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return 200;
    }
}
