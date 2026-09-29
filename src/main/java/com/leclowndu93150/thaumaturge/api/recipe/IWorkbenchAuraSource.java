package com.leclowndu93150.thaumaturge.api.recipe;

import net.minecraft.server.level.ServerPlayer;

/**
 * A supplier of aura vis for arcane crafts that do not run at a Thaumaturge arcane workbench.
 * A placed arcane workbench always pays the aura part of a craft from the chunk aura around it;
 * every other host, placed or virtual, pays it from the registered aura sources in registration
 * order. A craft with an aura cost fails when the sources together cannot cover it.
 *
 * <p>Supply follows the same simulate contract as {@link IWorkbenchVisSource}: a simulated call
 * changes nothing, and a real call supplies what the matching simulated call reported.
 *
 * <p>Register sources through {@link RegisterWorkbenchAuraSourcesEvent}.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface IWorkbenchAuraSource {
    /**
     * Supplies up to {@code need} aura vis toward a craft.
     *
     * @param context   where and for whom the craft runs
     * @param player    the crafting player
     * @param workbench the workbench inventory
     * @param need      the aura vis still required
     * @param simulate  true to report what would be supplied without changing anything
     * @return the vis supplied, never more than {@code need}; larger or negative values are
     *         clamped by the caller
     */
    int supply(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneWorkbench workbench,
            int need,
            boolean simulate);
}
