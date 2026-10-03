package com.leclowndu93150.thaumaturge.api.casters;

import java.util.Set;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * An element that carries the spell from rays to hits, such as touch, bolt, or projectile.
 *
 * <p>A medium either resolves its output immediately, or defers by launching an
 * intermediary such as a projectile entity: it spawns the carrier with
 * {@link CastContext#continuation()} and returns null, which halts the current branch. The
 * intermediary later resumes the spell through
 * {@link FocusEngine#run(Level, FocusPackage, CastStreams)}.
 *
 * @since 1.0.0
 */
public non-sealed interface FocusMedium extends FocusElement {
    @Override
    default Set<SupplyType> requires() {
        return SUPPLIES_TRAJECTORIES;
    }

    @Override
    default Set<SupplyType> supplies() {
        return SUPPLIES_TARGETS;
    }

    /**
     * Executes this medium over the incoming streams.
     *
     * @param ctx      the execution scope
     * @param settings the configured settings
     * @param incoming the streams supplied by the previous node
     * @return the streams for the next node, or null to halt the branch because target
     *         resolution was deferred to an intermediary or the medium could not act
     */
    @Nullable
    CastStreams cast(CastContext ctx, FocusSettings settings, CastStreams incoming);
}
