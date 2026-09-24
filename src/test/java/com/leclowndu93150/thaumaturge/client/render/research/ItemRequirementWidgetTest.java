package com.leclowndu93150.thaumaturge.client.render.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class ItemRequirementWidgetTest {
    @Test
    void requirementIconKeepsRequiredComponents() {
        Component name = Component.literal("Required variant");
        ResearchRequirement requirement = new ResearchRequirement(
                HolderSet.direct(Items.STONE.builtInRegistryHolder()),
                DataComponentPatch.builder()
                        .set(DataComponents.CUSTOM_NAME, name)
                        .build(),
                1);

        ItemStack displayed = ItemRequirementWidget.cycleItemStack(requirement, 0);

        assertEquals(name, displayed.get(DataComponents.CUSTOM_NAME));
        assertTrue(requirement.matches(displayed));
    }
}
