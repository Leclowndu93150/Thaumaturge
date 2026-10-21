package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * An input-dependent infusion executed by the normal matrix pipeline. Implementations register a
 * standard recipe serializer and use the shared recipe type returned by {@link #getType()}.
 * Matching, quoting and preparation must be deterministic and side-effect free. The matrix saves
 * the prepared result before payment; recipe reloads do not change an accepted craft.
 */
public interface DynamicInfusionRecipe extends IInfusionRecipe, Recipe<InfusionRecipeInput> {
    boolean matches(InfusionRecipeInput input, HolderLookup.Provider registries);

    default AspectList quote(InfusionRecipeInput input, HolderLookup.Provider registries) {
        return aspects();
    }

    ItemStack prepareResult(InfusionRecipeInput input, HolderLookup.Provider registries);

    /** Returns the same deterministic preparation used by the matrix, without authorizing a craft. */
    default Optional<InfusionRecipePreview> preview(InfusionRecipeInput input, HolderLookup.Provider registries) {
        try {
            if (!matches(input, registries) || matchComponents(input.components()) == null) {
                return Optional.empty();
            }
            return Optional.of(new InfusionRecipePreview(input, prepareResult(input, registries), quote(input, registries), instability()));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return Optional.empty();
        }
    }

    /**
     * Finds a representative display from at most 32 catalyst candidates. Recipes requiring
     * component-specific example stacks may override this method and use the input overload.
     */
    default Optional<InfusionRecipePreview> preview(HolderLookup.Provider registries) {
        if (components().size() > 64) {
            return Optional.empty();
        }
        List<ItemStack> examples = components().stream().map(ingredient -> ingredient.items().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY)).toList();
        if (examples.stream().anyMatch(ItemStack::isEmpty)) {
            return Optional.empty();
        }
        for (ItemStack catalyst : catalyst().items().limit(32).map(ItemStack::new).toList()) {
            Optional<InfusionRecipePreview> preview = preview(new InfusionRecipeInput(catalyst, examples), registries);
            if (preview.isPresent()) {
                return preview;
            }
        }
        return Optional.empty();
    }

    @Override
    default boolean matches(InfusionRecipeInput input, Level level) {
        return matches(input, level.registryAccess());
    }

    @Override
    default ItemStack assemble(InfusionRecipeInput input) {
        return resultItem();
    }

    @Override
    default RecipeType<? extends DynamicInfusionRecipe> getType() {
        return TTRecipeTypes.DYNAMIC_INFUSION.get();
    }

    @Override
    default String group() {
        return "";
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }
}
