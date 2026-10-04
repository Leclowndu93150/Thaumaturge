package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ArcaneShapelessCraftingRecipe extends ArcaneCraftingRecipe {
    public static final MapCodec<ArcaneShapelessCraftingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter((o) -> o.group),
                    ExtraCodecs.POSITIVE_INT.fieldOf("vis").forGetter(o -> o.vis),
                    ResearchGate.CODEC.optionalFieldOf("research").forGetter(o -> o.gate),
                    ArcaneCraftingRecipe.PRIMAL_ASPECTS_CODEC
                            .fieldOf("crystals")
                            .forGetter(o -> o.aspects),
                    ItemStack.CODEC.fieldOf("result").forGetter((o) -> o.result),
                    Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(
                                    1, ArcaneShapedRecipePattern.maxHeight * ArcaneShapedRecipePattern.maxWidth))
                            .fieldOf("ingredients")
                            .forGetter((o) -> o.ingredients))
            .apply(i, ArcaneShapelessCraftingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneShapelessCraftingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    (o) -> o.group,
                    ByteBufCodecs.VAR_INT,
                    (o) -> o.vis,
                    ByteBufCodecs.optional(ResearchGate.STREAM_CODEC),
                    o -> o.gate,
                    AspectList.STREAM_CODEC,
                    o -> o.aspects,
                    ItemStack.STREAM_CODEC,
                    (o) -> o.result,
                    Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
                    (o) -> o.ingredients,
                    ArcaneShapelessCraftingRecipe::new);

    public static final RecipeSerializer<ArcaneShapelessCraftingRecipe> SERIALIZER =
            new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
    private final ItemStack result;
    private final List<Ingredient> ingredients;
    private final boolean isSimple;

    public ArcaneShapelessCraftingRecipe(
            String group,
            int vis,
            Optional<ResearchGate> researchGate,
            AspectList aspects,
            ItemStack result,
            List<Ingredient> ingredients) {
        super(group, vis, researchGate, aspects);
        this.result = result;
        this.ingredients = ingredients;
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    @Override
    public @NonNull RecipeSerializer<ArcaneShapelessCraftingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean matches(IArcaneCraftingInput input, Level level) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        } else if (!this.isSimple) {
            ArrayList<ItemStack> nonEmptyItems = new ArrayList<>(input.ingredientCount());

            for (ItemStack item : input.items().subList(0, input.width() * input.height())) {
                if (!item.isEmpty()) {
                    nonEmptyItems.add(item);
                }
            }

            return RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
        } else {
            return input.size() == 1 && this.ingredients.size() == 1
                    ? this.ingredients.getFirst().test(input.getItem(0))
                    : input.stackedContents().canCraft(this, null);
        }
    }

    public @Nullable ItemStack result() {
        return this.result;
    }

    public List<Ingredient> ingredients() {
        return this.ingredients;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(this.ingredients);
        return list;
    }

    @Override
    public ItemStack assemble(IArcaneCraftingInput input, HolderLookup.Provider registries) {
        return assembleResult(this.result, input);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.ingredients.size();
    }
}
