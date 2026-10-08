package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record MobEffectAction(
        Holder<MobEffect> effect,
        float ticks,
        float perDuration,
        float amplifier,
        float perPower,
        int maxAmplifier,
        float chance)
        implements SpellAction {
    public static final MapCodec<MobEffectAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    MobEffect.CODEC.fieldOf("effect").forGetter(MobEffectAction::effect),
                    Codec.FLOAT.optionalFieldOf("ticks", 0.0F).forGetter(MobEffectAction::ticks),
                    Codec.FLOAT.optionalFieldOf("per_duration", 20.0F).forGetter(MobEffectAction::perDuration),
                    Codec.FLOAT.optionalFieldOf("amplifier", 0.0F).forGetter(MobEffectAction::amplifier),
                    Codec.FLOAT.optionalFieldOf("per_power", 0.0F).forGetter(MobEffectAction::perPower),
                    Codec.INT.optionalFieldOf("max_amplifier", 4).forGetter(MobEffectAction::maxAmplifier),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(MobEffectAction::chance))
            .apply(i, MobEffectAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.MOB_EFFECT.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        if (chance < 1.0F && ctx.cast().random().nextFloat() >= chance) {
            return;
        }
        ActionTargets.living(ctx).ifPresent(living -> {
            int duration = Math.round(ActionTargets.lasting(ticks, perDuration, ctx));
            int level = Mth.clamp(Mth.floor(ActionTargets.magnitude(amplifier, perPower, ctx)), 0, maxAmplifier);
            if (duration > 0) {
                living.addEffect(
                        new MobEffectInstance(effect, duration, level),
                        ctx.cast().caster());
            }
        });
    }
}
