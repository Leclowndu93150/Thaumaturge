package com.leclowndu93150.thaumaturge.content.entity.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class StrikeEffectTrait implements MobTrait {
    private final Holder<MobEffect> effect;
    private final int ticks;
    private final int amplifier;

    public StrikeEffectTrait(Holder<MobEffect> effect, int ticks, int amplifier) {
        this.effect = effect;
        this.ticks = ticks;
        this.amplifier = amplifier;
    }

    @Override
    public float onAttack(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        target.addEffect(new MobEffectInstance(effect, ticks, amplifier), mob);
        return amount;
    }
}
