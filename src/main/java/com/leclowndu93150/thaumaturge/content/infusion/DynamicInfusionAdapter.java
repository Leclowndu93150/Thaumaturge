package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.DynamicInfusionRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionRecipeInput;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

final class DynamicInfusionAdapter implements InfusionJobRecipe {
    private final DynamicInfusionRecipe recipe;
    private final ItemStack result;
    private final AspectList quote;

    DynamicInfusionAdapter(DynamicInfusionRecipe recipe, InfusionRecipeInput input, HolderLookup.Provider registries) {
        this.recipe = recipe;
        var preview = recipe.preview(input, registries).orElseThrow(() -> new IllegalArgumentException("Invalid prepared infusion result"));
        this.result = preview.output();
        this.quote = preview.essentia();
    }

    @Override
    public boolean matches(InfusionInput input, Level level) {
        return recipe.matches(new InfusionRecipeInput(input.catalyst(), input.components()), level);
    }

    @Override
    public ItemStack assemble(InfusionInput input) {
        return result.copy();
    }

    @Override
    public AspectList jobEssentia(InfusionInput input) {
        return quote;
    }

    @Override
    public Ingredient catalyst() {
        return recipe.catalyst();
    }

    @Override
    public List<Ingredient> components() {
        return recipe.components();
    }

    @Override
    public AspectList aspects() {
        return recipe.aspects();
    }

    @Override
    public int instability() {
        return recipe.instability();
    }

    @Override
    public ItemStack resultItem() {
        return result.copy();
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return recipe.researchGate();
    }

    @Override
    public RecipeSerializer<? extends Recipe<InfusionInput>> getSerializer() {
        throw new UnsupportedOperationException("Prepared infusion adapters are not serialized as recipes");
    }

    @Override
    public RecipeType<? extends Recipe<InfusionInput>> getType() {
        throw new UnsupportedOperationException("Prepared infusion adapters are not registered as recipes");
    }

    @Override
    public String group() {
        return recipe.group();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return recipe.recipeBookCategory();
    }

    @Override
    public PlacementInfo placementInfo() {
        return recipe.placementInfo();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
