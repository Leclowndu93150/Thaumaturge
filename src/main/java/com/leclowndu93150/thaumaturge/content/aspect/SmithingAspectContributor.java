package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumaturge.mixin.world.item.crafting.SmithingTransformRecipeAccessor;
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
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

public final class SmithingAspectContributor implements IAspectRecipeContributor {
    private Map<Item, List<Candidate>> candidates = Map.of();

    private record Candidate(List<Ingredient> ingredients, int count) {}

    @Override
    public void beginBuild(RecipeManager recipes, HolderLookup.Provider registries) {
        Map<Item, List<Candidate>> map = new HashMap<>();
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            if (!(holder.value() instanceof SmithingTransformRecipe recipe)) {
                continue;
            }
            ItemStack output;
            try {
                output = recipe.getResultItem(registries);
            } catch (RuntimeException ignored) {
                continue;
            }
            if (output.isEmpty()) {
                continue;
            }
            SmithingTransformRecipeAccessor access = (SmithingTransformRecipeAccessor) recipe;
            map.computeIfAbsent(output.getItem(), item -> new ArrayList<>())
                    .add(new Candidate(
                            List.of(
                                    access.thaumaturge$getTemplate(),
                                    access.thaumaturge$getBase(),
                                    access.thaumaturge$getAddition()),
                            output.getCount()));
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
