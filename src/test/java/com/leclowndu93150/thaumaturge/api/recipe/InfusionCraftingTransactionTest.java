package com.leclowndu93150.thaumaturge.api.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class InfusionCraftingTransactionTest {
    @Test
    void inspectionDoesNotExposeMutableStacks() {
        ItemStack output = new ItemStack(Items.STONE, 2);
        ItemStack catalyst = new ItemStack(Items.DIRT, 3);
        ItemStack remainder = new ItemStack(Items.BUCKET);
        InfusionCraftingTransaction.Inspection inspection = new InfusionCraftingTransaction.Inspection(
                true,
                InfusionCraftingTransaction.Failure.NONE,
                null,
                AspectList.EMPTY,
                0,
                ResearchStatus.NOT_REQUIRED,
                output,
                catalyst,
                List.of(remainder),
                true);

        output.setCount(64);
        catalyst.setCount(64);
        remainder.setCount(64);
        ItemStack returnedOutput = inspection.output();
        ItemStack returnedCatalyst = inspection.catalystAfter();
        List<ItemStack> returnedRemainders = inspection.componentRemainders();
        assertEquals(2, returnedOutput.getCount());
        assertEquals(3, returnedCatalyst.getCount());
        assertEquals(1, returnedRemainders.getFirst().getCount());

        returnedOutput.setCount(1);
        returnedCatalyst.setCount(1);
        returnedRemainders.getFirst().setCount(2);
        assertEquals(2, inspection.output().getCount());
        assertEquals(3, inspection.catalystAfter().getCount());
        assertEquals(1, inspection.componentRemainders().getFirst().getCount());
    }
}
