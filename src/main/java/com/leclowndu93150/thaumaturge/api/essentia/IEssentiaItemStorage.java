package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;

/**
 * Transfer-capable essentia storage bound to one item stack.
 *
 * <p>This capability is distinct from {@link IEssentiaContainerItem}, which also covers items that
 * merely carry aspects for scanning. Presence of this capability explicitly permits automation.
 *
 * <p>Operations never mutate the stack used to obtain this capability. Callers commit a transfer
 * by replacing their source slot with {@link ItemEssentiaTransferResult#resultingStack()}. A
 * simulated operation returns the same hypothetical replacement without changing external state.
 * Implementations must reject ambiguous stacked-container transfers unless they can return a safe,
 * complete replacement for the entire source stack.
 *
 * @since 1.0.0
 */
public interface IEssentiaItemStorage {
    /** Returns an immutable snapshot of all stored essentia. */
    AspectList contents();

    /** Returns the total capacity available for the given aspect. */
    int capacity(Holder<IAspect> aspect);

    /** Returns whether the bound stack is compatible with the given aspect. */
    boolean canInsert(Holder<IAspect> aspect);

    /** Inserts up to {@code amount}, returning the amount moved and replacement stack. */
    ItemEssentiaTransferResult insert(Holder<IAspect> aspect, int amount, boolean simulate);

    /** Extracts up to {@code amount}, returning the amount moved and replacement stack. */
    ItemEssentiaTransferResult extract(Holder<IAspect> aspect, int amount, boolean simulate);
}
