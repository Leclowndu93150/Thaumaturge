package com.leclowndu93150.thaumaturge.content.entity.ai;

import java.util.EnumSet;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public final class HoldStillGoal<T extends Mob & HoldsStill> extends Goal {
    private final T mob;

    public HoldStillGoal(T mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return mob.isAlive() && mob.onGround() && !mob.isInWater() && mob.holdingStill();
    }

    @Override
    public void start() {
        mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        mob.releaseHold();
    }
}
