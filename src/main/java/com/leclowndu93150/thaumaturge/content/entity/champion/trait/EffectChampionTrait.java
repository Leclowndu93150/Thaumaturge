package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class EffectChampionTrait extends AbstractChampionTrait {
    private static final float PROC_CHANCE = 0.4F;

    private final Holder<MobEffect> effect;
    private final int ticks;

    public EffectChampionTrait(Holder<MobEffect> effect, int ticks) {
        this.effect = effect;
        this.ticks = ticks;
    }

    @Override
    public float onAttack(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        if (mob.getRandom().nextFloat() < PROC_CHANCE) {
            target.addEffect(new MobEffectInstance(effect, ticks));
        }
        return amount;
    }
}
