package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;

final class AffinityRunner {
    static final String POWER = "power";
    static final String DURATION = "duration";

    private AffinityRunner() {}

    static Optional<AspectAffinity> affinity(CastContext ctx) {
        Optional<Holder<IAspect>> aspect = ctx.aspect();
        if (aspect.isEmpty() || aspect.get().unwrapKey().isEmpty()) {
            return Optional.empty();
        }
        return Spells.affinity(ctx.registries(), aspect.get().unwrapKey().get());
    }

    static float magnitude(CastContext ctx, float power) {
        return Math.max(1, ctx.setting(POWER)) * power;
    }

    static float duration(CastContext ctx, float fallback) {
        float base = ctx.part().setting(DURATION).isPresent() ? ctx.setting(DURATION) : fallback;
        return base * ctx.state().get(SpellStats.DURATION);
    }

    static void strike(
            CastContext ctx,
            AspectAffinity affinity,
            SpellTarget target,
            float magnitude,
            float duration,
            float radius) {
        ActionContext action = new ActionContext(ctx, target, ctx.aspect().orElseThrow(), magnitude, duration, radius);
        if (target.entity().isPresent()) {
            run(affinity.entity(), action);
        } else {
            run(affinity.block(), action);
        }
    }

    static void imbue(CastContext ctx, AspectAffinity affinity, SpellTarget target, float magnitude, float duration) {
        run(affinity.imbue(), new ActionContext(ctx, target, ctx.aspect().orElseThrow(), magnitude, duration, 0.0F));
    }

    private static void run(List<SpellAction> actions, ActionContext action) {
        for (SpellAction step : actions) {
            step.apply(action);
        }
    }
}
