package com.leclowndu93150.thaumaturge.content.item.equipment;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class GogglesItem extends Item implements IVisDiscountGear {
    public GogglesItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return 5;
    }
}
