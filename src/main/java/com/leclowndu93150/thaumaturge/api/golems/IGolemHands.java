package com.leclowndu93150.thaumaturge.api.golems;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * The carry slots of a golem. A golem has one hand, or two when it has the hauler trait.
 *
 * <p>All methods are server-side operations on live entity equipment. They mutate the stacks passed in only where documented.
 *
 * @since 1.0.0
 */
public interface IGolemHands {
    /**
     * Stores as much of a stack as fits, filling a matching partial stack before an empty hand.
     *
     * @param stack the stack to store; shrunk by the amount stored
     * @return the remainder that did not fit, which is {@code stack} itself when anything is left
     */
    ItemStack hold(ItemStack stack);

    /**
     * Takes items out of the hands.
     *
     * @param wanted the stack to match and count against, or an empty stack to release the first held stack whole
     * @return the released items, or an empty stack when nothing matched
     */
    ItemStack release(ItemStack wanted);

    /**
     * @param stack the stack to measure
     * @return how many items of that kind the hands can still take
     */
    int room(ItemStack stack);

    /**
     * @param stack   the stack to test
     * @param partial whether taking only part of the stack counts
     * @return whether the hands can take the stack
     */
    default boolean canTake(ItemStack stack, boolean partial) {
        int room = room(stack);
        return room > 0 && (partial || room >= stack.getCount());
    }

    /**
     * @param stack the stack to look for, compared by item and components
     * @return whether a hand holds a matching stack; always false for an empty stack
     */
    boolean holds(ItemStack stack);

    /**
     * @return the live stacks in each hand, main hand first; never empty, and may contain empty stacks
     */
    List<ItemStack> contents();
}
