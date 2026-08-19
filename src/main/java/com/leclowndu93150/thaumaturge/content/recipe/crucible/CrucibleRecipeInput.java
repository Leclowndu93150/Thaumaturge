package com.leclowndu93150.thaumaturge.content.recipe.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record CrucibleRecipeInput(ItemStack catalyst, AspectList availableAspects) implements RecipeInput {
    @Override
    public ItemStack getItem(int i) {
        if (i == 0)
            return catalyst;
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }
}
