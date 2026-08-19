package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.workbench.WorkbenchPayment;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public abstract class ArcaneCraftingRecipe implements IArcaneRecipe {

    public static Codec<AspectInstance> LIMITED_ASPECTS = AspectInstance.CODEC.validate(
            instance -> instance.amount() > 64 ? DataResult.error(() -> "The amount for '" + instance.aspect().getKey().identifier() + "' aspect must not exceed 64.") : DataResult.success(instance));
    public static Codec<AspectList> PRIMAL_ASPECTS_CODEC = LIMITED_ASPECTS.listOf(0, 6).flatXmap(entries -> {
        AspectList result = AspectList.EMPTY;
        for (AspectInstance entry : entries) {
            if (!entry.aspect().value().isPrimal())
                return DataResult.error(() -> "'" + entry.aspect().getKey().identifier() + "' is not a primal aspect.", result);
            result = result.add(entry);
        }
        return DataResult.success(result);
    }, list -> DataResult.success(list.entries()));

    protected final Recipe.CommonInfo commonInfo;
    protected final int vis;
    protected final Optional<ResearchGate> gate;
    protected final AspectList aspects;
    private @Nullable PlacementInfo placementInfo;

    protected ArcaneCraftingRecipe(Recipe.CommonInfo commonInfo, int vis, Optional<ResearchGate> gate, AspectList aspects) {
        this.commonInfo = commonInfo;
        this.vis = vis;
        this.gate = gate;
        this.aspects = aspects;
    }

    protected static NonNullList<ItemStack> defaultCraftingReminder(IArcaneCraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int slot = 0; slot < result.size(); ++slot) {
            ItemStack item = input.getItem(slot);
            ItemStackTemplate remainder = item.getCraftingRemainder();
            result.set(slot, remainder != null ? remainder.create() : ItemStack.EMPTY);
        }

        return result;
    }

    public final boolean showNotification() {
        return this.commonInfo.showNotification();
    }

    protected abstract PlacementInfo createPlacementInfo();

    public final PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = this.createPlacementInfo();
        }

        return this.placementInfo;
    }

    @Override
    public RecipeType<ArcaneCraftingRecipe> getType() {
        return TCRecipeTypes.ARCANE.get();
    }

    @Override
    public abstract RecipeSerializer<? extends ArcaneCraftingRecipe> getSerializer();

    public NonNullList<ItemStack> getRemainingItems(IArcaneCraftingInput input) {
        return defaultCraftingReminder(input);
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public int getBaseVis() {
        return vis;
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return gate;
    }

    public List<SlotDisplay> crystalDisplays() {
        List<SlotDisplay> displays = new ArrayList<>();
        for (AspectInstance entry : aspects.entries()) {
            ItemStack crystal = EssentiaCrystalFactory.of(entry.aspect(), entry.amount());
            displays.add(new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(crystal.typeHolder(), crystal.getCount(), crystal.getComponentsPatch())));
        }
        return displays;
    }

    @Override
    public AspectList getCrystals() {
        return aspects;
    }

    @Override
    public boolean matches(IArcaneCraftingInput input, Level level) {
        WorkbenchPayment.Plan plan = WorkbenchPayment.plan(this, input, input.player());
        return plan.crystalsSatisfied();
    }
}
