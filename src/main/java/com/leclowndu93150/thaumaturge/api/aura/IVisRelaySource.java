package com.leclowndu93150.thaumaturge.api.aura;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

/**
 * Addon-owned typed-centivis source usable as a root of the native vis-relay graph.
 *
 * <p>All calls are on the owning server thread. {@link #reserve} must not mutate storage. A live
 * reservation freezes its quoted amount until commit or close; commit drains it exactly once.
 * Different aspects must be reserved independently.
 */
public interface IVisRelaySource {
    /** Whether the host still exists and owns this source. */
    boolean isValid();

    /** Whether the host is currently powered/active. */
    boolean isActive();

    /** Whether its remote transport/link is currently usable. */
    boolean isLinked();

    /** Returns a non-mutating reservation, or null when none can be supplied. */
    @Nullable
    Reservation reserve(ResourceKey<IAspect> primal, int requested);

    interface Reservation extends AutoCloseable {
        /** Quoted centivis. Thaumaturge clamps this to the original request. */
        int amount();

        /** Drains the reserved centivis once and returns the amount actually drained. */
        int commit();

        /** Releases an uncommitted reservation without draining. */
        @Override
        void close();
    }
}
