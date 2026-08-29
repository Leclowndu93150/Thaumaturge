package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;

/**
 * An external supplier of primal vis toward an arcane craft. Registered sources are consulted by
 * the workbench payment planner after the player's wand and the loaded crystals, but before the
 * block entity's aura buffer, so that an addon (for example a networked storage system adjacent to
 * the workbench) can pay part of a craft's cost.
 *
 * <p>Sources are consulted with {@code simulate == true} while the planner sizes the cost and again
 * with {@code simulate == false} at commit time. A source must return the same value for identical
 * inputs across a simulate and its matching commit, and must only mutate its own state on commit.
 *
 * <p>Register sources through {@link RegisterWorkbenchVisSourcesEvent} on the mod event bus.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface IWorkbenchVisSource {
    /**
     * Context-aware form used by arcane-crafting transactions.
     *
     * <p>The default preserves compatibility with sources compiled against the original API.
     */
    default int supply(
            ArcaneWorkbenchContext context,
            Player player,
            IArcaneWorkbench workbench,
            Holder<IAspect> aspect,
            int need,
            boolean simulate) {
        return supply(player, workbench, aspect, need, simulate);
    }

    /**
     * Supplies up to {@code need} centivis of the given aspect toward a craft at the workbench.
     *
     * @param player    the crafting player
     * @param workbench the workbench inventory
     * @param aspect    the primal aspect required
     * @param need      the centivis still required for this aspect
     * @param simulate  when true, do not modify source state; only report what would be supplied
     * @return the centivis supplied, never more than {@code need}
     */
    int supply(Player player, IArcaneWorkbench workbench, Holder<IAspect> aspect, int need, boolean simulate);
}
