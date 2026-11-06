package com.leclowndu93150.thaumaturge.mixin.world.item.crafting;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingTrimRecipe.class)
public interface SmithingTrimRecipeAccessor {
    @Accessor("template")
    Ingredient thaumaturge$getTemplate();

    @Accessor("base")
    Ingredient thaumaturge$getBase();

    @Accessor("addition")
    Ingredient thaumaturge$getAddition();
}
