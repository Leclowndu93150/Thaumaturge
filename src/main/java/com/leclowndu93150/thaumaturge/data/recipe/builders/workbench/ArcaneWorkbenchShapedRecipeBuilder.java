package com.leclowndu93150.thaumaturge.data.recipe.builders.workbench;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapedCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapedRecipePattern;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public class ArcaneWorkbenchShapedRecipeBuilder extends ArcaneWorkbenchRecipeBuilder<ArcaneWorkbenchShapedRecipeBuilder> {

    private final HolderGetter<Item> items;
    private final List<String> rows;
    private final Map<Character, Ingredient> key;

    public ArcaneWorkbenchShapedRecipeBuilder(RecipeCategory category, ItemStackTemplate result, HolderGetter<Item> items, HolderGetter<IAspect> aspects, int vis) {
        super(category, result, aspects, vis);
        this.items = items;
        this.rows = Lists.newArrayList();
        this.key = Maps.newLinkedHashMap();
    }

    public ArcaneWorkbenchShapedRecipeBuilder define(Character symbol, TagKey<Item> tag) {
        return this.define(symbol, Ingredient.of(this.items.getOrThrow(tag)));
    }

    public ArcaneWorkbenchShapedRecipeBuilder define(Character symbol, ItemLike item) {
        return this.define(symbol, Ingredient.of(item));
    }

    public ArcaneWorkbenchShapedRecipeBuilder define(Character symbol, Ingredient ingredient) {
        if (this.key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        } else if (symbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(symbol, ingredient);
            return this;
        }
    }

    public ArcaneWorkbenchShapedRecipeBuilder pattern(String row) {
        if (!this.rows.isEmpty() && row.length() != this.rows.getFirst().length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(row);
            return this;
        }
    }

    @Override
    protected ArcaneCraftingRecipe makeRecipe(Recipe.CommonInfo commonInfo, ItemStackTemplate result, AspectList aspects, Optional<ResearchGate> gate, int vis) {
        ArcaneShapedRecipePattern pattern = ArcaneShapedRecipePattern.of(key, rows);
        return new ArcaneShapedCraftingRecipe(commonInfo, vis, gate, aspects, pattern, result);
    }
}
