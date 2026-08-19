package com.leclowndu93150.thaumaturge.content.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public final class LongRangeAttackGoal extends RangedAttackGoal {
    private final Mob wielder;
    private final double minDistance;

    public LongRangeAttackGoal(RangedAttackMob mob, double minDistance, double speedModifier, int attackIntervalMin, int attackIntervalMax, float attackRadius) {
        super(mob, speedModifier, attackIntervalMin, attackIntervalMax, attackRadius);
        this.minDistance = minDistance;
        this.wielder = (Mob) mob;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        LivingEntity target = this.wielder.getTarget();
        if (target == null) {
            return false;
        }
        if (!target.isAlive()) {
            this.wielder.setTarget(null);
            return false;
        }
        double distSq = this.wielder.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
        return distSq >= this.minDistance * this.minDistance;
    }
}
