package com.leclowndu93150.thaumaturge.data.recipe.builders.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapelessCraftingRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class ArcaneWorkbenchShapelessRecipeBuilder extends ArcaneWorkbenchRecipeBuilder<ArcaneWorkbenchShapelessRecipeBuilder> {
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final HolderGetter<Item> items;

    public ArcaneWorkbenchShapelessRecipeBuilder(RecipeCategory category, ItemStackTemplate result, HolderGetter<IAspect> aspects, int vis, HolderGetter<Item> items) {
        super(category, result, aspects, vis);
        this.items = items;
    }

    public ArcaneWorkbenchShapelessRecipeBuilder requires(TagKey<Item> tag) {
        return this.requires(Ingredient.of(this.items.getOrThrow(tag)));
    }

    public ArcaneWorkbenchShapelessRecipeBuilder requires(ItemLike item) {
        return this.requires((ItemLike) item, 1);
    }

    public ArcaneWorkbenchShapelessRecipeBuilder requires(ItemLike item, int count) {
        for (int i = 0; i < count; ++i) {
            this.requires(Ingredient.of(item));
        }

        return this;
    }

    public ArcaneWorkbenchShapelessRecipeBuilder requires(Ingredient ingredient) {
        return this.requires((Ingredient) ingredient, 1);
    }

    public ArcaneWorkbenchShapelessRecipeBuilder requires(Ingredient ingredient, int count) {
        for (int i = 0; i < count; ++i) {
            this.ingredients.add(ingredient);
        }
        return this;
    }

    @Override
    protected ArcaneCraftingRecipe makeRecipe(Recipe.CommonInfo commonInfo, ItemStackTemplate result, AspectList aspects, Optional<ResearchGate> gate, int vis) {
        return new ArcaneShapelessCraftingRecipe(commonInfo, vis, gate, aspects, result, ingredients);
    }
}
