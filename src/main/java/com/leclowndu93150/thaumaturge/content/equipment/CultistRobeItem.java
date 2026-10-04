package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CultistRobeItem extends Item implements IVisDiscountGear {
    private static final int VIS_DISCOUNT = 1;
    private static final int WARP = 1;

    public CultistRobeItem(Properties properties) {
        super(GearWarp.with(properties, WARP));
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return VIS_DISCOUNT;
    }

}
