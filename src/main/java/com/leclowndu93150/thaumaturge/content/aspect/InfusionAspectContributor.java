package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
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

public final class InfusionAspectContributor implements IAspectRecipeContributor {
    private Map<Item, List<Candidate>> candidates = Map.of();

    private record Candidate(List<Ingredient> ingredients, AspectList aspects, int count) {
    }

    @Override
    public void beginBuild(RecipeManager recipes, HolderLookup.Provider registries) {
        Map<Item, List<Candidate>> map = new HashMap<>();
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            if (!(holder.value() instanceof InfusionRecipe infusion)) {
                continue;
            }
            ItemStack output = infusion.resultItem();
            if (output.isEmpty()) {
                continue;
            }
            List<Ingredient> ingredients = new ArrayList<>(infusion.components().size() + 1);
            ingredients.add(infusion.catalyst());
            ingredients.addAll(infusion.components());
            map.computeIfAbsent(output.getItem(), item -> new ArrayList<>()).add(new Candidate(List.copyOf(ingredients), infusion.aspects(), output.getCount()));
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
            AspectList out = RecipeAspectDerivation.fromIngredients(candidate.ingredients(), candidate.count(), partial);
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
