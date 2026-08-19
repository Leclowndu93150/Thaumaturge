package com.leclowndu93150.thaumaturge.content.golem.ai;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public final class GolemArrowAttackGoal extends Goal {
    private static final int SEE_TIME_THRESHOLD = 20;

    private final Mob mob;
    private final RangedAttackMob rangedMob;
    private final double moveSpeed;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;

    public GolemArrowAttackGoal(RangedAttackMob rangedMob, double moveSpeed, int attackIntervalMin, int attackIntervalMax, float attackRadius) {
        this.rangedMob = rangedMob;
        this.mob = (Mob) rangedMob;
        this.moveSpeed = moveSpeed;
        this.attackIntervalMin = attackIntervalMin;
        this.attackIntervalMax = attackIntervalMax;
        this.attackRadius = attackRadius;
        this.attackRadiusSqr = attackRadius * attackRadius;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || !mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        seeTime = 0;
        attackTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        double distSqr = mob.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
        boolean canSee = mob.getSensing().hasLineOfSight(target);
        if (canSee) {
            seeTime++;
        } else {
            seeTime = 0;
        }
        if (distSqr <= attackRadiusSqr && seeTime >= SEE_TIME_THRESHOLD) {
            mob.getNavigation().stop();
        } else {
            mob.getNavigation().moveTo(target, moveSpeed);
        }
        mob.getLookControl().setLookAt(target, 10.0F, 30.0F);
        if (--attackTime == 0) {
            if (distSqr > attackRadiusSqr || !canSee) {
                return;
            }
            float ratio = (float) Math.sqrt(distSqr) / attackRadius;
            float power = Mth.clamp(ratio, 0.1F, 1.0F);
            rangedMob.performRangedAttack(target, power);
            attackTime = Mth.floor(ratio * (attackIntervalMax - attackIntervalMin) + attackIntervalMin);
        } else if (attackTime < 0) {
            float ratio = (float) Math.sqrt(distSqr) / attackRadius;
            attackTime = Mth.floor(ratio * (attackIntervalMax - attackIntervalMin) + attackIntervalMin);
        }
    }
}
