package com.leclowndu93150.thaumaturge.api.wands;

import net.minecraft.world.item.ItemStack;

/**
 * Optional storage override attached to a {@link WandRod}. When a rod declares one, every read and
 * write of the vis on a wand built from that rod goes through it instead of the
 * {@code thaumaturge:wand_vis} data component. This covers casting, crafting, charging,
 * {@link WandAccess} and the HUD.
 *
 * <p>Both methods run on either logical side: the client reads vis for tooltips, the vis bar and
 * the caster HUD. Keep the backing data on the stack in a network-synchronized data component so
 * the client sees the same numbers as the server.
 *
 * @since 1.0.0
 */
public interface IWandVisStorage {
    /**
     * Returns the vis currently stored on the wand.
     *
     * @param wand the wand stack
     * @return the stored vis, never null; {@link WandVis#EMPTY} when the wand holds none
     */
    WandVis getVis(ItemStack wand);

    /**
     * Replaces the vis stored on the wand. Charging and filling clamp amounts to the wand's
     * capacity before calling this; {@link WandAccess#withVis} passes raw amounts through.
     *
     * @param wand the wand stack to modify in place
     * @param vis  the new vis storage
     */
    void setVis(ItemStack wand, WandVis vis);
}
