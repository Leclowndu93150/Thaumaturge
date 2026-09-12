package com.leclowndu93150.thaumaturge.content.taint.effect;

import java.util.Set;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.EffectCure;

public final class VisExhaustEffect extends MobEffect {
    public VisExhaustEffect() {
        super(MobEffectCategory.HARMFUL, 0x80407F);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        // Thaumcraft's Flux Goo/Gas explicitly cleared curative items from Vis Exhaust. Keep that
        // property on the effect itself so every source behaves consistently in modern NeoForge.
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return false;
    }

    @Override
    public boolean applyEffectTick(LivingEntity mob, int amplification) {
        return true;
    }
}
