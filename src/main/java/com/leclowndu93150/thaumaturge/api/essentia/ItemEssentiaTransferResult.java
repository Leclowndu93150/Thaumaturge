package com.leclowndu93150.thaumaturge.api.essentia;

import net.minecraft.world.item.ItemStack;

/** Result of a portable essentia storage transfer. */
public record ItemEssentiaTransferResult(int amountMoved, ItemStack resultingStack) {
    public ItemEssentiaTransferResult {
        if (amountMoved < 0) throw new IllegalArgumentException("amountMoved must not be negative");
        resultingStack = resultingStack.copy();
    }

    /** Returns a defensive copy suitable for replacing the source slot. */
    @Override
    public ItemStack resultingStack() {
        return resultingStack.copy();
    }
}
