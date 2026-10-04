package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public final class EntityTaskGoal extends TaskGoal {
    private static final double BASE_REACH_SQR = 3.5;
    private static final float GAZE_TURN = 10.0F;

    public EntityTaskGoal(EntityThaumaturgeGolem golem) {
        super(golem);
    }

    @Override
    protected boolean claim(ServerLevel level) {
        for (Task task : TaskBoard.of(level).openEntityTasks(golem.getUUID(), golem)) {
            Entity target = task.entity();
            if (target != null && mayTake(task, target.blockPosition()) && pathEndsNear(target)) {
                float halfWidth = target.getBbWidth() / 2.0F;
                reachSqr = BASE_REACH_SQR + halfWidth * halfWidth;
                take(task);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void approach(Task task) {
        Entity target = task.entity();
        if (target != null) {
            golem.getNavigation().moveTo(target, golem.getGolemMoveSpeed());
        }
    }

    @Override
    protected double distanceSqrTo(Task task) {
        Entity target = task.entity();
        return target == null ? Double.MAX_VALUE : golem.distanceToSqr(target);
    }

    @Override
    public void tick() {
        super.tick();
        Task task = golem.getTask();
        Entity target = task == null ? null : task.entity();
        if (target != null) {
            golem.getLookControl().setLookAt(target, GAZE_TURN, golem.getMaxHeadXRot());
        }
    }

    private boolean pathEndsNear(Entity target) {
        if (golem.distanceToSqr(target) < reachSqr) {
            return true;
        }
        Path path = golem.getNavigation().createPath(target, 0);
        Node end = path == null ? null : path.getEndNode();
        if (end == null) {
            return false;
        }
        int dx = end.x - Mth.floor(target.getX());
        int dz = end.z - Mth.floor(target.getZ());
        return dx * dx + dz * dz < reachSqr;
    }
}
