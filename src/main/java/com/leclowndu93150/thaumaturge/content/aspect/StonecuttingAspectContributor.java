package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectRecipeContributor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public final class StonecuttingAspectContributor implements IAspectRecipeContributor {
    private Map<Item, List<Candidate>> candidates = Map.of();

    private record Candidate(List<Ingredient> ingredients, int count) {}

    @Override
    public void beginBuild(RecipeManager recipes, HolderLookup.Provider registries) {
        Map<Item, List<Candidate>> map = new HashMap<>();
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            if (!(holder.value() instanceof StonecutterRecipe recipe)) {
                continue;
            }
            try {
                ItemStack output = recipe.getResultItem(registries);
                if (!output.isEmpty()) {
                    map.computeIfAbsent(output.getItem(), item -> new ArrayList<>())
                            .add(new Candidate(List.copyOf(recipe.getIngredients()), output.getCount()));
                }
            } catch (RuntimeException ignored) {
            }
        }
        candidates = map;
    }

    @Override
    public Optional<AspectList> derive(
            Item item, RecipeManager recipes, HolderLookup.Provider registries, IAspectIndex partial) {
        List<Candidate> list = candidates.get(item);
        if (list == null) {
            return Optional.empty();
        }
        AspectList best = null;
        int bestSize = Integer.MAX_VALUE;
        for (Candidate candidate : list) {
            AspectList out =
                    RecipeAspectDerivation.fromIngredients(candidate.ingredients(), candidate.count(), partial);
            int size = out.totalAmount();
            if (size > 0 && size < bestSize) {
                best = out;
                bestSize = size;
            }
        }
        return Optional.ofNullable(best);
    }
}
