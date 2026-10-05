package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.Test;

class JarUpgradeTest {
    @Test
    void arcaneUpgradePreservesContentsLabelAndNameWithoutChangingInput() {
        ResourceKey<IAspect> key =
                ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("thaumaturge", "aer"));
        ItemStack jar = new ItemStack(TCItems.JAR_NORMAL.get(), 3);
        EssentiaList contents = new EssentiaList(AspectList.EMPTY.add(Holder.direct(mock(IAspect.class)), 48));
        jar.set(TCDataComponents.ESSENTIA_CONTENTS, contents);
        jar.set(TCDataComponents.ASPECT_FILTER, key);
        jar.set(DataComponents.CUSTOM_NAME, Component.literal("Air supply"));
        IArcaneCraftingInput input = mock(IArcaneCraftingInput.class);
        when(input.size()).thenReturn(2);
        when(input.getItem(0)).thenReturn(ItemStack.EMPTY);
        when(input.getItem(1)).thenReturn(jar);
        ItemStack template = new ItemStack(TCItems.JAR_VOID.get());
        ArcaneShapelessCraftingRecipe recipe = new ArcaneShapelessCraftingRecipe(
                "", 50, Optional.empty(), AspectList.EMPTY, template, List.of(Ingredient.of(TCItems.JAR_NORMAL)));

        ItemStack result = recipe.assemble(input, null);

        assertTrue(result.is(TCItems.JAR_VOID));
        assertEquals(1, result.getCount());
        assertEquals(contents, result.get(TCDataComponents.ESSENTIA_CONTENTS));
        assertEquals(key, result.get(TCDataComponents.ASPECT_FILTER));
        assertEquals(jar.get(DataComponents.CUSTOM_NAME), result.get(DataComponents.CUSTOM_NAME));
        assertEquals(3, jar.getCount());
        assertNull(template.get(TCDataComponents.ESSENTIA_CONTENTS));
    }
}
