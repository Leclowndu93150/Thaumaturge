package com.leclowndu93150.thaumaturge.api.spell.event;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired on the game bus before any node runs. Listeners may edit the target list the node receives.
 * Cancelling skips the node and everything below it on this path.
 *
 * @since 1.0.0
 */
public final class SpellNodeEvent extends Event implements ICancellableEvent {
    private final CastContext context;
    private final List<SpellTarget> targets;

    /**
     * Creates the event.
     *
     * @param context the node's context
     * @param targets the targets the node will receive; copied
     */
    public SpellNodeEvent(CastContext context, List<SpellTarget> targets) {
        this.context = context;
        this.targets = new ArrayList<>(targets);
    }

    /**
     * The node's context.
     *
     * @return the context
     */
    public CastContext context() {
        return context;
    }

    /**
     * The mutable list of targets the node will receive.
     *
     * @return the targets
     */
    public List<SpellTarget> targets() {
        return targets;
    }
}
