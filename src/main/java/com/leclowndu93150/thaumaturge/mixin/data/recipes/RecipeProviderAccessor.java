package com.leclowndu93150.thaumaturge.mixin.data.recipes;

import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeProvider.class)
public interface RecipeProviderAccessor {

    @Invoker("generateCraftingRecipe")
    void thaumaturge$generateCraftingRecipe(BlockFamily family, BlockFamily.Variant variant, Block result, ItemLike base);

    @Invoker("generateStonecutterRecipe")
    void thaumaturge$generateStonecutterRecipe(BlockFamily family, BlockFamily.Variant variant, Block base);
}
