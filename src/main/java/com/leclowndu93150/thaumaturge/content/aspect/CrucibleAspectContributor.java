package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
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

public final class CrucibleAspectContributor implements IAspectRecipeContributor {
    private Map<Item, List<Candidate>> candidates = Map.of();

    private record Candidate(Ingredient catalyst, AspectList aspects, int count) {
    }

    @Override
    public void beginBuild(RecipeManager recipes, HolderLookup.Provider registries) {
        Map<Item, List<Candidate>> map = new HashMap<>();
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            if (!(holder.value() instanceof CrucibleRecipe crucible)) {
                continue;
            }
            ItemStack output = crucible.rawResult().create();
            if (output.isEmpty()) {
                continue;
            }
            map.computeIfAbsent(output.getItem(), item -> new ArrayList<>()).add(new Candidate(crucible.catalyst(), crucible.aspects(), output.getCount()));
        }
        candidates = map;
    }

    @Override
    public Optional<AspectList> derive(Item item, RecipeManager recipes, HolderLookup.Provider registries, IAspectIndex partial) {
        List<Candidate> list = candidates.get(item);
        if (list == null) {
            return Optional.empty();
        }
        for (Candidate candidate : list) {
            ItemStack catalyst = RecipeAspectDerivation.representativeStack(candidate.catalyst());
            if (catalyst.isEmpty()) {
                continue;
            }
            AspectList out = partial.of(catalyst);
            for (AspectInstance entry : RecipeAspectDerivation.drain(candidate.aspects(), candidate.count()).entries()) {
                out = out.add(entry);
            }
            if (!out.isEmpty()) {
                return Optional.of(out);
            }
        }
        return Optional.empty();
    }
}
