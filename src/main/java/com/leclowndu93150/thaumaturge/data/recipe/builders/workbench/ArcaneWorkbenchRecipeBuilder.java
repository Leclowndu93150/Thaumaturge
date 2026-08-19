package com.leclowndu93150.thaumaturge.data.recipe.builders.workbench;

import com.google.common.base.Preconditions;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.data.recipe.builders.SimpleRecipeBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

public abstract class ArcaneWorkbenchRecipeBuilder<R extends ArcaneWorkbenchRecipeBuilder<R>> extends SimpleRecipeBuilder {
    private final HolderGetter<IAspect> aspectsGetter;
    private @Nullable ResearchGate gate;
    private AspectList aspects = AspectList.EMPTY;
    private final int vis;

    public ArcaneWorkbenchRecipeBuilder(RecipeCategory category, ItemStackTemplate result, HolderGetter<IAspect> aspectsGetter, int vis) {
        super(result, category);
        this.aspectsGetter = aspectsGetter;
        Preconditions.checkArgument(vis > 0, "Vis cannot be <= 0");
        this.vis = vis;
    }

    public R allAspects() {
        return aspect(TCAspects.AER).aspect(TCAspects.IGNIS).aspect(TCAspects.TERRA).aspect(TCAspects.AQUA).aspect(TCAspects.ORDO).aspect(TCAspects.PERDITIO);
    }

    public R aspect(ResourceKey<IAspect> aspect) {
        return aspect(aspect, 1);
    }

    public R aspect(ResourceKey<IAspect> aspect, int amount) {
        return aspect(aspectsGetter.getOrThrow(aspect), amount);
    }

    public R aspect(Holder<IAspect> aspect) {
        return aspect(aspect, 1);
    }

    public R aspect(Holder<IAspect> aspect, int amount) {
        return aspect(new AspectInstance(aspect, amount));
    }

    public R aspect(AspectInstance instance) {
        Preconditions.checkArgument(instance.aspect().value().isPrimal(), "The aspect can only be a primal aspect !");
        Preconditions.checkArgument(instance.amount() > 0, "The amount of aspect must be positive !");
        this.aspects = aspects.add(instance);
        return (R) this;
    }

    public R gate(ResearchGate gate) {
        Preconditions.checkNotNull(gate, "The research gate must not be null !");
        this.gate = gate;
        return (R) this;
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> key) {
        ArcaneCraftingRecipe recipe = makeRecipe(RecipeBuilder.createCraftingCommonInfo(true), this.result, this.aspects, Optional.ofNullable(gate), vis);

        output.accept(key, recipe, this.advancementBuilder.build(output, key, this.category));
    }

    protected abstract ArcaneCraftingRecipe makeRecipe(Recipe.CommonInfo commonInfo, ItemStackTemplate result, AspectList aspects, Optional<ResearchGate> gate, int vis);

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, result.typeHolder().unwrapKey().orElseThrow().identifier().withPrefix("arcane_workbench/"));
    }
}
