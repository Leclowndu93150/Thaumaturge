package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

/**
 * An external supplier of primal vis toward an arcane craft. Registered sources are consulted by
 * the workbench payment planner after the wand in the wand slot and before the loaded crystals, so
 * that an addon (for example a networked storage system near the workbench) can pay a primal's
 * share of a craft instead of a crystal.
 *
 * <p>The planner sizes a craft with simulated calls, and a craft simulates every payment before it
 * pays anything. A simulated call must not change any state. A real call must supply exactly what
 * the matching simulated call reported when nothing changed in between; the craft fails otherwise.
 * Sources are only consulted on the server, when the craft has an {@link ArcaneWorkbenchContext}.
 *
 * <p>Register sources through {@link RegisterWorkbenchVisSourcesEvent}.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface IWorkbenchVisSource {
    /**
     * Supplies up to {@code need} centivis of the given aspect toward a craft.
     *
     * @param context   where and for whom the craft runs
     * @param player    the crafting player
     * @param workbench the workbench inventory
     * @param aspect    the primal aspect required
     * @param need      the centivis still required for this aspect
     * @param simulate  true to report what would be supplied without changing anything
     * @return the centivis supplied, never more than {@code need}; larger or negative values are
     *         clamped by the caller
     */
    int supply(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneWorkbench workbench,
            Holder<IAspect> aspect,
            int need,
            boolean simulate);
}
