package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public final class CultistRobeItem extends ArmorItem implements IVisDiscountGear {
    private static final int VIS_DISCOUNT = 1;
    private static final int WARP = 1;

    public CultistRobeItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Properties properties) {
        super(material, type, GearWarp.with(properties, WARP));
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return VIS_DISCOUNT;
    }
}
