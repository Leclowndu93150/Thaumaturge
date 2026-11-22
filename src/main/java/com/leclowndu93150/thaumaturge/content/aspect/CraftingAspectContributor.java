package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private static final int NO_VIS = -1;
    private static final int MIN_OUTPUT_COUNT = 1;
    private static final int VIS_PER_STEP = 2;

    private Map<Item, List<Candidate>> candidates = Map.of();
    private Holder<IAspect> magic;

    private record Candidate(List<Ingredient> ingredients, int count, int vis) {
    }

    @Override
    public void beginBuild(RecipeManager recipes, HolderLookup.Provider registries) {
        Holder<IAspect> resolved = Aspects.resolve(registries, TTAspects.PRAECANTATIO);
        if (resolved == null) {
            throw new IllegalStateException("Aspect " + TTAspects.PRAECANTATIO.identifier() + " is missing from the aspect registry");
        }
        Map<Item, List<Candidate>> map = new HashMap<>();
        int skipped = 0;
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            ItemStack output = outputOf(holder.value());
            if (output.isEmpty()) {
                continue;
            }
            try {
                map.computeIfAbsent(output.getItem(), item -> new ArrayList<>()).add(candidateOf(holder.value(), output));
            } catch (RuntimeException e) {
                skipped++;
            }
        }
        if (skipped > 0) {
            Thaumaturge.LOGGER.warn("Skipped {} crafting recipes with broken placement info while indexing aspects", skipped);
        }
        magic = resolved;
        candidates = map;
    }

    @Override
    public Optional<AspectList> derive(Item item, RecipeManager recipes, HolderLookup.Provider registries, IAspectIndex partial) {
        List<Candidate> pool = candidates.get(item);
        if (pool == null) {
            return Optional.empty();
        }
        return pool.stream().map(candidate -> aspectsOf(candidate, partial)).filter(aspects -> aspects.totalAmount() > 0).min(Comparator.comparingInt(AspectList::totalAmount));
    }

    private AspectList aspectsOf(Candidate candidate, IAspectIndex partial) {
        AspectList base = RecipeAspectDerivation.fromIngredients(candidate.ingredients(), candidate.count(), partial);
        int bonus = magicBonus(candidate);
        return bonus > 0 ? base.add(magic, bonus) : base;
    }

    private static int magicBonus(Candidate candidate) {
        if (candidate.vis() <= 0) {
            return 0;
        }
        double root = Math.sqrt(1 + candidate.vis() / VIS_PER_STEP);
        return (int) (root / Math.max(MIN_OUTPUT_COUNT, candidate.count()));
    }

    private static ItemStack outputOf(Recipe<?> recipe) {
        try {
            // Some mods' special recipes return null instead of ItemStack.EMPTY for an input they don't match
            if (recipe instanceof IArcaneRecipe arcane) {
                return Objects.requireNonNullElse(arcane.assemble(ArcaneCraftingInput.EMPTY), ItemStack.EMPTY);
            }
            if (recipe instanceof CraftingRecipe crafting) {
                return Objects.requireNonNullElse(crafting.assemble(CraftingInput.EMPTY), ItemStack.EMPTY);
            }
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    private static Candidate candidateOf(Recipe<?> recipe, ItemStack output) {
        int count = Math.max(MIN_OUTPUT_COUNT, output.getCount());
        if (recipe instanceof IArcaneRecipe arcane) {
            return new Candidate(List.copyOf(arcane.placementInfo().ingredients()), count, arcane.visCost());
        }
        return new Candidate(List.copyOf(recipe.placementInfo().ingredients()), output.getCount(), NO_VIS);
    }
}
