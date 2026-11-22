package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemTrace;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class EntityTaskGoal extends TaskGoal {
    private static final double BASE_REACH_SQR = 3.5;
    private static final float GAZE_TURN = 10.0F;
    private static final double FOREIGN_FRAME_PENALTY_SQR = 64.0;
    private static final int MAX_FAILED_ITEMS = 64;
    private static final int MAX_PATH_CHECKS_PER_CLAIM = 3;
    private final Map<UUID, Long> failedItemsUntil = new HashMap<>();

    public EntityTaskGoal(EntityThaumaturgeGolem golem) {
        super(golem);
    }

    @Override
    protected boolean claim(ServerLevel level) {
        var tasks = new ArrayList<>(TaskBoard.of(level).openEntityTasks(golem.getUUID(), golem));
        SpaceFrame ownFrame = LevelSpaces.trackedFrame(golem);
        // Preserve the board's priority/distance ordering, but slightly favor
        // work on this deck. A blocked item on another deck must not monopolize
        // every golem while nearby items wait.
        tasks.sort(Comparator.comparingDouble(task -> entityTaskScore(task, ownFrame)));
        if ((GolemTrace.DEBUG_TRACE && GolemTrace.VERBOSE_TASK_TRACE)) {
            GolemTrace.log(
                    "[golem-task] tick={} golem={} openEntityTasks={}",
                    level.getGameTime(),
                    golem.getUUID(),
                    tasks.size());
        }
        int pathChecks = 0;
        for (Task task : tasks) {
            Entity target = task.entity();
            if (target == null) continue;
            boolean allowed = mayTakeEntity(task, target);
            boolean retry = level.getGameTime() >= failedItemsUntil.getOrDefault(target.getUUID(), 0L);
            if (!allowed || !retry) {
                if ((GolemTrace.DEBUG_TRACE && GolemTrace.VERBOSE_TASK_TRACE)) {
                    GolemTrace.log(
                            "[golem-task] tick={} golem={} task={} target={} allowed={} retry={}",
                            level.getGameTime(),
                            golem.getUUID(),
                            task.id(),
                            target.getUUID(),
                            allowed,
                            retry);
                }
                continue;
            }
            // Bounded work: many impossible item paths must not create another
            // synchronous pathfinding storm during one AI goal evaluation.
            if (pathChecks++ >= MAX_PATH_CHECKS_PER_CLAIM) break;
            PathResult result = pathEndsNear(target);
            if ((GolemTrace.DEBUG_TRACE && GolemTrace.VERBOSE_TASK_TRACE)) {
                GolemTrace.log(
                        "[golem-task] tick={} golem={} pos={} task={} target={} targetPos={} result={} deferred={}",
                        level.getGameTime(),
                        golem.getUUID(),
                        golem.position(),
                        task.id(),
                        target.getUUID(),
                        target.position(),
                        result,
                        GolemPathSearchBudget.wasDeferred(level, golem.getUUID()));
            }
            if (result == PathResult.REACHABLE) {
                float halfWidth = target.getBbWidth() / 2.0F;
                reachSqr = BASE_REACH_SQR + halfWidth * halfWidth;
                if (GolemTrace.DEBUG_TRACE) {
                    GolemTrace.log(
                            "[golem-item] event=claimed tick={} golem={} task={} item={} "
                                    + "itemPos={} golemPos={} itemFrame={} golemFrame={}",
                            level.getGameTime(),
                            golem.getUUID(),
                            task.id(),
                            target.getUUID(),
                            target.position(),
                            golem.position(),
                            LevelSpaces.trackedFrame(target),
                            LevelSpaces.trackedFrame(golem));
                }
                take(task);
                return true;
            }
            // Native navigation may return a partial one-node path directly
            // below a target. Short-cooldown such paths; never claim them as reachable.
            // Pending cooperative searches remain pending, not failures.
            if (!GolemPathSearchBudget.wasDeferred(level, golem.getUUID())) {
                if (GolemTrace.DEBUG_TRACE) {
                    GolemTrace.log(
                            "[golem-item] event=path_rejected tick={} golem={} task={} item={} "
                                    + "result={} itemPos={} golemPos={} itemFrame={} golemFrame={}",
                            level.getGameTime(),
                            golem.getUUID(),
                            task.id(),
                            target.getUUID(),
                            result,
                            target.position(),
                            golem.position(),
                            LevelSpaces.trackedFrame(target),
                            LevelSpaces.trackedFrame(golem));
                }
                rememberFailedItem(target.getUUID(), result == PathResult.PARTIAL ? 30 : 80);
                // A proven failure belongs to this item, not to every other
                // item in the same block. Try another candidate next.
                continue;
            }
            // The cooperative search is still pending. Let it finish rather
            // than replacing it with a different destination every five ticks.
            break;
        }
        return false;
    }

    private double entityTaskScore(Task task, SpaceFrame ownFrame) {
        Entity target = task.entity();
        if (target == null) return Double.POSITIVE_INFINITY;
        return golem.distanceToSqr(target)
                - 256.0 * task.priority()
                + (ownFrame != null && ownFrame.equals(LevelSpaces.trackedFrame(target))
                        ? 0.0
                        : FOREIGN_FRAME_PENALTY_SQR);
    }

    private void rememberFailedItem(UUID item, int cooldownTicks) {
        if (failedItemsUntil.size() >= MAX_FAILED_ITEMS) {
            long now = golem.level().getGameTime();
            failedItemsUntil.entrySet().removeIf(entry -> entry.getValue() <= now);
            if (failedItemsUntil.size() >= MAX_FAILED_ITEMS) failedItemsUntil.clear();
        }
        failedItemsUntil.put(item, golem.level().getGameTime() + cooldownTicks);
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
        double halfWidth = target.getBbWidth() / 2.0;
        double pickupReachSqr = BASE_REACH_SQR + halfWidth * halfWidth;
        if (golem.distanceToSqr(target) < pickupReachSqr) {
            return PathResult.REACHABLE;
        }
        Path path = golem.getNavigation().createPath(target, 1);
        Node end = path == null ? null : path.getEndNode();
        if (end == null) {
            return PathResult.NO_ROUTE;
        }
        // Entity.position() is already a world-space coordinate. The old code
        // transformed it through LevelSpaces.toWorld() a second time, making
        // reachable items on sublevels appear unreachable.
        Vec3 endpoint = GolemSpaceNavigation.pathEndWorld(golem.level(), path);
        Vec3 itemWorld = target.position();
        // Collection requires actual 3D proximity. Checking only X/Z
        // marked a path ending several blocks *below* the item as reachable.
        return endpoint.distanceToSqr(itemWorld) < pickupReachSqr ? PathResult.REACHABLE : PathResult.PARTIAL;
    }

    private enum PathResult {
        REACHABLE,
        PARTIAL,
        NO_ROUTE
    }
}
