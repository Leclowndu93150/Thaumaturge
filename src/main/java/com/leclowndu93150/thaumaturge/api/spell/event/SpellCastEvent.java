package com.leclowndu93150.thaumaturge.api.spell.event;

import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired on the game bus around a cast started by a caster item or a mob. Server side only.
 *
 * @since 1.0.0
 */
public abstract sealed class SpellCastEvent extends Event permits SpellCastEvent.Pre, SpellCastEvent.Post {
    private final LivingEntity caster;
    private final ItemStack focus;
    private final Spell spell;

    /**
     * Creates the event.
     *
     * @param caster the casting entity
     * @param focus  the focus the spell comes from, empty for mob casts
     * @param spell  the spell
     */
    protected SpellCastEvent(LivingEntity caster, ItemStack focus, Spell spell) {
        this.caster = caster;
        this.focus = focus;
        this.spell = spell;
    }

    /**
     * The casting entity.
     *
     * @return the caster
     */
    public LivingEntity caster() {
        return caster;
    }

    /**
     * The focus the spell comes from.
     *
     * @return the focus stack, empty for mob casts
     */
    public ItemStack focus() {
        return focus;
    }

    /**
     * The spell being cast.
     *
     * @return the spell
     */
    public Spell spell() {
        return spell;
    }

    /**
     * The cast style.
     *
     * @return the spell's style
     */
    public CastStyle style() {
        return spell.style();
    }

    /**
     * Fired before vis is paid. Cancelling stops the cast without paying or starting a cooldown.
     *
     * @since 1.0.0
     */
    public static final class Pre extends SpellCastEvent implements ICancellableEvent {
        private float power;
        private float visCost;

        /**
         * Creates the event.
         *
         * @param caster  the casting entity
         * @param focus   the focus, empty for mob casts
         * @param spell   the spell
         * @param power   the starting power multiplier (charge, channel pulse)
         * @param visCost the vis this cast will drain, 0 for mob casts
         */
        public Pre(LivingEntity caster, ItemStack focus, Spell spell, float power, float visCost) {
            super(caster, focus, spell);
            this.power = power;
            this.visCost = visCost;
        }

        /**
         * The power multiplier the spell starts with.
         *
         * @return the multiplier
         */
        public float power() {
            return power;
        }

        /**
         * Replaces the starting power multiplier.
         *
         * @param power the multiplier, clamped to zero or more
         */
        public void setPower(float power) {
            this.power = Math.max(0.0F, power);
        }

        /**
         * The vis the cast will drain before discounts.
         *
         * @return the vis cost
         */
        public float visCost() {
            return visCost;
        }

        /**
         * Replaces the vis cost.
         *
         * @param visCost the vis, clamped to zero or more
         */
        public void setVisCost(float visCost) {
            this.visCost = Math.max(0.0F, visCost);
        }
    }

    /**
     * Fired after the spell ran its instant part. Carriers launched by the spell may still be in
     * flight.
     *
     * @since 1.0.0
     */
    public static final class Post extends SpellCastEvent {
        /**
         * Creates the event.
         *
         * @param caster the casting entity
         * @param focus  the focus, empty for mob casts
         * @param spell  the spell
         */
        public Post(LivingEntity caster, ItemStack focus, Spell spell) {
            super(caster, focus, spell);
        }
    }
}
