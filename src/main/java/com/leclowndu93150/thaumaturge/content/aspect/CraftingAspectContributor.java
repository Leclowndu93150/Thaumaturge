package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class CraftingAspectContributor implements IAspectRecipeContributor {
    private static final CraftingInput EMPTY_INPUT = CraftingInput.of(0, 0, List.of());

    private Map<Item, List<Candidate>> candidates = Map.of();
    private Holder<IAspect> magic;

    private record Candidate(List<Ingredient> ingredients, int count, int vis) {
    }

    @Override
    public void beginBuild(RecipeManager recipes, HolderLookup.Provider registries) {
        magic = registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TCAspects.PRAECANTATIO);
        Map<Item, List<Candidate>> map = new HashMap<>();
        int skipped = 0;
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            Recipe<?> recipe = holder.value();
            try {
                if (recipe instanceof ArcaneCraftingRecipe arcane) {
                    ItemStack output = safeAssembleArcane(arcane);
                    if (!output.isEmpty()) {
                        int count = Math.max(1, output.getCount());
                        map.computeIfAbsent(output.getItem(), item -> new ArrayList<>()).add(new Candidate(arcane.placementInfo().ingredients(), count, arcane.getBaseVis()));
                    }
                } else if (recipe instanceof CraftingRecipe crafting) {
                    ItemStack output = safeAssemble(crafting);
                    if (!output.isEmpty()) {
                        map.computeIfAbsent(output.getItem(), item -> new ArrayList<>()).add(new Candidate(crafting.placementInfo().ingredients(), output.getCount(), -1));
                    }
                }
            } catch (Throwable t) {
                skipped++;
            }
        }
        if (skipped > 0) {
            Thaumaturge.LOGGER.warn("Skipped {} crafting recipes with broken placement info while indexing aspects", skipped);
        }
        candidates = map;
    }

    @Override
    public Optional<AspectList> derive(Item item, RecipeManager recipes, HolderLookup.Provider registries, IAspectIndex partial) {
        List<Candidate> list = candidates.get(item);
        if (list == null) {
            return Optional.empty();
        }
        AspectList best = null;
        int bestSize = Integer.MAX_VALUE;
        for (Candidate candidate : list) {
            AspectList out = RecipeAspectDerivation.fromIngredients(candidate.ingredients(), candidate.count(), partial);
            if (candidate.vis() > 0) {
                int bonus = (int) (Math.sqrt(1 + candidate.vis() / 2) / candidate.count());
                if (bonus > 0) {
                    out = out.add(magic, bonus);
                }
            }
            if (out == null || out.isEmpty()) {
                continue;
            }
            int size = out.totalAmount();
            if (size > 0 && size < bestSize) {
                best = out;
                bestSize = size;
            }
        }
        return Optional.ofNullable(best);
    }

    private static ItemStack safeAssemble(CraftingRecipe recipe) {
        try {
            ItemStack result = recipe.assemble(EMPTY_INPUT);
            return result == null ? ItemStack.EMPTY : result;
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack safeAssembleArcane(ArcaneCraftingRecipe recipe) {
        try {
            ItemStack result = recipe.assemble(ArcaneCraftingInput.EMPTY);
            return result == null ? ItemStack.EMPTY : result;
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }
}
