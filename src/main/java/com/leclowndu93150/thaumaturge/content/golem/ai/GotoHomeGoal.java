package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public final class GotoHomeGoal extends Goal {
    private static final int IDLE_INTERVAL = 50;
    private static final double NEAR_HOME_DIST_SQR = 5.0;
    private static final double FAR_HOME_DIST_SQR = 1024.0;
    private static final double CONTINUE_DIST_SQR = 3.0;

    private final EntityThaumaturgeGolem golem;
    private double moveX;
    private double moveY;
    private double moveZ;
    private int idleCounter = 10;

    public GotoHomeGoal(EntityThaumaturgeGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (idleCounter > 0) {
            idleCounter--;
            return false;
        }
        idleCounter = IDLE_INTERVAL;
        double distSqr = golem.distanceToSqr(Vec3.atCenterOf(golem.getHomePosition()));
        if (distSqr < NEAR_HOME_DIST_SQR) {
            return false;
        }
        if (distSqr > FAR_HOME_DIST_SQR) {
            Vec3 target = DefaultRandomPos.getPosTowards(golem, 16, 7, Vec3.atLowerCornerOf(golem.getHomePosition()), Math.PI / 2.0);
            if (target == null) {
                return false;
            }
            moveX = target.x;
            moveY = target.y;
            moveZ = target.z;
            return true;
        }
        moveX = golem.getHomePosition().getX();
        moveY = golem.getHomePosition().getY();
        moveZ = golem.getHomePosition().getZ();
        return true;
    }

    @Override
    public void start() {
        golem.getNavigation().moveTo(moveX, moveY, moveZ, golem.getGolemMoveSpeed());
    }

    @Override
    public boolean canContinueToUse() {
        return golem.getTask() == null && !golem.getNavigation().isDone() && golem.distanceToSqr(Vec3.atCenterOf(golem.getHomePosition())) > CONTINUE_DIST_SQR;
    }

    @Override
    public void stop() {
        idleCounter = IDLE_INTERVAL;
        golem.getNavigation().stop();
    }
}
