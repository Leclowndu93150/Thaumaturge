package com.leclowndu93150.thaumaturge.data.recipe.builders;

import com.google.common.base.Preconditions;
import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionEnchantmentRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public final class InfusionEnchantmentRecipeBuilder {
    private final HolderGetter<IAspect> aspectsGetter;
    private final InfusionEnchantment enchantment;
    private final Ingredient displayCatalyst;
    private final List<Ingredient> components = new ArrayList<>();
    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
    private AspectList aspects = AspectList.EMPTY;
    private ResearchGate gate;

    public InfusionEnchantmentRecipeBuilder(HolderGetter<IAspect> aspectsGetter, InfusionEnchantment enchantment, Ingredient displayCatalyst) {
        this.aspectsGetter = aspectsGetter;
        this.enchantment = enchantment;
        this.displayCatalyst = displayCatalyst;
    }

    public InfusionEnchantmentRecipeBuilder component(Ingredient ingredient) {
        components.add(ingredient);
        return this;
    }

    public InfusionEnchantmentRecipeBuilder aspect(ResourceKey<IAspect> aspect, int amount) {
        Preconditions.checkArgument(amount > 0, "The amount of aspect must be positive !");
        this.aspects = aspects.add(new AspectInstance(aspectsGetter.getOrThrow(aspect), amount));
        return this;
    }

    public InfusionEnchantmentRecipeBuilder gate(ResearchGate gate) {
        Preconditions.checkNotNull(gate, "The research gate must not be null !");
        this.gate = gate;
        return this;
    }

    public InfusionEnchantmentRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    public void save(RecipeOutput output) {
        Preconditions.checkState(!components.isEmpty(), "Infusion enchantment recipe has no components");
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(TCIds.MODID, "infusion_enchantment/" + enchantment.getSerializedName()));
        InfusionEnchantmentRecipe recipe = new InfusionEnchantmentRecipe(enchantment, components, aspects, displayCatalyst, Optional.ofNullable(gate));
        output.accept(key, recipe, null);
    }
}
