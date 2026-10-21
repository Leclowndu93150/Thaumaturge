package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.recipe.DynamicInfusionRecipe;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public final class InfusionRecipePreviews {
    private InfusionRecipePreviews() {}

    public static List<RecipeDisplay> displays(Recipe<?> recipe, HolderLookup.Provider registries) {
        if (!(recipe instanceof DynamicInfusionRecipe dynamic)) {
            return recipe.display();
        }
        return dynamic.preview(registries).<List<RecipeDisplay>>map(preview -> List.of(new InfusionRecipeDisplay(slot(preview.input().catalyst()),
                preview.input().components().stream().map(InfusionRecipePreviews::slot).toList(), preview.essentia(), preview.instability(), slot(preview.output())))).orElseGet(List::of);
    }

    private static SlotDisplay slot(ItemStack stack) {
        return new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
    }
}
