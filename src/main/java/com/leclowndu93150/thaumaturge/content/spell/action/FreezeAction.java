package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FreezeAction(float ticks, float perDuration) implements SpellAction {
    public static final MapCodec<FreezeAction> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("ticks", 0.0F).forGetter(FreezeAction::ticks), Codec.FLOAT.optionalFieldOf("per_duration", 20.0F).forGetter(FreezeAction::perDuration))
                    .apply(i, FreezeAction::new));

    private static final int THAW_PER_TICK = 2;

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.FREEZE.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        ActionTargets.living(ctx).filter(living -> living.canFreeze()).ifPresent(living -> {
            int frozen = Math.round(ActionTargets.lasting(ticks, perDuration, ctx));
            living.setTicksFrozen(Math.max(living.getTicksFrozen(), living.getTicksRequiredToFreeze() + frozen * THAW_PER_TICK));
        });
    }
}
