package com.leclowndu93150.thaumaturge.mixin.jei.gui.ingredients;

import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.ingredients.IngredientFilterApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IngredientFilterApi.class)
public interface IngredientFilterApiAccessor {
    @Accessor("ingredientFilter")
    IngredientFilter thaumaturge$ingredientFilter();
}
