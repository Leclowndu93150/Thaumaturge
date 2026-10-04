package com.leclowndu93150.thaumaturge.content.golem.ai;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public final class VolleyGoal extends Goal {
    private static final int STEADY_AIM_TICKS = 20;
    private static final float GAZE_TURN = 10.0F;
    private static final float GAZE_PITCH = 30.0F;
    private static final float WEAKEST_SHOT = 0.1F;

    private final Mob shooter;
    private final RangedAttackMob archer;
    private final double pace;
    private final int quickestReload;
    private final int slowestReload;
    private final float range;
    private int reload = -1;
    private int steadyTicks;

    public VolleyGoal(RangedAttackMob archer, double pace, int quickestReload, int slowestReload, float range) {
        this.archer = archer;
        this.shooter = (Mob) archer;
        this.pace = pace;
        this.quickestReload = quickestReload;
        this.slowestReload = slowestReload;
        this.range = range;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return shooter.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || !shooter.getNavigation().isDone();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        steadyTicks = 0;
        reload = -1;
    }

    @Override
    public void tick() {
        LivingEntity target = shooter.getTarget();
        if (target == null) {
            return;
        }
        double distanceSqr = shooter.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
        boolean inSight = shooter.getSensing().hasLineOfSight(target);
        steadyTicks = inSight ? steadyTicks + 1 : 0;
        boolean inRange = distanceSqr <= range * range;
        if (inRange && steadyTicks >= STEADY_AIM_TICKS) {
            shooter.getNavigation().stop();
        } else {
            shooter.getNavigation().moveTo(target, pace);
        }
        shooter.getLookControl().setLookAt(target, GAZE_TURN, GAZE_PITCH);
        float reach = (float) Math.sqrt(distanceSqr) / range;
        if (--reload == 0) {
            if (inRange && inSight) {
                archer.performRangedAttack(target, Mth.clamp(reach, WEAKEST_SHOT, 1.0F));
                reload = reloadFor(reach);
            }
        } else if (reload < 0) {
            reload = reloadFor(reach);
        }
    }

    private int reloadFor(float reach) {
        return Mth.floor(reach * (slowestReload - quickestReload) + quickestReload);
    }
}
