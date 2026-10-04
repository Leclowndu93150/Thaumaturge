package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ReturnHomeGoal extends Goal {
    private static final int FIRST_CHECK = 10;
    private static final int CHECK_INTERVAL = 50;
    private static final double AT_HOME_SQR = 5.0;
    private static final double FAR_AWAY_SQR = 1024.0;
    private static final double ARRIVED_SQR = 3.0;
    private static final int STEP_RANGE = 16;
    private static final int STEP_HEIGHT = 7;

    private final EntityThaumaturgeGolem golem;
    private int wait = FIRST_CHECK;
    private @Nullable Vec3 heading;

    public ReturnHomeGoal(EntityThaumaturgeGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (wait > 0) {
            wait--;
            return false;
        }
        wait = CHECK_INTERVAL;
        double awaySqr = golem.distanceToSqr(Vec3.atCenterOf(golem.getHomePosition()));
        if (awaySqr < AT_HOME_SQR) {
            return false;
        }
        heading = awaySqr > FAR_AWAY_SQR
                ? DefaultRandomPos.getPosTowards(golem, STEP_RANGE, STEP_HEIGHT, Vec3.atLowerCornerOf(golem.getHomePosition()), Math.PI / 2.0)
                : Vec3.atLowerCornerOf(golem.getHomePosition());
        return heading != null;
    }

    @Override
    public void start() {
        if (heading != null) {
            golem.getNavigation().moveTo(heading.x, heading.y, heading.z, golem.getGolemMoveSpeed());
        }
    }

    @Override
    public boolean canContinueToUse() {
        return golem.getTask() == null && !golem.getNavigation().isDone() && golem.distanceToSqr(Vec3.atCenterOf(golem.getHomePosition())) > ARRIVED_SQR;
    }

    @Override
    public void stop() {
        wait = CHECK_INTERVAL;
        golem.getNavigation().stop();
    }
}
