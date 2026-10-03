package com.leclowndu93150.thaumaturge.api.items;

import net.minecraft.world.item.ItemStack;

/**
 * Chest gear that lets its wearer hover while it holds fuel, such as the Thaumostatic Harness.
 *
 * <p>The hover system only consults gear worn in the chest slot. Hovering is toggled by the wearer and
 * drains one unit of fuel at a fixed interval while active; once {@link #getHoverFuel(ItemStack)} reaches
 * zero the wearer stops hovering.
 *
 * @apiNote Fuel accessors are called on both logical sides. Implementations must read fuel from data
 *          synchronized with the stack so the client sees the same amount as the server.
 * @since 1.0.0
 */
public interface IHoverGear {
    /**
     * Returns the fuel currently stored in the gear.
     *
     * @param stack the worn stack
     * @return the remaining fuel units, never negative
     */
    int getHoverFuel(ItemStack stack);

    /**
     * Returns the largest amount of fuel the gear can hold, used to scale fuel displays.
     *
     * @param stack the worn stack
     * @return the fuel capacity, always positive
     */
    int getMaxHoverFuel(ItemStack stack);

    /**
     * Removes one unit of fuel from the gear. Does nothing when the gear is already empty.
     *
     * @param stack the worn stack, mutated in place
     * @implNote Called on the logical server only.
     */
    void consumeHoverFuel(ItemStack stack);
}
