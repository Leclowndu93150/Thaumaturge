package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.Nullable;

public final class PrimordialPearlItem extends Item {
    public static final int MAX_DAMAGE = 8;

    public static final int PEARL_MAX_DAMAGE = 2;
    public static final int NODULE_MAX_DAMAGE = 5;

    public PrimordialPearlItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        if (instance.getOrDefault(DataComponents.DAMAGE, 0) + 1 >= MAX_DAMAGE) {
            return null;
        }
        return new ItemStackTemplate(TCItems.PRIMORDIAL_PEARL, DataComponentPatch.builder().set(DataComponents.DAMAGE, instance.getOrDefault(DataComponents.DAMAGE, 0) + 1).build());
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
