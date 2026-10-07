package com.leclowndu93150.thaumaturge.content.spell.modifier;

import com.leclowndu93150.thaumaturge.api.spell.behavior.AbstractModifierBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.world.Cones;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class ScatterModifier extends AbstractModifierBehavior {
    public static final MapCodec<ScatterModifier> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.floatRange(1.0F, 16.0F).optionalFieldOf("shared_forks", 2.0F).forGetter(ScatterModifier::shareDivisor)).apply(i, ScatterModifier::new));

    private static final String FORKS = "forks";
    private static final String CONE = "cone";

    private final float shareDivisor;

    public ScatterModifier(float shareDivisor) {
        this.shareDivisor = shareDivisor;
    }

    public float shareDivisor() {
        return shareDivisor;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.SCATTER.get();
    }

    @Override
    protected SpellState modify(CastContext ctx, SpellState state) {
        return state.multiply(SpellStats.POWER, Math.min(1.0F, shareDivisor / Math.max(1, ctx.setting(FORKS))));
    }

    @Override
    protected List<SpellTarget> transform(CastContext ctx, List<SpellTarget> incoming) {
        int forks = Math.max(1, ctx.setting(FORKS));
        float halfCone = ctx.setting(CONE) * Mth.DEG_TO_RAD / 2.0F;
        RandomSource random = ctx.random();
        List<SpellTarget> out = new ArrayList<>(incoming.size() * forks);
        for (SpellTarget target : incoming) {
            for (int fork = 0; fork < forks; fork++) {
                out.add(target.moved(target.position(), Cones.jitter(target.direction(), halfCone, random)));
            }
        }
        return out;
    }
}
