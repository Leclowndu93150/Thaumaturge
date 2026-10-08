package com.leclowndu93150.thaumaturge.content.golem.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemArmAbility;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemDart;
import com.leclowndu93150.thaumaturge.content.golem.ai.VolleyGoal;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public final class GolemArmDart implements IGolemArmAbility {
    private static final float DAMAGE_DIVISOR = 3.0F;
    private static final float DART_VELOCITY = 1.6F;
    private static final float DART_INACCURACY = 3.0F;
    private static final double ATTACK_MOVE_SPEED = 1.0;
    private static final int ATTACK_INTERVAL_MIN = 20;
    private static final int ATTACK_INTERVAL_MAX = 25;
    private static final float ATTACK_RADIUS = 16.0F;

    @Override
    public void onRangedAttack(IGolemAPI golem, LivingEntity target, float power) {
        LivingEntity shooter = golem.asEntity();
        double damage = (float) shooter.getAttributeValue(Attributes.ATTACK_DAMAGE) / DAMAGE_DIVISOR
                + power
                + golem.level().getRandom().nextGaussian() * 0.25;
        EntityGolemDart.loose(shooter, target, damage, power * power, DART_VELOCITY, DART_INACCURACY);
        shooter.playSound(
                SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (golem.level().getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Override
    public Goal createRangedGoal(RangedAttackMob mob) {
        return new VolleyGoal(mob, ATTACK_MOVE_SPEED, ATTACK_INTERVAL_MIN, ATTACK_INTERVAL_MAX, ATTACK_RADIUS);
    }
}
