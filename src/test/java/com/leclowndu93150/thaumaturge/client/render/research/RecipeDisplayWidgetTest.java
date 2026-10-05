package com.leclowndu93150.thaumaturge.client.render.research;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.junit.jupiter.api.Test;

class RecipeDisplayWidgetTest {
    @Test
    void infusionHitKeepsSlotBoundsAndItemComponentsAsMouseMoves() {
        InfusionRecipe recipe = mock(InfusionRecipe.class);
        ItemStack result = new ItemStack(Items.STONE);
        result.set(DataComponents.CUSTOM_NAME, Component.literal("Recipe variant"));
        when(recipe.resultItem()).thenReturn(result);
        when(recipe.catalyst()).thenReturn(Ingredient.EMPTY);
        when(recipe.components()).thenReturn(List.of());
        RecipeHolder<?> holder = new RecipeHolder<>(ResourceLocation.withDefaultNamespace("test"), recipe);

        RecipeDisplayWidget.ItemHit first = RecipeDisplayWidget.hoverItemForDisplay(100, 100, holder, 144, 67);
        RecipeDisplayWidget.ItemHit last = RecipeDisplayWidget.hoverItemForDisplay(100, 100, holder, 159.9, 82.9);

        assertNotNull(first);
        assertEquals(first, last);
        assertEquals(144, first.x());
        assertEquals(67, first.y());
        assertSame(result, first.stack());
        assertSame(result, RecipeDisplayWidget.hoverStackForDisplay(100, 100, holder, 0, 150, 75));
        assertNull(RecipeDisplayWidget.hoverItemForDisplay(100, 100, holder, 160, 75));
        assertNull(RecipeDisplayWidget.hoverItemForDisplay(100, 100, holder, 150, 83));
        assertFalse(first.contains(143.9, 75));
        assertFalse(first.contains(150, 66.9));
    }

    @Test
    void multiblockResultReportsItsActualOutputSlot() {
        DustTriggerMultiblockRecipe recipe = mock(DustTriggerMultiblockRecipe.class);
        ItemStack result = new ItemStack(Items.STONE);
        when(recipe.result()).thenReturn(result);
        RecipeHolder<?> holder = new RecipeHolder<>(ResourceLocation.withDefaultNamespace("test"), recipe);

        RecipeDisplayWidget.ItemHit hit = RecipeDisplayWidget.hoverItemForDisplay(100, 100, holder, 155, 80);

        assertNotNull(hit);
        assertSame(result, hit.stack());
        assertEquals(144, hit.x());
        assertEquals(68, hit.y());
        assertTrue(hit.contains(155, 80));
    }
}
