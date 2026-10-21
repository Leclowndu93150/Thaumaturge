package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import net.minecraft.world.item.ItemStack;

/** The exact result and unmodified cost for one immutable set of example inputs. */
public record InfusionRecipePreview(InfusionRecipeInput input, ItemStack output, AspectList essentia, int instability) {
    public InfusionRecipePreview {
        if (output.isEmpty() || output.getCount() > output.getMaxStackSize() || essentia.entries().size() > 64 || instability < 0 || instability > 100) {
            throw new IllegalArgumentException("Invalid infusion preview");
        }
        output = output.copy();
    }

    @Override
    public ItemStack output() {
        return output.copy();
    }
}
