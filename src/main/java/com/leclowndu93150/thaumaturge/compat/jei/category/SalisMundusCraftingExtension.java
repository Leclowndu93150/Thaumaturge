package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.content.recipe.SalisMundusRecipe;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public final class SalisMundusCraftingExtension implements ICraftingCategoryExtension<SalisMundusRecipe> {
    public static final SalisMundusCraftingExtension INSTANCE = new SalisMundusCraftingExtension();

    private SalisMundusCraftingExtension() {}

    private static final int CRYSTAL_SLOTS = 3;

    @Override
    public List<SlotDisplay> getIngredients(RecipeHolder<SalisMundusRecipe> recipeHolder) {
        List<SlotDisplay> crystals = crystalDisplays();
        List<SlotDisplay> slots = new ArrayList<>();
        slots.add(new SlotDisplay.ItemSlotDisplay(Items.FLINT));
        slots.add(new SlotDisplay.ItemSlotDisplay(Items.BOWL));
        slots.add(new SlotDisplay.ItemSlotDisplay(Items.REDSTONE));
        for (int slot = 0; slot < CRYSTAL_SLOTS; slot++) {
            slots.add(rotated(crystals, slot));
        }
        return slots;
    }

    private static SlotDisplay rotated(List<SlotDisplay> displays, int offset) {
        if (displays.size() <= 1) {
            return displays.isEmpty() ? SlotDisplay.Empty.INSTANCE : displays.get(0);
        }
        List<SlotDisplay> shifted = new ArrayList<>(displays.size());
        for (int i = 0; i < displays.size(); i++) {
            shifted.add(displays.get((i + offset) % displays.size()));
        }
        return new SlotDisplay.Composite(shifted);
    }

    private static List<SlotDisplay> crystalDisplays() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            List<SlotDisplay> variants = new ArrayList<>();
            for (ItemStack crystal : EssentiaCrystalFactory.discoveredCrystals(player)) {
                variants.add(new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(crystal)));
            }
            if (!variants.isEmpty()) {
                return variants;
            }
        }
        return List.of(new SlotDisplay.ItemSlotDisplay(TTItems.ESSENTIA_CRYSTAL.get()));
    }
}
