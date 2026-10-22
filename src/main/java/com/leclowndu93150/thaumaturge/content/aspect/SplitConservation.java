package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.Tags;

final class SplitConservation {
    private static final int SETTLE_PASSES = 8;

    private final List<Split> splits;

    private record Split(Item whole, int wholes, Item part, int parts) {}

    private record Uniform(Item input, int inputs, Item output, int outputs) {}

    private record Join(Item input, Item output) {}

    private SplitConservation(List<Split> splits) {
        this.splits = splits;
    }

    static SplitConservation scan(RecipeManager recipes, HolderLookup.Provider registries) {
        List<Uniform> uniforms = new ArrayList<>();
        Set<Join> joins = new HashSet<>();
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            if (holder.value() instanceof CraftingRecipe crafting) {
                uniformOf(crafting, registries).ifPresent(uniform -> {
                    uniforms.add(uniform);
                    joins.add(new Join(uniform.input(), uniform.output()));
                });
            }
        }
        List<Split> splits = new ArrayList<>();
        for (Uniform uniform : uniforms) {
            if (uniform.outputs() > uniform.inputs() && joins.contains(new Join(uniform.output(), uniform.input()))) {
                splits.add(new Split(uniform.input(), uniform.inputs(), uniform.output(), uniform.outputs()));
            }
        }
        return new SplitConservation(List.copyOf(splits));
    }

    Map<Item, AspectList> settle(Map<Item, AspectList> resolved, Set<Item> declared) {
        Map<Item, AspectList> settled = new HashMap<>();
        for (Split split : splits) {
            if (raisesWhole(split, declared)) {
                AspectList whole = valueOf(split.whole(), resolved, settled);
                AspectList raised = raise(whole, valueOf(split.part(), resolved, settled), split);
                if (!raised.equals(whole)) {
                    settled.put(split.whole(), raised);
                }
            }
        }
        boolean changed = true;
        for (int pass = 0; pass < SETTLE_PASSES && changed; pass++) {
            changed = false;
            for (Split split : splits) {
                AspectList whole = valueOf(split.whole(), resolved, settled);
                AspectList part = valueOf(split.part(), resolved, settled);
                if (whole.isEmpty() || part.isEmpty()) {
                    continue;
                }
                AspectList cut = cut(part, whole, split);
                if (!cut.equals(part)) {
                    settled.put(split.part(), cut);
                    changed = true;
                }
            }
        }
        return settled;
    }

    private static boolean raisesWhole(Split split, Set<Item> declared) {
        if (split.whole().builtInRegistryHolder().is(Tags.Items.STORAGE_BLOCKS)) {
            return true;
        }
        return !declared.contains(split.whole());
    }

    private static AspectList raise(AspectList whole, AspectList part, Split split) {
        AspectList contents = AspectList.EMPTY;
        for (AspectInstance entry : part.entries()) {
            int worth = Math.min(
                    AspectIndexBuilder.MAX_AMOUNT_PER_ASPECT,
                    Mth.positiveCeilDiv(entry.amount() * split.parts(), split.wholes()));
            contents = contents.add(entry.aspect(), worth);
        }
        return whole.merge(contents);
    }

    private static AspectList cut(AspectList part, AspectList whole, Split split) {
        AspectList result = AspectList.EMPTY;
        for (AspectInstance entry : part.entries()) {
            int share = whole.amountOf(entry.aspect()) * split.wholes() / split.parts();
            int amount = Math.min(entry.amount(), share);
            if (amount > 0) {
                result = result.add(entry.aspect(), amount);
            }
        }
        return result;
    }

    private static AspectList valueOf(Item item, Map<Item, AspectList> resolved, Map<Item, AspectList> settled) {
        AspectList value = settled.get(item);
        return value != null ? value : resolved.getOrDefault(item, AspectList.EMPTY);
    }

    private static Optional<Uniform> uniformOf(CraftingRecipe recipe, HolderLookup.Provider registries) {
        ItemStack output;
        List<Ingredient> ingredients;
        try {
            output = recipe.getResultItem(registries);
            ingredients = recipe.getIngredients().stream()
                    .filter(ingredient -> !ingredient.isEmpty())
                    .toList();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
        if (output.isEmpty() || ingredients.isEmpty()) {
            return Optional.empty();
        }
        Item input = RecipeAspectDerivation.representativeStack(ingredients.getFirst())
                .getItem();
        if (input == Items.AIR || input == output.getItem()) {
            return Optional.empty();
        }
        for (Ingredient ingredient : ingredients) {
            if (RecipeAspectDerivation.representativeStack(ingredient).getItem() != input) {
                return Optional.empty();
            }
        }
        return Optional.of(new Uniform(input, ingredients.size(), output.getItem(), output.getCount()));
    }
}
