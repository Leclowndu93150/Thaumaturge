package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** An immutable snapshot of the single items offered by an infusion altar. */
public final class InfusionRecipeInput implements RecipeInput {
    private final ItemStack catalyst;
    private final List<ItemStack> components;

    public InfusionRecipeInput(ItemStack catalyst, List<ItemStack> components) {
        if (components.size() > 64) {
            throw new IllegalArgumentException("Infusion supports at most 64 components");
        }
        this.catalyst = catalyst.copyWithCount(1);
        this.components = components.stream().map(stack -> stack.copyWithCount(1)).toList();
    }

    public ItemStack catalyst() {
        return catalyst.copy();
    }

    public List<ItemStack> components() {
        return components.stream().map(ItemStack::copy).toList();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? catalyst() : components.get(index - 1).copy();
    }

    @Override
    public int size() {
        return components.size() + 1;
    }
}
