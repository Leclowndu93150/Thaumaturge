package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockTaskGoal extends TaskGoal {
    private static final double PATH_SLACK_SQR = 2.25;
    private static final int HEAD_HEIGHT = 2;
    private static final float GAZE_TURN = 10.0F;

    private @Nullable BlockPos standingSpot;

    public BlockTaskGoal(EntityThaumaturgeGolem golem) {
        super(golem);
    }

    @Override
    protected boolean claim(ServerLevel level) {
        standingSpot = null;
        var tasks = TaskBoard.of(level).openBlockTasks(golem.getUUID(), golem);
        // Thaumaturge.LOGGER.debug(
        // "[golem-task] tick={} golem={} openBlockTasks={}", level.getGameTime(), golem.getUUID(), tasks.size());
        for (Task task : tasks) {
            BlockPos target = task.pos();
            if (!mayTake(task, target) || !shouldTestPath(target)) continue;
            // TaskBoard already ranks candidates. Evaluate at most one expensive path per claim.
            PathResult result = pathEndsNear(target);
            if (result == PathResult.REACHABLE) {
                standingSpot = nearestOpenSide(target);
                take(task);
                return true;
            }
            // A partial path may become usable as the deck moves. Neither partial
            // routes nor budget-deferred searches should be cached as failures.
            if (result == PathResult.NO_ROUTE && !GolemPathSearchBudget.wasDeferred(level, golem.getUUID())) {
                rememberFailedPath(target);
            }
            break;
        }
        return false;
    }

    @Override
    protected void approach(Task task) {
        Vec3 goal = Vec3.atCenterOf(destination(task));
        golem.getNavigation().moveTo(goal.x, goal.y, goal.z, golem.getGolemMoveSpeed());
    }

    @Override
    protected double distanceSqrTo(Task task) {
        return golem.distanceToSqr(LevelSpaces.worldCenter(golem.level(), destination(task)));
    }

    private BlockPos destination(Task task) {
        return standingSpot != null ? standingSpot : task.pos();
    }

    @Override
    public void tick() {
        super.tick();
        Task task = golem.getTask();
        if (task != null) {
            Vec3 centre = LevelSpaces.worldCenter(golem.level(), task.pos());
            golem.getLookControl().setLookAt(centre.x, centre.y, centre.z, GAZE_TURN, golem.getMaxHeadXRot());
        }
    }

    private @Nullable BlockPos nearestOpenSide(BlockPos pos) {
        return Direction.Plane.HORIZONTAL.stream()
                .map(pos::relative)
                .filter(side -> golem.level()
                        .getBlockState(side)
                        .getCollisionShape(golem.level(), side)
                        .isEmpty())
                .min(Comparator.comparingDouble(
                        side -> LevelSpaces.distanceSqr(golem.level(), Vec3.atCenterOf(side), golem.position())))
                .orElse(null);
    }

    private PathResult pathEndsNear(BlockPos pos) {
        Vec3 worldTarget = LevelSpaces.worldCenter(golem.level(), pos);
        if (golem.distanceToSqr(worldTarget) < reachSqr) {
            return PathResult.REACHABLE;
        }
        Path path = golem.getNavigation().createPath(pos, 1);
        Node end = path == null ? null : path.getEndNode();
        if (end == null) {
            return PathResult.NO_ROUTE;
        }
        BlockPos targetInPath = BlockPos.containing(
                LevelSpaces.frameOf(golem.level(), end.asBlockPos()).toLocal(worldTarget));
        int dx = end.x - targetInPath.getX();
        int dy = end.y - targetInPath.getY();
        int dz = end.z - targetInPath.getZ();
        if (dx == 0 && dz == 0 && dy == HEAD_HEIGHT) {
            dy--;
        }
        return dx * dx + dy * dy + dz * dz < PATH_SLACK_SQR ? PathResult.REACHABLE : PathResult.PARTIAL;
    }

    private enum PathResult {
        REACHABLE,
        PARTIAL,
        NO_ROUTE
    }
}
