package com.leclowndu93150.thaumaturge.content.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.client.recipes.TCClientRecipes;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ThaumaturgeCraftingManager {

    @SuppressWarnings("unchecked")
    public static @Nullable ArcaneCraftingRecipe findMatchingArcaneRecipe(Level level, ArcaneCraftingInput input, Player player) {
        RecipeMap recipes = level.isClientSide() ? TCClientRecipes.getRecipeMapForType(level, TCRecipeTypes.ARCANE.get()) : ((ServerLevel) level).recipeAccess().recipeMap();
        return recipes.byType(TCRecipeTypes.ARCANE.get()).stream().filter(r -> r.value().matches(input, level)).filter(r -> r.value().doesPassGate(player)).findFirst().map(RecipeHolder::value)
                .orElse(null);
    }

    public static @Nullable CrucibleRecipe findMatchingCrucibleRecipe(ServerLevel level, Player player, AspectList aspects, ItemStack lastDrop) {
        int highest = 0;
        CrucibleRecipe out = null;

        List<CrucibleRecipe> recipes = level.recipeAccess().getRecipes().stream().filter(r -> r.value() instanceof CrucibleRecipe).map(RecipeHolder::value).map(CrucibleRecipe.class::cast)
                .filter(r -> r.matches(new CrucibleRecipeInput(lastDrop, aspects), level)).toList();

        for (CrucibleRecipe recipe : recipes) {
            if (player != null && recipe.doesPassGate(player)) {
                int result = recipe.aspects().totalAmount();
                if (result > highest) {
                    highest = result;
                    out = recipe;
                }
            }
        }

        return out;
    }
}
