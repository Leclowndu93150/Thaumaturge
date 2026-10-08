package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;

public record HealAction(float base, float perPower, float undeadMultiplier) implements SpellAction {
    public static final MapCodec<HealAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.FLOAT.optionalFieldOf("base", 0.0F).forGetter(HealAction::base),
                    Codec.FLOAT.optionalFieldOf("per_power", 1.0F).forGetter(HealAction::perPower),
                    Codec.FLOAT.optionalFieldOf("undead_multiplier", 1.5F).forGetter(HealAction::undeadMultiplier))
            .apply(i, HealAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.HEAL.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        ActionTargets.living(ctx).ifPresent(living -> {
            float amount = ActionTargets.magnitude(base, perPower, ctx);
            if (living.isInvertedHealAndHarm()) {
                ServerLevel level = ctx.cast().level();
                living.hurt(
                        level.damageSources()
                                .indirectMagic(ctx.cast().caster(), ctx.cast().caster()),
                        amount * undeadMultiplier);
            } else {
                living.heal(amount);
            }
        });
    }
}
