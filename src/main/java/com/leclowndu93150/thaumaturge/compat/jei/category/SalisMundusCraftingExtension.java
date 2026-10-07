package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.content.recipe.SalisMundusRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class SalisMundusCraftingExtension implements ICraftingCategoryExtension<SalisMundusRecipe> {
    public static final SalisMundusCraftingExtension INSTANCE = new SalisMundusCraftingExtension();

    private static final int CRYSTAL_SLOTS = 3;

    private SalisMundusCraftingExtension() {}

    @Override
    public void setRecipe(
            RecipeHolder<SalisMundusRecipe> recipeHolder,
            IRecipeLayoutBuilder builder,
            ICraftingGridHelper craftingGridHelper,
            IFocusGroup focuses) {
        List<ItemStack> crystals = crystalStacks();
        List<List<ItemStack>> inputs = new ArrayList<>();
        inputs.add(List.of(new ItemStack(Items.FLINT)));
        inputs.add(List.of(new ItemStack(Items.BOWL)));
        inputs.add(List.of(new ItemStack(Items.REDSTONE)));
        for (int slot = 0; slot < CRYSTAL_SLOTS; slot++) {
            inputs.add(rotated(crystals, slot));
        }
        craftingGridHelper.createAndSetInputs(builder, inputs, 0, 0);
        craftingGridHelper.createAndSetOutputs(builder, List.of(new ItemStack(TTItems.SALIS_MUNDUS.get())));
    }

    private static List<ItemStack> rotated(List<ItemStack> stacks, int offset) {
        if (stacks.size() <= 1) {
            return stacks;
        }
        List<ItemStack> shifted = new ArrayList<>(stacks.size());
        for (int i = 0; i < stacks.size(); i++) {
            shifted.add(stacks.get((i + offset) % stacks.size()));
        }
        return shifted;
    }

    private static List<ItemStack> crystalStacks() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            List<ItemStack> variants = EssentiaCrystalFactory.discoveredCrystals(player);
            if (!variants.isEmpty()) {
                return variants;
            }
        }
        return List.of(new ItemStack(TTItems.ESSENTIA_CRYSTAL.get()));
    }
}
