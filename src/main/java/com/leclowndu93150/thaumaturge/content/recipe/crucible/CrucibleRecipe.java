package com.leclowndu93150.thaumaturge.content.recipe.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGated;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CrucibleRecipe implements Recipe<CrucibleRecipeInput>, ResearchGated {

    public static final MapCodec<CrucibleRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Ingredient.CODEC.fieldOf("catalyst").forGetter(r -> r.catalyst),
                    AspectList.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(r -> r.aspects),
                    ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                    ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research))
            .apply(i, CrucibleRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            r -> r.catalyst,
            AspectList.STREAM_CODEC,
            r -> r.aspects,
            ItemStack.STREAM_CODEC,
            r -> r.result,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC),
            r -> r.research,
            CrucibleRecipe::new);

    public static final RecipeSerializer<CrucibleRecipe> SERIALIZER =
            new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient catalyst;
    private final AspectList aspects;
    private final ItemStack result;
    private final Optional<ResearchGate> research;

    public CrucibleRecipe(Ingredient catalyst, AspectList aspects, ItemStack result, Optional<ResearchGate> research) {
        this.catalyst = catalyst;
        this.aspects = aspects;
        this.result = result;
        this.research = research;
    }

    @Override
    public boolean matches(CrucibleRecipeInput input, Level level) {
        if (!catalyst.test(input.catalyst())) return false;
        if (input.availableAspects().isEmpty()) return false;
        for (AspectInstance aspect : aspects.entries()) {
            if (input.availableAspects().amountOf(aspect.aspect()) < aspect.amount()) {
                return false;
            }
        }
        return true;
    }

    public AspectList removeMatching(AspectList old) {
        AspectList temp = AspectList.ofEntries(old.entries());

        for (AspectInstance tag : this.aspects().entries()) {
            temp = temp.remove(tag.aspect(), tag.amount());
        }

        return temp;
    }

    @Override
    public ItemStack assemble(CrucibleRecipeInput crucibleRecipeInput, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<CrucibleRecipeInput>> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<CrucibleRecipeInput>> getType() {
        return TCRecipeTypes.CRUCIBLE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public AspectList aspects() {
        return aspects;
    }

    public Ingredient catalyst() {
        return catalyst;
    }

    public ItemStack rawResult() {
        return result;
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return research;
    }
}
