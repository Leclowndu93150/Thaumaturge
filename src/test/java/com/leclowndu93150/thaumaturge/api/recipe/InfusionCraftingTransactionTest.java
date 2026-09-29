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
        ItemStack remainder = new ItemStack(Items.BUCKET);
        InfusionCraftingTransaction.Inspection inspection = new InfusionCraftingTransaction.Inspection(
                InfusionCraftingTransaction.Failure.NONE,
                null,
                AspectList.EMPTY,
                0,
                ResearchGateStatus.NOT_REQUIRED,
                output,
                List.of(remainder),
                true);

        output.setCount(64);
        remainder.setCount(64);
        ItemStack returnedOutput = inspection.output();
        List<ItemStack> returnedRemainders = inspection.componentRemainders();
        assertEquals(2, returnedOutput.getCount());
        assertEquals(1, returnedRemainders.getFirst().getCount());

        returnedOutput.setCount(1);
        returnedRemainders.getFirst().setCount(2);
        assertEquals(2, inspection.output().getCount());
        assertEquals(1, inspection.componentRemainders().getFirst().getCount());
    }
}
