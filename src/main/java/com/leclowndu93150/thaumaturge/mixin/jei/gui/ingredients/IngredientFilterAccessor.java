package com.leclowndu93150.thaumaturge.mixin.jei.gui.ingredients;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.search.IElementSearch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IngredientFilter.class)
public interface IngredientFilterAccessor {
    @Accessor("elementSearch")
    IElementSearch thaumaturge$elementSearch();

    @Accessor("modIdHelper")
    IModIdHelper thaumaturge$modIdHelper();
}
