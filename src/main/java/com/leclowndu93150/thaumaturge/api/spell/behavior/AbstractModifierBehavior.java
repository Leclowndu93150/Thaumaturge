package com.leclowndu93150.thaumaturge.api.spell.behavior;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import java.util.List;

/**
 * Base for modifier behaviours: rewrite the state, the targets or both, then run the children.
 *
 * @since 1.0.0
 */
public abstract class AbstractModifierBehavior implements SpellBehavior {
    @Override
    public final void execute(CastContext ctx, List<SpellTarget> incoming) {
        ctx.proceed(transform(ctx, incoming), modify(ctx, ctx.state()));
    }

    /**
     * Rewrites the state handed to the children.
     *
     * @param ctx   the node's context
     * @param state the incoming state
     * @return the outgoing state; the incoming one by default
     */
    protected SpellState modify(CastContext ctx, SpellState state) {
        return state;
    }

    /**
     * Rewrites the targets handed to the children.
     *
     * @param ctx      the node's context
     * @param incoming the incoming targets
     * @return the outgoing targets; the incoming ones by default
     */
    protected List<SpellTarget> transform(CastContext ctx, List<SpellTarget> incoming) {
        return incoming;
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
