package com.leclowndu93150.thaumaturge.content.spell.flow;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import java.util.List;

public record DelayFlow() implements SpellBehavior {
    public static final MapCodec<DelayFlow> CODEC = MapCodec.unit(new DelayFlow());

    private static final String SETTING = "ticks";

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.DELAY.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        ctx.proceedLater(Math.max(1, ctx.setting(SETTING)), incoming, ctx.state());
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
