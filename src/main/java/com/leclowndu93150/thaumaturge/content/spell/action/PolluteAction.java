package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PolluteAction(float base, float perPower, float chance) implements SpellAction {
    public static final MapCodec<PolluteAction> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("base", 0.0F).forGetter(PolluteAction::base), Codec.FLOAT.optionalFieldOf("per_power", 0.1F).forGetter(PolluteAction::perPower),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(PolluteAction::chance)).apply(i, PolluteAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.POLLUTE.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        if (ctx.cast().random().nextFloat() < chance) {
            AuraHelper.polluteAura(ctx.cast().level(), ctx.target().blockPos(), ActionTargets.magnitude(base, perPower, ctx), true);
        }
    }
}
