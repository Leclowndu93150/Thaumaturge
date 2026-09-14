package com.leclowndu93150.thaumaturge.content.infusion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class InfusionResultTest {
    @Test
    void projectedResultPreservesNativeCatalystDamageRatio() {
        ItemStack catalyst = new ItemStack(Items.IRON_PICKAXE);
        catalyst.setDamageValue(catalyst.getMaxDamage() / 2);
        ItemStack result = new ItemStack(Items.DIAMOND_PICKAXE);

        ItemStack projected = BlockEntityInfusionMatrix.preserveCatalystDamage(result, catalyst);

        assertEquals(projected.getMaxDamage() / 2, projected.getDamageValue(), 1);
        assertEquals(0, result.getDamageValue());
    }
}
