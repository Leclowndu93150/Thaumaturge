package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.wands.assembly.WandAssemblyHook;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public abstract class ArcaneCraftingRecipe implements IArcaneRecipe {

    public static Codec<AspectInstance> LIMITED_ASPECTS =
            AspectInstance.CODEC.validate(instance -> instance.amount() > 64
                    ? DataResult.error(() ->
                            "The amount for '" + instance.aspect().getKey().location() + "' aspect must not exceed 64.")
                    : DataResult.success(instance));
    public static Codec<AspectList> PRIMAL_ASPECTS_CODEC = LIMITED_ASPECTS
            .listOf(0, 6)
            .flatXmap(
                    entries -> {
                        AspectList result = AspectList.EMPTY;
                        for (AspectInstance entry : entries) {
                            if (!entry.aspect().value().isPrimal())
                                return DataResult.error(
                                        () -> "'" + entry.aspect().getKey().location() + "' is not a primal aspect.",
                                        result);
                            result = result.add(entry);
                        }
                        return DataResult.success(result);
                    },
                    list -> DataResult.success(list.entries()));

    protected final String group;
    protected final int vis;
    protected final Optional<ResearchGate> gate;
    protected final AspectList aspects;

    protected ArcaneCraftingRecipe(String group, int vis, Optional<ResearchGate> gate, AspectList aspects) {
        this.group = group;
        this.vis = vis;
        this.gate = gate;
        this.aspects = aspects;
    }

    protected static ItemStack assembleResult(ItemStack result, IArcaneCraftingInput input) {
        return WandAssemblyHook.apply(result.copy(), input);
    }

    protected static NonNullList<ItemStack> defaultCraftingReminder(IArcaneCraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int slot = 0; slot < result.size(); ++slot) {
            result.set(slot, input.getItem(slot).getCraftingRemainingItem());
        }

        return result;
    }

    @Override
    public RecipeType<ArcaneCraftingRecipe> getType() {
        return TCRecipeTypes.ARCANE.get();
    }

    @Override
    public abstract RecipeSerializer<? extends ArcaneCraftingRecipe> getSerializer();

    @Override
    public NonNullList<ItemStack> getRemainingItems(IArcaneCraftingInput input) {
        return defaultCraftingReminder(input);
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public boolean isIncomplete() {
        return true;
    }

    @Override
    public int getBaseVis() {
        return vis;
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return gate;
    }

    @Override
    public AspectList getCrystals() {
        return aspects;
    }
}
