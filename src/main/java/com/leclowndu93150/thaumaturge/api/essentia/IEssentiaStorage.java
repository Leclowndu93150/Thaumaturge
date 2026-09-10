package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;

/**
 * A side-bound, server-authoritative enumerable essentia storage view.
 *
 * <p>Implementations expose this contract through {@link EssentiaCapabilities#STORAGE}. The
 * capability context selects the side, so operations on a returned view require no additional
 * direction argument. Querying with a {@code null} side does not expose this capability. A view
 * may be read-only by returning zero from both transfer methods.
 *
 * <p>Transfer methods return the amount moved, or the amount that would move when simulating.
 * Simulation must not mutate contents or advance {@link #contentRevision()}.
 *
 * <p>All methods are server-thread contracts. Capability instances follow NeoForge block
 * capability lifetime rules and become invalid when their owning block entity is removed or its
 * capabilities are invalidated; consumers must not retain a view after its invalidation listener
 * fires.
 *
 * @since 1.0.0
 */
public interface IEssentiaStorage {
    /**
     * Returns an immutable snapshot of every currently stored aspect. The snapshot must not expose
     * mutable implementation state and remains safe to retain after the storage changes.
     *
     * @return all visible contents, never {@code null}
     */
    AspectList contents();

    /**
     * Returns the stored amount of one aspect.
     *
     * @param aspect the aspect queried
     * @return its amount, or zero when absent
     */
    default int amount(Holder<IAspect> aspect) {
        return contents().amountOf(aspect);
    }

    /**
     * Inserts up to {@code amount} of an aspect.
     *
     * @param aspect the aspect supplied
     * @param amount the maximum amount to insert
     * @param simulate whether to report the result without changing storage
     * @return the amount accepted or that would be accepted, between zero and {@code amount}
     */
    int insert(Holder<IAspect> aspect, int amount, boolean simulate);

    /**
     * Extracts up to {@code amount} of an aspect.
     *
     * @param aspect the aspect requested
     * @param amount the maximum amount to extract
     * @param simulate whether to report the result without changing storage
     * @return the amount extracted or that would be extracted, between zero and {@code amount}
     */
    int extract(Holder<IAspect> aspect, int amount, boolean simulate);

    /**
     * Returns a monotonically increasing revision for committed content changes.
     *
     * <p>Server-side consumers may cache {@link #contents()} while this value is unchanged.
     * Revisions are scoped to the lifetime of this capability instance; they need not survive
     * chunk unloads or server restarts. Every committed mutation that changes {@link #contents()},
     * including mutations performed outside this capability view, must advance it. Simulations,
     * loading persisted state, client synchronization and non-content configuration changes do not
     * advance it.
     *
     * @return the current content revision
     */
    long contentRevision();
}
