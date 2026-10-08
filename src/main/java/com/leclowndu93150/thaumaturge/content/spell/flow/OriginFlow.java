package com.leclowndu93150.thaumaturge.content.spell.flow;

import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.MapCodec;
import java.util.List;

public record OriginFlow() implements SpellBehavior {
    public static final MapCodec<OriginFlow> CODEC = MapCodec.unit(new OriginFlow());

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.ORIGIN.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        ctx.proceed(incoming);
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
