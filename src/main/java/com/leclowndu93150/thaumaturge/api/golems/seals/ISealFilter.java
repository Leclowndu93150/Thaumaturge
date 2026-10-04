package com.leclowndu93150.thaumaturge.api.golems.seals;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * The item filter of a placed seal: a row of ghost slots, an optional amount per slot and a whitelist/blacklist switch.
 *
 * <p>Changes are not saved or synced by themselves; call {@link ISealEntity#markChanged} afterwards.
 *
 * @since 1.0.0
 */
public interface ISealFilter {
    /**
     * @return the filter shape declared by the seal type
     */
    SealFilterSpec spec();

    /**
     * @param slot the slot, from 0 to {@code spec().slots() - 1}
     * @return the ghost stack in the slot, or an empty stack
     */
    ItemStack stack(int slot);

    /**
     * @param slot  the slot
     * @param stack the ghost stack; a copy is stored
     */
    void setStack(int slot, ItemStack stack);

    /**
     * @param slot the slot
     * @return the slot's amount, meaningful only when {@link #usesLimits()} is true
     */
    int limit(int slot);

    /**
     * @param slot  the slot
     * @param limit the slot's amount
     */
    void setLimit(int slot, int limit);

    /**
     * @return whether the filter excludes the listed items rather than allowing only them; always false in
     *         {@link SealFilterMode#WHITELIST_WITH_LIMITS} mode
     */
    boolean isBlacklist();

    /**
     * @param blacklist whether the filter excludes the listed items
     */
    void setBlacklist(boolean blacklist);

    /**
     * @return whether slot amounts currently apply
     */
    default boolean usesLimits() {
        return spec().mode().usesLimits(isBlacklist());
    }

    /**
     * @return a read-only live view of the ghost stacks, one per slot
     */
    List<ItemStack> stacks();

    /**
     * @return a read-only live view of the slot amounts, one per slot
     */
    List<Integer> limits();
}
