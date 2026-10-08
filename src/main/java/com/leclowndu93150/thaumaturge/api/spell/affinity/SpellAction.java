package com.leclowndu93150.thaumaturge.api.spell.affinity;

/**
 * One reaction in an {@link AspectAffinity}: deal damage, ignite, grow a crop. Actions are
 * configured from JSON and shared, so they keep no per-cast state.
 *
 * @since 1.0.0
 */
public interface SpellAction {
    /**
     * The type this action was decoded with.
     *
     * @return the type
     */
    SpellActionType<?> type();

    /**
     * Runs the action. Entity actions ignore block targets and the reverse; actions must check
     * the target kind themselves.
     *
     * @param ctx the magnitudes and target
     */
    void apply(ActionContext ctx);
}
