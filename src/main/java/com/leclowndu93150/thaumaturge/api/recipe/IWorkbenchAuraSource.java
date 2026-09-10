package com.leclowndu93150.thaumaturge.api.recipe;

import net.minecraft.world.entity.player.Player;

/**
 * Host-aware source for the untyped aura-vis portion of an arcane craft.
 *
 * <p>This is deliberately separate from {@link IWorkbenchVisSource}, which substitutes typed
 * primal centivis for crystal requirements. Sources are queried in registration order. A source
 * must not mutate during simulation and must return the same clamped amount when the matching
 * commit immediately follows on the server thread.
 *
 * @since 0.3.2
 */
@FunctionalInterface
public interface IWorkbenchAuraSource {
    /**
     * Supplies up to {@code need} untyped aura vis for the exact context host.
     *
     * @return the supplied amount; invalid values are clamped to {@code [0, need]}
     */
    int supply(ArcaneWorkbenchContext context, Player player, IArcaneWorkbench workbench, int need, boolean simulate);
}
