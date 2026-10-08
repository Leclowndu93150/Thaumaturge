package com.leclowndu93150.thaumaturge.api.research.scan;

import net.minecraft.world.item.ItemStack;

/**
 * A scan of an item stack, from an inventory slot or from inside a scanned container.
 *
 * @param stack the stack
 * @since 1.0.0
 */
public record ScannedStack(ItemStack stack) implements ScanTarget {
    @Override
    public ItemStack carriedStack() {
        return stack;
    }
}
