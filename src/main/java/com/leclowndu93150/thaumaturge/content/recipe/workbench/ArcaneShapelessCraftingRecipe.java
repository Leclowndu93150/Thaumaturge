package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ArcaneShapelessCraftingRecipe extends ArcaneCraftingRecipe {
    public static final MapCodec<ArcaneShapelessCraftingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(CommonInfo.MAP_CODEC.forGetter((o) -> o.commonInfo),
            ExtraCodecs.POSITIVE_INT.fieldOf("vis").forGetter(o -> o.vis), ResearchGate.CODEC.optionalFieldOf("research").forGetter(o -> o.gate),
            ArcaneCraftingRecipe.PRIMAL_ASPECTS_CODEC.fieldOf("crystals").forGetter(o -> o.aspects), ItemStackTemplate.CODEC.fieldOf("result").forGetter((o) -> o.result),
            Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(1, ArcaneShapedRecipePattern.maxHeight * ArcaneShapedRecipePattern.maxWidth)).fieldOf("ingredients").forGetter((o) -> o.ingredients))
            .apply(i, ArcaneShapelessCraftingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneShapelessCraftingRecipe> STREAM_CODEC = StreamCodec.composite(CommonInfo.STREAM_CODEC, (o) -> o.commonInfo, ByteBufCodecs.VAR_INT,
            (o) -> o.vis, ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), o -> o.gate, AspectList.STREAM_CODEC, o -> o.aspects, ItemStackTemplate.STREAM_CODEC, (o) -> o.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), (o) -> o.ingredients, ArcaneShapelessCraftingRecipe::new);

    public static final RecipeSerializer<ArcaneShapelessCraftingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;
    private final boolean isSimple;

    public ArcaneShapelessCraftingRecipe(Recipe.CommonInfo commonInfo, int vis, Optional<ResearchGate> researchGate, AspectList aspects, ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, vis, researchGate, aspects);
        this.result = result;
        this.ingredients = ingredients;
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    public @NonNull RecipeSerializer<ArcaneShapelessCraftingRecipe> getSerializer() {
        return SERIALIZER;
    }

    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(this.ingredients);
    }

    public boolean matches(IArcaneCraftingInput input, Level level) {
        if (!super.matches(input, level))
            return false;
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        } else if (!this.isSimple) {
            ArrayList<ItemStack> nonEmptyItems = new ArrayList<>(input.ingredientCount());

            for (ItemStack item : input.items().subList(0, 9)) {
                if (!item.isEmpty()) {
                    nonEmptyItems.add(item);
                }
            }

            return RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
        } else {
            return input.size() == 1 && this.ingredients.size() == 1 ? this.ingredients.getFirst().test(input.getItem(0)) : input.stackedContents().canCraft(this, null);
        }
    }

    public @Nullable ItemStackTemplate result() {
        return this.result;
    }

    public List<Ingredient> ingredients() {
        return this.ingredients;
    }

    public ItemStack assemble(IArcaneCraftingInput input) {
        return this.result.create();
    }

    public List<RecipeDisplay> display() {
        return List.of(new ArcaneCraftingRecipeDisplay(0, 0, true, this.ingredients.stream().map(Ingredient::display).map(d -> (SlotDisplay) d).toList(),
                new SlotDisplay.ItemStackSlotDisplay(this.result), new SlotDisplay.ItemSlotDisplay(TCBlocks.ARCANE_WORKBENCH.get().asItem()), this.vis, crystalDisplays()));
    }
}
