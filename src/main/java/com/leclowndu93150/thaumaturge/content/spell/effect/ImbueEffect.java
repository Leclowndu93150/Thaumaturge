package com.leclowndu93150.thaumaturge.content.spell.effect;

import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractEffectBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public final class ImbueEffect extends AbstractEffectBehavior {
    public static final MapCodec<ImbueEffect> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("default_duration", 3.0F).forGetter(ImbueEffect::defaultDuration)).apply(i, ImbueEffect::new));

    private static final String POTENCY = "potency";

    private final float defaultDuration;

    public ImbueEffect(float defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    public float defaultDuration() {
        return defaultDuration;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.IMBUE.get();
    }

    @Override
    protected void apply(CastContext ctx, SpellTarget target, float power, int index) {
        Optional<AspectAffinity> affinity = AffinityRunner.affinity(ctx);
        if (affinity.isEmpty() || target.entity().isEmpty()) {
            return;
        }
        ctx.fx().impact(affinity.get().fx(), target.position(), ctx.color());
        AffinityRunner.imbue(ctx, affinity.get(), target, Math.max(1, ctx.setting(POTENCY)) * power, AffinityRunner.duration(ctx, defaultDuration));
    }
}
