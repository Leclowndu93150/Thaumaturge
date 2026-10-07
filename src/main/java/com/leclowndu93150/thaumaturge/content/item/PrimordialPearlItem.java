package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PrimordialPearlItem extends Item {
    public static final int MAX_DAMAGE = 8;

    public static final int PEARL_MAX_DAMAGE = 2;
    public static final int NODULE_MAX_DAMAGE = 5;

    public PrimordialPearlItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return stack.getOrDefault(DataComponents.DAMAGE, 0) + 1 < MAX_DAMAGE;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        int nextDamage = stack.getOrDefault(DataComponents.DAMAGE, 0) + 1;
        if (nextDamage >= MAX_DAMAGE) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = new ItemStack(TTItems.PRIMORDIAL_PEARL.get());
        remainder.set(DataComponents.DAMAGE, nextDamage);
        return remainder;
    }

    @Override
    public Component getName(ItemStack stack) {
        int damage = stack.getDamageValue();
        String suffix;
        if (damage < 3) {
            suffix = ".pearl";
        } else if (damage < 6) {
            suffix = ".nodule";
        } else {
            suffix = ".mote";
        }
        return Component.translatable(this.getDescriptionId() + suffix);
    }
}
