package com.leclowndu93150.thaumaturge.data.recipe.builders;

import com.google.common.base.Preconditions;
import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public class CrucibleRecipeBuilder extends SimpleRecipeBuilder {
    private final HolderGetter<IAspect> aspectsGetter;
    private final Ingredient catalyst;
    private @Nullable ResearchGate gate;
    private AspectList aspects = AspectList.EMPTY;

    public CrucibleRecipeBuilder(
            HolderGetter<IAspect> aspectsGetter, RecipeCategory category, ItemStack result, Ingredient catalyst) {
        super(result, category);
        this.aspectsGetter = aspectsGetter;
        this.catalyst = catalyst;
    }

    public CrucibleRecipeBuilder aspect(ResourceKey<IAspect> aspect) {
        return aspect(aspect, 1);
    }

    public CrucibleRecipeBuilder aspect(ResourceKey<IAspect> aspect, int amount) {
        return aspect(aspectsGetter.getOrThrow(aspect), amount);
    }

    public CrucibleRecipeBuilder aspect(Holder<IAspect> aspect) {
        return aspect(aspect, 1);
    }

    public CrucibleRecipeBuilder aspect(Holder<IAspect> aspect, int amount) {
        return aspect(new AspectInstance(aspect, amount));
    }

    public CrucibleRecipeBuilder aspect(AspectInstance instance) {
        Preconditions.checkArgument(instance.amount() > 0, "The amount of aspect must be positive !");
        this.aspects = aspects.add(instance);
        return this;
    }

    public CrucibleRecipeBuilder gate(ResearchGate gate) {
        Preconditions.checkNotNull(gate, "The research gate must not be null !");
        this.gate = gate;
        return this;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        CrucibleRecipe recipe = new CrucibleRecipe(this.catalyst, this.aspects, this.result, Optional.ofNullable(gate));

        output.accept(id, recipe, buildAdvancement(output, id));
    }

    @Override
    protected ResourceLocation defaultId() {
        return TCIds.rl(
                "crucible/" + RecipeBuilder.getDefaultRecipeId(getResult()).getPath());
    }
}
