package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.registry.TTSpellActions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public record DispelAction(float base, float perPower) implements SpellAction {
    public static final MapCodec<DispelAction> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("base", 1.0F).forGetter(DispelAction::base), Codec.FLOAT.optionalFieldOf("per_power", 0.5F).forGetter(DispelAction::perPower)).apply(i,
                    DispelAction::new));

    @Override
    public SpellActionType<?> type() {
        return TTSpellActions.DISPEL.get();
    }

    @Override
    public void apply(ActionContext ctx) {
        ActionTargets.living(ctx).ifPresent(living -> {
            MobEffectCategory purge = ctx.cast().isAlly(living) ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL;
            int count = Math.max(1, Math.round(ActionTargets.magnitude(base, perPower, ctx)));
            removeUpTo(living, purge, count);
        });
    }

    private static void removeUpTo(LivingEntity living, MobEffectCategory category, int count) {
        List<Holder<MobEffect>> matching = new ArrayList<>();
        for (MobEffectInstance instance : living.getActiveEffects()) {
            if (instance.getEffect().value().getCategory() == category) {
                matching.add(instance.getEffect());
            }
        }
        for (int index = 0; index < Math.min(count, matching.size()); index++) {
            living.removeEffect(matching.get(index));
        }
    }
}
