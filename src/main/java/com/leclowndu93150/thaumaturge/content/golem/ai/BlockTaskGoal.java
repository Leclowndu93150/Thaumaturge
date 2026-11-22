package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemTrace;
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
    private static final float GAZE_TURN = 10.0F;

    private @Nullable BlockPos standingSpot;
    private long lastProbeTraceTick = -1000;

    private boolean carryingItems() {
        return golem.hands().contents().stream().anyMatch(stack -> !stack.isEmpty());
    }

    private boolean shouldTraceProbe() {
        if (!GolemTrace.DEBUG_TRACE) return false;
        long now = golem.level().getGameTime();
        if (now - lastProbeTraceTick < 40) return false;
        lastProbeTraceTick = now;
        return true;
    }

    public BlockTaskGoal(EntityThaumaturgeGolem golem) {
        super(golem);
    }

    @Override
    protected boolean claim(ServerLevel level) {
        standingSpot = null;
        var tasks = TaskBoard.of(level).openBlockTasks(golem.getUUID(), golem);
        if (tasks.isEmpty() && carryingItems() && shouldTraceProbe()) {
            GolemTrace.log(
                    "[golem-block] tick={} golem={} reason=no_block_tasks hands={}",
                    level.getGameTime(),
                    golem.getUUID(),
                    golem.hands().contents());
        }
        if ((GolemTrace.DEBUG_TRACE && GolemTrace.VERBOSE_TASK_TRACE)) {
            GolemTrace.log(
                    "[golem-task] tick={} golem={} openBlockTasks={}",
                    level.getGameTime(),
                    golem.getUUID(),
                    tasks.size());
        }
        for (Task task : tasks) {
            BlockPos target = task.pos();
            boolean allowed = mayTake(task, target);
            boolean retry = shouldTestPath(target);
            if (!allowed || !retry) {
                if (carryingItems() && shouldTraceProbe()) {
                    GolemTrace.log(
                            "[golem-block] tick={} golem={} reason=task_ineligible " + "target={} allowed={} retry={}",
                            level.getGameTime(),
                            golem.getUUID(),
                            target,
                            allowed,
                            retry);
                }
                continue;
            }
            // TaskBoard already ranks candidates. Evaluate at most one expensive path per claim.
            PathResult result = pathEndsNear(target);
            if (result == PathResult.REACHABLE || result == PathResult.REACHABLE_COOPERATIVE) {
                // A cooperative route already ends on a supported, reachable
                // foothold beside the block. Requesting a different open-side
                // block after claiming it discards this finished search and
                // repeats the whole cross-frame A* (up to ~100 extra ticks in
                // the slanted-deck tests). Keep its original block target so
                // the navigator can hand the completed route directly to us.
                standingSpot = result == PathResult.REACHABLE_COOPERATIVE ? null : nearestOpenSide(target);
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
        BlockPos block = destination(task);
        var frame = LevelSpaces.frameOf(golem.level(), block);
        if (!frame.isWorld() && golem.getNavigation() instanceof GolemSpaceNavigation spaces) {
            // A block task is stored as a plot-local block key. Converting it
            // to world coordinates and passing those to PathNavigation.moveTo
            // loses the Sable frame. On a rotating deck this creates a new
            // target block every few ticks, discarding each successful A* result.
            // Keep both the frame and the exact local destination stable.
            spaces.moveToFrame(frame, Vec3.atCenterOf(block), golem.getGolemMoveSpeed());
        } else {
            Vec3 goal = LevelSpaces.worldCenter(golem.level(), block);
            golem.getNavigation().moveTo(goal.x, goal.y, goal.z, golem.getGolemMoveSpeed());
        }
    }

    @Override
    protected double distanceSqrTo(Task task) {
        var frame = LevelSpaces.frameOf(golem.level(), destination(task));
        if (!golem.onGround()
                && !golem.properties().hasTrait(com.leclowndu93150.thaumaturge.registry.TTGolemTraits.FLYER.get())
                && !frame.isWorld()
                && frame.directionToWorld(new Vec3(0, 1, 0)).y < 0.99) {
            return Double.MAX_VALUE;
        }
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
                        side -> LevelSpaces.worldCenter(golem.level(), side).distanceToSqr(golem.position())))
                .orElse(null);
    }

    /**
     * Compare actual world-space feet to the rotating target, not integer
     * floors of two independently moving positions. A 1 cm pose change can
     * change a floored X/Z cell and incorrectly reject a valid delivery path.
     */
    static boolean pathEndpointReaches(Vec3 endpoint, Vec3 target) {
        if (endpoint.distanceToSqr(target) < PATH_SLACK_SQR) return true;
        double dx = endpoint.x - target.x;
        double dz = endpoint.z - target.z;
        double dy = Math.abs(endpoint.y - target.y);
        // Preserve the historical tolerance for a path endpoint above a
        // two-block-high target, but only when centered horizontally.
        return Math.abs(dx) < 0.7
                && Math.abs(dz) < 0.7
                && dy >= 1.75
                && dy <= 2.25
                && dx * dx + dz * dz + (dy - 1) * (dy - 1) < PATH_SLACK_SQR;
    }

    private PathResult pathEndsNear(BlockPos pos) {
        Vec3 worldTarget = LevelSpaces.worldCenter(golem.level(), pos);
        if (golem.distanceToSqr(worldTarget) < reachSqr) return PathResult.REACHABLE;
        Path path = golem.getNavigation().createPath(pos, 1);
        Node end = path == null ? null : path.getEndNode();
        if (end == null) {
            if (shouldTraceProbe()) {
                GolemSpaceNavigation space = golem.getNavigation() instanceof GolemSpaceNavigation nav ? nav : null;
                GolemTrace.log(
                        "[golem-block] tick={} golem={} target={} reason=no_path "
                                + "frame={} position={} deferred={} pending={} ready={}",
                        golem.level().getGameTime(),
                        golem.getUUID(),
                        pos,
                        LevelSpaces.frameOf(golem.level(), pos),
                        golem.position(),
                        golem.level() instanceof ServerLevel server
                                && GolemPathSearchBudget.wasDeferred(server, golem.getUUID()),
                        space != null && space.hasPendingSearch(),
                        space != null && space.hasReadySearch());
            }
            return PathResult.NO_ROUTE;
        }
        Vec3 pathEnd = GolemSpaceNavigation.pathEndWorld(golem.level(), path);
        boolean reachable = pathEndpointReaches(pathEnd, worldTarget);
        if (!reachable && shouldTraceProbe()) {
            GolemTrace.log(
                    "[golem-block] tick={} golem={} target={} reason=endpoint_too_far "
                            + "endpoint={} worldTarget={} distance={} frame={}",
                    golem.level().getGameTime(),
                    golem.getUUID(),
                    pos,
                    pathEnd,
                    worldTarget,
                    Math.sqrt(pathEnd.distanceToSqr(worldTarget)),
                    LevelSpaces.frameOf(golem.level(), pos));
        }
        return !reachable
                ? PathResult.PARTIAL
                : GolemSpaceNavigation.isCooperativePath(path)
                        ? PathResult.REACHABLE_COOPERATIVE
                        : PathResult.REACHABLE;
    }

    private enum PathResult {
        REACHABLE,
        REACHABLE_COOPERATIVE,
        PARTIAL,
        NO_ROUTE
    }
}
