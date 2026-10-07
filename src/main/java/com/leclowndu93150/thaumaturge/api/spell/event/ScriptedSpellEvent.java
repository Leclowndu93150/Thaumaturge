package com.leclowndu93150.thaumaturge.api.spell.event;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.bus.api.Event;

/**
 * Fired on the game bus when a node with a scripted behaviour runs. The behaviour itself does
 * nothing: listeners matching {@link #key()} supply the logic, then the spell continues with
 * {@link #targets()} and {@link #state()} unless a listener called {@link #stop()}.
 *
 * <p>Scripted effects receive one event per target with that single target in the list, after the
 * touch budget and {@link SpellEffectEvent}; the effect's children always run with the original
 * targets, so {@link #stop()} and target edits only matter for scripted deliveries, modifiers and
 * flow nodes, which receive one event with every incoming target.
 *
 * @since 1.0.0
 */
public final class ScriptedSpellEvent extends Event {
    private final String key;
    private final CastContext context;
    private final List<SpellTarget> targets;
    private final float power;
    private SpellState state;
    private boolean stopped;

    /**
     * Creates the event.
     *
     * @param key     the script key from the part's behaviour config
     * @param context the node's context
     * @param targets the incoming targets; copied
     * @param power   the power the node acts with
     */
    public ScriptedSpellEvent(String key, CastContext context, List<SpellTarget> targets, float power) {
        this.key = key;
        this.context = context;
        this.targets = new ArrayList<>(targets);
        this.power = power;
        this.state = context.state();
    }

    /**
     * The power the node acts with: the context's power, after {@link SpellEffectEvent} listeners for
     * scripted effects.
     *
     * @return the power
     */
    public float power() {
        return power;
    }

    /**
     * The script key, e.g. {@code mypack:thunder}.
     *
     * @return the key
     */
    public String key() {
        return key;
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
     * The mutable list of targets handed to the children.
     *
     * @return the targets
     */
    public List<SpellTarget> targets() {
        return targets;
    }

    /**
     * The state handed to the children.
     *
     * @return the state
     */
    public SpellState state() {
        return state;
    }

    /**
     * Replaces the state handed to the children.
     *
     * @param state the new state
     */
    public void setState(SpellState state) {
        this.state = state;
    }

    /**
     * Ends this path of the spell after the event.
     */
    public void stop() {
        stopped = true;
    }

    /**
     * Whether a listener ended the path.
     *
     * @return true after {@link #stop()}
     */
    public boolean stopped() {
        return stopped;
    }
}
