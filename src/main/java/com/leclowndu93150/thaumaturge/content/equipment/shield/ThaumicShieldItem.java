package com.leclowndu93150.thaumaturge.content.equipment.shield;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

public class ThaumicShieldItem extends ShieldItem {
    private final TagKey<Item> repairItems;
    private final int enchantmentValue;

    public ThaumicShieldItem(Properties properties, TagKey<Item> repairItems, int enchantmentValue) {
        super(properties);
        this.repairItems = repairItems;
        this.enchantmentValue = enchantmentValue;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(repairItems);
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }
}
