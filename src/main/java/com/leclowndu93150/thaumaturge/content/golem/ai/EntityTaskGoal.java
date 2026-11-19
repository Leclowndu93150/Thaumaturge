package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class EntityTaskGoal extends TaskGoal {
    private static final double BASE_REACH_SQR = 3.5;
    private static final float GAZE_TURN = 10.0F;

    public EntityTaskGoal(EntityThaumaturgeGolem golem) {
        super(golem);
    }

    @Override
    protected boolean claim(ServerLevel level) {
        var tasks = TaskBoard.of(level).openEntityTasks(golem.getUUID(), golem);
        Thaumaturge.LOGGER.debug(
                "[golem-task] tick={} golem={} openEntityTasks={}", level.getGameTime(), golem.getUUID(), tasks.size());
        for (Task task : tasks) {
            Entity target = task.entity();
            if (target == null) continue;
            boolean allowed = mayTake(task, target.blockPosition());
            boolean retry = shouldTestPath(target.blockPosition());
            if (!allowed || !retry) {
                Thaumaturge.LOGGER.debug(
                        "[golem-task] tick={} golem={} task={} target={} allowed={} retry={}",
                        level.getGameTime(),
                        golem.getUUID(),
                        task.id(),
                        target.getUUID(),
                        allowed,
                        retry);
                continue;
            }
            PathResult result = pathEndsNear(target);
            Thaumaturge.LOGGER.debug(
                    "[golem-task] tick={} golem={} pos={} task={} target={} targetPos={} result={} deferred={}",
                    level.getGameTime(),
                    golem.getUUID(),
                    golem.position(),
                    task.id(),
                    target.getUUID(),
                    target.position(),
                    result,
                    GolemPathSearchBudget.wasDeferred(level, golem.getUUID()));
            if (result == PathResult.REACHABLE) {
                float halfWidth = target.getBbWidth() / 2.0F;
                reachSqr = BASE_REACH_SQR + halfWidth * halfWidth;
                take(task);
                return true;
            }
            // A partial route has an endpoint, but a moving target/deck may make
            // it reachable on the very next evaluation. Only cache absent routes.
            if (result == PathResult.NO_ROUTE && !GolemPathSearchBudget.wasDeferred(level, golem.getUUID())) {
                rememberFailedPath(target.blockPosition());
            }
            break;
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

    private PathResult pathEndsNear(Entity target) {
        if (golem.distanceToSqr(target) < reachSqr) {
            return PathResult.REACHABLE;
        }
        Path path = golem.getNavigation().createPath(target, 1);
        Node end = path == null ? null : path.getEndNode();
        if (end == null) {
            return PathResult.NO_ROUTE;
        }
        var level = golem.level();
        Vec3 targetInPath =
                LevelSpaces.frameOf(level, end.asBlockPos()).toLocal(LevelSpaces.toWorld(level, target.position()));
        double dx = (double) end.x - Mth.floor(targetInPath.x);
        double dz = (double) end.z - Mth.floor(targetInPath.z);
        return dx * dx + dz * dz < reachSqr ? PathResult.REACHABLE : PathResult.PARTIAL;
    }

    private enum PathResult {
        REACHABLE,
        PARTIAL,
        NO_ROUTE
    }
}
