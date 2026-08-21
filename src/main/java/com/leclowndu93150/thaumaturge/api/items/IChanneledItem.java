package com.leclowndu93150.thaumaturge.api.items;

import net.minecraft.world.item.ItemStack;

/**
 * An item whose continuous use is a channel that must not keep running while its holder has a
 * screen open.
 *
 * <p>The vanilla client only releases item use from its in-game key handler, and that handler is
 * skipped entirely while any screen is open. A channel started before a screen opens therefore
 * keeps ticking on both sides until the screen closes again, even after the use key is let go.
 * Items implementing this interface have their use released as soon as a screen opens, through the
 * same path the key handler uses.
 *
 * @since 1.0.0
 */
public interface IChanneledItem {
    /**
     * Returns whether the given stack's channel ends when its holder opens a screen.
     *
     * @param stack the stack currently being used
     * @return {@code true} to release the item use when a screen opens
     */
    default boolean releasesOnScreenOpen(ItemStack stack) {
        return true;
    }
}
