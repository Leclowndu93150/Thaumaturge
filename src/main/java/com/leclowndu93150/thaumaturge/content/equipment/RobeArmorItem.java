package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public final class RobeArmorItem extends ArmorItem implements IVisDiscountGear {
    private final int visDiscount;

    public RobeArmorItem(int visDiscount, ArmorItem.Type type, Properties properties) {
        super(TTMaterials.ARMOR_ROBES, type, properties);
        this.visDiscount = visDiscount;
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return visDiscount;
    }
}
