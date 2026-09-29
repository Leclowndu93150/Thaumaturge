package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;

/**
 * Essentia storage in one item stack that automation may fill and drain, such as a warded jar.
 *
 * <p>This is separate from {@link IEssentiaContainerItem}, which also covers items that only carry
 * aspects for scanning; having this capability is what permits transfers.
 *
 * <p>No method changes the stack the capability was obtained from. A transfer returns the stack
 * that should replace it, and the caller commits the transfer by putting that stack into the
 * source slot, or discards the result to leave everything unchanged. Transfers only work on a
 * stack of one item, because a stack of several containers cannot be replaced by one result.
 *
 * @since 1.0.0
 */
public interface IEssentiaItemStorage {
    /**
     * An immutable snapshot of the stored essentia.
     *
     * @return the contents, never {@code null}
     */
    AspectList contents();

    /**
     * How much of an aspect the item holds when full.
     *
     * @param aspect the aspect
     * @return the capacity, or zero when the stack cannot hold it
     */
    int capacity(Holder<IAspect> aspect);

    /**
     * Whether the item could take the aspect now, given its filter and current contents.
     *
     * @param aspect the aspect
     * @return true when an insertion of this aspect can succeed
     */
    boolean canInsert(Holder<IAspect> aspect);

    /**
     * Works out an insertion of up to {@code amount} of an aspect. Items that only hold full loads,
     * such as phials, accept either a full load or nothing.
     *
     * @param aspect the aspect supplied
     * @param amount the most to insert
     * @return the amount that moves and the stack to put in the source slot
     */
    ItemEssentiaTransferResult insert(Holder<IAspect> aspect, int amount);

    /**
     * Works out an extraction of up to {@code amount} of an aspect. Items that only hold full
     * loads give up their whole load or nothing.
     *
     * @param aspect the aspect requested
     * @param amount the most to extract
     * @return the amount that moves and the stack to put in the source slot
     */
    ItemEssentiaTransferResult extract(Holder<IAspect> aspect, int amount);

    /**
     * Plays this item's feedback for a committed transfer, such as a sound. The default does
     * nothing. Call it through {@link EssentiaTransferFeedback}, which skips empty transfers and
     * the client.
     *
     * @param player    the player the transfer was for
     * @param direction whether the item was filled or drained
     */
    default void playTransferFeedback(Player player, TransferDirection direction) {}

    /**
     * Which way essentia moved.
     *
     * @since 1.0.0
     */
    enum TransferDirection {
        /** Essentia went into the item. */
        FILL,
        /** Essentia came out of the item. */
        DRAIN
    }
}
