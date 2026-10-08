package com.leclowndu93150.thaumaturge.api.spell.affinity;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import net.minecraft.core.Holder;

/**
 * The magnitudes one affinity action works with, computed by the aspect-driven effect that runs
 * it.
 *
 * @param cast     the effect node's context
 * @param target   the target being affected
 * @param aspect   the aspect whose affinity runs
 * @param power    the magnitude; the effect's power setting times the power stat
 * @param duration the duration factor; the effect's duration setting times the duration stat
 * @param radius   the area radius for block reactions, 0 for a single block
 * @since 1.0.0
 */
public record ActionContext(
        CastContext cast, SpellTarget target, Holder<IAspect> aspect, float power, float duration, float radius) {}
