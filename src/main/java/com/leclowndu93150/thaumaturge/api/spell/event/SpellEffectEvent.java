package com.leclowndu93150.thaumaturge.api.spell.event;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired on the game bus before an effect node acts on one target, after the touch budget admitted
 * it. Cancelling skips this target only; the spell still continues to the node's children.
 *
 * @since 1.0.0
 */
public final class SpellEffectEvent extends Event implements ICancellableEvent {
    private final CastContext context;
    private final SpellTarget target;
    private float power;

    /**
     * Creates the event.
     *
     * @param context the effect node's context
     * @param target  the target about to be affected
     * @param power   the power the effect will use
     */
    public SpellEffectEvent(CastContext context, SpellTarget target, float power) {
        this.context = context;
        this.target = target;
        this.power = power;
    }

    /**
     * The effect node's context: part, aspect, settings, caster and state.
     *
     * @return the context
     */
    public CastContext context() {
        return context;
    }

    /**
     * The target about to be affected.
     *
     * @return the target
     */
    public SpellTarget target() {
        return target;
    }

    /**
     * The power the effect will use.
     *
     * @return the power
     */
    public float power() {
        return power;
    }

    /**
     * Replaces the power the effect will use.
     *
     * @param power the new power, clamped to zero or more
     */
    public void setPower(float power) {
        this.power = Math.max(0.0F, power);
    }
}
