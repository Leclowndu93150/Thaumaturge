package com.leclowndu93150.thaumaturge.api.spell.behavior;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * What a node does. Behaviours are configured once from JSON and shared by every spell using the
 * part, so they must keep no per-cast state; everything per cast comes through
 * {@link CastContext}.
 *
 * @see AbstractEffectBehavior
 * @see AbstractModifierBehavior
 * @since 1.0.0
 */
public interface SpellBehavior {
    /**
     * The type this behaviour was decoded with.
     *
     * @return the type
     */
    SpellBehaviorType<?> type();

    /**
     * Runs the node.
     *
     * @param ctx      the node's context
     * @param incoming the targets handed down by the parent
     */
    void execute(CastContext ctx, List<SpellTarget> incoming);

    /**
     * The most children a node of this behaviour may have.
     *
     * @return the maximum, 1 for chains, 0 for terminal nodes, more for branches
     */
    default int maxChildren() {
        return 1;
    }

    /**
     * The fewest children a finished node needs; fewer is reported as an empty branch.
     *
     * @return the minimum
     */
    default int minChildren() {
        return 0;
    }

    /**
     * Whether the node achieves something as the last node of a path. A path ending in a node that
     * does not is reported as leading nowhere.
     *
     * @return true when a leaf of this behaviour is useful
     */
    default boolean standalone() {
        return type().kind() == SpellPartKind.EFFECT;
    }

    /**
     * How many extra times the node re-runs its children, counted against the focus's repeat
     * limit.
     *
     * @param settings reads the node's resolved settings by key
     * @return the repeat count, 0 by default
     */
    default int repeats(ToIntFunction<String> settings) {
        return 0;
    }
}
