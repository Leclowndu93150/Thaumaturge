package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TaintBroodTrait implements MobTrait {
    private static final double SCALE = 0.4;
    private static final double MAX_HEALTH = 5.0;
    private static final double ATTACK_DAMAGE = 2.0;
    private static final double FOLLOW_RANGE = 12.0;

    @Override
    public boolean isTaint() {
        return true;
    }

    @Override
    public void modifiers(LivingEntity mob, MobTraitModifiers modifiers) {
        modifiers.setBase(Attributes.SCALE, SCALE);
        modifiers.setBase(Attributes.MAX_HEALTH, MAX_HEALTH);
        modifiers.setBase(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
        modifiers.setBase(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    public void onAdded(LivingEntity mob) {
        mob.setHealth(mob.getMaxHealth());
    }
}
