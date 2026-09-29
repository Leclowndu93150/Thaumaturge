package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.IInfusionRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class InfusionRecipe implements Recipe<InfusionInput>, IInfusionRecipe {
    public static final MapCodec<InfusionRecipe> MAP_CODEC = RecordCodecBuilder.<InfusionRecipe>mapCodec(i -> i.group(
                            Ingredient.CODEC.fieldOf("catalyst").forGetter(r -> r.catalyst),
                            Ingredient.CODEC.listOf(1, 64).fieldOf("components").forGetter(r -> r.components),
                            AspectList.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(r -> r.aspects),
                            Codec.intRange(0, 100)
                                    .optionalFieldOf("instability", 0)
                                    .forGetter(r -> r.instability),
                            ItemStack.CODEC.optionalFieldOf("result").forGetter(r -> r.result),
                            DataComponentPatch.CODEC
                                    .optionalFieldOf("catalyst_patch")
                                    .forGetter(r -> r.catalystPatch),
                            ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research))
                    .apply(i, InfusionRecipe::new))
            .validate(recipe -> recipe.result.isPresent() == recipe.catalystPatch.isPresent()
                    ? DataResult.error(() -> "Infusion recipe needs exactly one of result or catalyst_patch")
                    : DataResult.success(recipe));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Ingredient>> COMPONENTS_STREAM_CODEC =
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list());
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemStack>> RESULT_STREAM_CODEC =
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC);
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<DataComponentPatch>> PATCH_STREAM_CODEC =
            ByteBufCodecs.optional(DataComponentPatch.STREAM_CODEC);
    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<ResearchGate>> RESEARCH_STREAM_CODEC =
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> STREAM_CODEC = StreamCodec.of(
            (buffer, recipe) -> {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.catalyst);
                COMPONENTS_STREAM_CODEC.encode(buffer, recipe.components);
                AspectList.STREAM_CODEC.encode(buffer, recipe.aspects);
                ByteBufCodecs.VAR_INT.encode(buffer, recipe.instability);
                RESULT_STREAM_CODEC.encode(buffer, recipe.result);
                PATCH_STREAM_CODEC.encode(buffer, recipe.catalystPatch);
                RESEARCH_STREAM_CODEC.encode(buffer, recipe.research);
            },
            buffer -> new InfusionRecipe(
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                    COMPONENTS_STREAM_CODEC.decode(buffer),
                    AspectList.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    RESULT_STREAM_CODEC.decode(buffer),
                    PATCH_STREAM_CODEC.decode(buffer),
                    RESEARCH_STREAM_CODEC.decode(buffer)));

    public static final RecipeSerializer<InfusionRecipe> SERIALIZER =
            new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient catalyst;
    private final List<Ingredient> components;
    private final AspectList aspects;
    private final int instability;
    private final Optional<ItemStack> result;
    private final Optional<DataComponentPatch> catalystPatch;
    private final Optional<ResearchGate> research;

    public InfusionRecipe(
            Ingredient catalyst,
            List<Ingredient> components,
            AspectList aspects,
            int instability,
            ItemStack result,
            Optional<ResearchGate> research) {
        this(catalyst, components, aspects, instability, Optional.of(result), Optional.empty(), research);
    }

    public InfusionRecipe(
            Ingredient catalyst,
            List<Ingredient> components,
            AspectList aspects,
            int instability,
            Optional<ItemStack> result,
            Optional<DataComponentPatch> catalystPatch,
            Optional<ResearchGate> research) {
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
            return result.get().copy();
        }
        ItemStack display = Arrays.stream(catalyst.getItems())
                .findFirst()
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
        catalystPatch.ifPresent(display::applyComponents);
        return display;
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return research;
    }

    @Override
    public ItemStack assemble(InfusionInput input, HolderLookup.Provider registries) {
        if (result.isPresent()) {
            return result.get().copy();
        }
        ItemStack out = input.catalyst().copyWithCount(1);
        catalystPatch.ifPresent(out::applyComponents);
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.map(ItemStack::copy).orElse(ItemStack.EMPTY);
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
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }
}
