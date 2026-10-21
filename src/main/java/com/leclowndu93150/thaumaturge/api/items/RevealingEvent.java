package com.leclowndu93150.thaumaturge.api.items;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * Queries derived revealing properties, such as enchantments, on either logical side.
 * Register listeners on the NeoForge event bus during mod setup. Listeners must be pure,
 * fast and safe on both sides. They may grant revealing, but cannot remove a permanent
 * goggles upgrade or another listener's grant. No result is written back to the stack.
 *
 * @since 1.0.4
 */
public final class RevealingEvent extends Event {
    private final ItemStack stack;
    private boolean revealing;

    public RevealingEvent(ItemStack stack) {
        this.stack = stack.copy();
    }

    /** @return a defensive copy of the queried stack */
    public ItemStack stack() {
        return stack.copy();
    }

    /** Grants revealing for this query only. */
    public void grant() {
        revealing = true;
    }

    /** @return whether a listener granted revealing */
    public boolean isRevealing() {
        return revealing;
    }
}
