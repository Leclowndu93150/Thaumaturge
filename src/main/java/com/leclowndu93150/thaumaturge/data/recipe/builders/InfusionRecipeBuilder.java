package com.leclowndu93150.thaumaturge.data.recipe.builders;

import com.google.common.base.Preconditions;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public class InfusionRecipeBuilder extends SimpleRecipeBuilder {
    private final HolderGetter<IAspect> aspectsGetter;
    private final Ingredient catalyst;
    private final List<Ingredient> components = new ArrayList<>();
    private AspectList aspects = AspectList.EMPTY;
    private int instability;
    private @Nullable ResearchGate gate;
    private @Nullable DataComponentPatch catalystPatch;

    public InfusionRecipeBuilder(HolderGetter<IAspect> aspectsGetter, RecipeCategory category, ItemStackTemplate result, Ingredient catalyst) {
        super(result, category);
        this.aspectsGetter = aspectsGetter;
        this.catalyst = catalyst;
    }

    public InfusionRecipeBuilder component(Ingredient ingredient) {
        components.add(ingredient);
        return this;
    }

    public InfusionRecipeBuilder aspect(ResourceKey<IAspect> aspect, int amount) {
        Preconditions.checkArgument(amount > 0, "The amount of aspect must be positive !");
        this.aspects = aspects.add(new AspectInstance(aspectsGetter.getOrThrow(aspect), amount));
        return this;
    }

    public InfusionRecipeBuilder instability(int instability) {
        this.instability = instability;
        return this;
    }

    public InfusionRecipeBuilder catalystPatch(DataComponentPatch catalystPatch) {
        this.catalystPatch = catalystPatch;
        return this;
    }

    public InfusionRecipeBuilder gate(ResearchGate gate) {
        Preconditions.checkNotNull(gate, "The research gate must not be null !");
        this.gate = gate;
        return this;
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> key) {
        Preconditions.checkState(!components.isEmpty(), "Infusion recipe has no components");
        InfusionRecipe recipe = catalystPatch != null
                ? new InfusionRecipe(catalyst, components, aspects, instability, Optional.empty(), Optional.of(catalystPatch), Optional.ofNullable(gate))
                : new InfusionRecipe(catalyst, components, aspects, instability, result, Optional.ofNullable(gate));
        output.accept(key, recipe, this.advancementBuilder.build(output, key, this.category));
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, result.typeHolder().unwrapKey().orElseThrow().identifier().withPrefix("infusion/"));
    }
}
