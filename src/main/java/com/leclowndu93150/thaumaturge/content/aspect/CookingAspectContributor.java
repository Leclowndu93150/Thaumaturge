package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
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
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class CookingAspectContributor implements IAspectRecipeContributor {
    private Map<Item, List<Candidate>> candidates = Map.of();

    private record Candidate(Ingredient input, int count) {}

    @Override
    public void beginBuild(RecipeManager recipes, HolderLookup.Provider registries) {
        Map<Item, List<Candidate>> map = new HashMap<>();
        int skipped = 0;
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            if (!(holder.value() instanceof AbstractCookingRecipe cooking)) {
                continue;
            }
            try {
                ItemStack output = cooking.getResultItem(registries);
                if (output != null
                        && !output.isEmpty()
                        && !cooking.getIngredients().isEmpty()) {
                    map.computeIfAbsent(output.getItem(), item -> new ArrayList<>())
                            .add(new Candidate(cooking.getIngredients().get(0), Math.max(1, output.getCount())));
                }
            } catch (RuntimeException e) {
                skipped++;
            }
        }
        if (skipped > 0) {
            Thaumaturge.LOGGER.warn(
                    "Skipped {} cooking recipes with unreadable results while indexing aspects", skipped);
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
            ItemStack input = RecipeAspectDerivation.representativeStack(candidate.input());
            if (input.isEmpty() || input.is(item)) {
                continue;
            }
            AspectList out = AspectList.EMPTY;
            for (AspectInstance entry : partial.of(input).entries()) {
                int amount = entry.amount() / candidate.count();
                if (amount > 0) {
                    out = out.add(entry.aspect(), amount);
                }
            }
            int size = out.totalAmount();
            if (size > 0 && size < bestSize) {
                best = out;
                bestSize = size;
            }
        }
        return Optional.ofNullable(best);
    }
}
