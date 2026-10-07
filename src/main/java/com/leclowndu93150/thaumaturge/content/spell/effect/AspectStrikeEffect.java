package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public final class AspectStrikeEffect extends AbstractEffectBehavior {
    public static final MapCodec<AspectStrikeEffect> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("default_duration", 2.0F).forGetter(AspectStrikeEffect::defaultDuration)).apply(i, AspectStrikeEffect::new));

    private final float defaultDuration;

    public AspectStrikeEffect(float defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    public float defaultDuration() {
        return defaultDuration;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.ASPECT_STRIKE.get();
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        Optional<AspectAffinity> affinity = AffinityRunner.affinity(ctx);
        if (affinity.isEmpty()) {
            return;
        }
        ctx.fx().impact(affinity.get().fx(), target.position(), ctx.color());
        AffinityRunner.strike(ctx, affinity.get(), target, AffinityRunner.magnitude(ctx, power), AffinityRunner.duration(ctx, defaultDuration), ctx.state().get(SpellStats.RADIUS));
    }
}
