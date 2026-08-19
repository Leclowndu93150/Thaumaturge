package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.IInfusionRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public final class InfusionRecipe implements Recipe<InfusionInput>, IInfusionRecipe {
    public static final MapCodec<InfusionRecipe> MAP_CODEC = RecordCodecBuilder
            .<InfusionRecipe>mapCodec(i -> i.group(Ingredient.CODEC.fieldOf("catalyst").forGetter(r -> r.catalyst), Ingredient.CODEC.listOf(1, 64).fieldOf("components").forGetter(r -> r.components),
                    AspectList.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(r -> r.aspects), Codec.intRange(0, 100).optionalFieldOf("instability", 0).forGetter(r -> r.instability),
                    ItemStackTemplate.CODEC.optionalFieldOf("result").forGetter(r -> r.result), DataComponentPatch.CODEC.optionalFieldOf("catalyst_patch").forGetter(r -> r.catalystPatch),
                    ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research)).apply(i, InfusionRecipe::new))
            .validate(recipe -> recipe.result.isPresent() == recipe.catalystPatch.isPresent()
                    ? DataResult.error(() -> "Infusion recipe needs exactly one of result or catalyst_patch")
                    : DataResult.success(recipe));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> STREAM_CODEC = StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC, r -> r.catalyst,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.components, AspectList.STREAM_CODEC, r -> r.aspects, ByteBufCodecs.VAR_INT, r -> r.instability,
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), r -> r.result, ByteBufCodecs.optional(DataComponentPatch.STREAM_CODEC), r -> r.catalystPatch,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), r -> r.research, InfusionRecipe::new);

    public static final RecipeSerializer<InfusionRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient catalyst;
    private final List<Ingredient> components;
    private final AspectList aspects;
    private final int instability;
    private final Optional<ItemStackTemplate> result;
    private final Optional<DataComponentPatch> catalystPatch;
    private final Optional<ResearchGate> research;

    public InfusionRecipe(Ingredient catalyst, List<Ingredient> components, AspectList aspects, int instability, ItemStackTemplate result, Optional<ResearchGate> research) {
        this(catalyst, components, aspects, instability, Optional.of(result), Optional.empty(), research);
    }

    public InfusionRecipe(Ingredient catalyst, List<Ingredient> components, AspectList aspects, int instability, Optional<ItemStackTemplate> result, Optional<DataComponentPatch> catalystPatch, Optional<ResearchGate> research) {
        this.catalyst = catalyst;
        this.components = List.copyOf(components);
        this.aspects = aspects;
        this.instability = instability;
        this.result = result;
        this.catalystPatch = catalystPatch;
        this.research = research;
    }

    @Override
    public boolean matches(InfusionInput input, Level level) {
        if (!catalyst.test(input.catalyst())) {
            return false;
        }
        return matchComponents(input.components()) != null;
    }

    @Override
    public Ingredient catalyst() {
        return catalyst;
    }

    @Override
    public List<Ingredient> components() {
        return components;
    }

    @Override
    public AspectList aspects() {
        return aspects;
    }

    @Override
    public int instability() {
        return instability;
    }

    @Override
    public ItemStack resultItem() {
        if (result.isPresent()) {
            return result.get().create();
        }
        ItemStack display = catalyst.items().findFirst().map(ItemStack::new).orElse(ItemStack.EMPTY);
        catalystPatch.ifPresent(display::applyComponents);
        return display;
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return research;
    }

    @Override
    public List<RecipeDisplay> display() {
        ItemStack out = resultItem();
        SlotDisplay resultDisplay = out.isEmpty() ? SlotDisplay.Empty.INSTANCE : new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(out));
        return List.of(new InfusionRecipeDisplay(catalyst.display(), components.stream().map(Ingredient::display).map(d -> (SlotDisplay) d).toList(), aspects, instability, resultDisplay));
    }

    @Override
    public ItemStack assemble(InfusionInput input) {
        if (result.isPresent()) {
            return result.get().create();
        }
        ItemStack out = input.catalyst().copyWithCount(1);
        catalystPatch.ifPresent(out::applyComponents);
        return out;
    }

    @Override
    public RecipeSerializer<InfusionRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<InfusionRecipe> getType() {
        return TCRecipeTypes.INFUSION.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }
}
