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

public final class FanModifier extends AbstractModifierBehavior {
    public static final MapCodec<FanModifier> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.floatRange(1.0F, 16.0F).optionalFieldOf("shared_count", 2.0F).forGetter(FanModifier::shareDivisor)).apply(i, FanModifier::new));

    private static final String COUNT = "count";
    private static final String ARC = "arc";

    private final float shareDivisor;

    public FanModifier(float shareDivisor) {
        this.shareDivisor = shareDivisor;
    }

    public float shareDivisor() {
        return shareDivisor;
    }

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.FAN.get();
    }

    @Override
    protected SpellState modify(CastContext ctx, SpellState state) {
        return state.multiply(SpellStats.POWER, Math.min(1.0F, shareDivisor / Math.max(1, ctx.setting(COUNT))));
    }

    @Override
    protected List<SpellTarget> transform(CastContext ctx, List<SpellTarget> incoming) {
        int count = Math.max(1, ctx.setting(COUNT));
        float arc = ctx.setting(ARC) * Mth.DEG_TO_RAD;
        List<SpellTarget> out = new ArrayList<>(incoming.size() * count);
        for (SpellTarget target : incoming) {
            for (int index = 0; index < count; index++) {
                float offset = count == 1 ? 0.0F : arc * (index / (float) (count - 1) - 0.5F);
                out.add(target.moved(target.position(), Cones.yaw(target.direction(), offset)));
            }
        }
        return out;
    }
}
