package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Routes between physical decks; the native navigator still owns paths inside one frame. */
public final class GolemSpaceNavigation extends PathNavigation {
    private static final double GRID = 0.5;
    private static final double SURFACE_RAY_MARGIN = 0.01;
    private static final int MAX_VISITED = 2048;
    private static final int VALIDATE_EVERY = 5;
    private final PathNavigation nativeNavigation;
    private final boolean flying;
    private int routeAge;
    private int progressNode = -1;
    private double bestDistance = Double.POSITIVE_INFINITY;
    private int stalled;
    private GolemPathGeometry.@Nullable Surface avoided;
    private @Nullable BlockPos avoidedTarget;
    private long avoidUntil;
    private @Nullable BlockPos failedTarget;
    private Vec3 failedStart = Vec3.ZERO;
    private Vec3 failedGoal = Vec3.ZERO;
    private long retryAfter;

    public GolemSpaceNavigation(Mob mob, Level level, PathNavigation nativeNavigation, boolean flying) {
        super(mob, level);
        this.nativeNavigation = nativeNavigation;
        this.flying = flying;
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        nodeEvaluator = new WalkNodeEvaluator();
        return new PathFinder(nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected Vec3 getTempMobPos() {
        return mob.position();
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    @Override
    protected @Nullable Path createPath(
            Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange) {
        BlockPos target = targets.stream()
                .min(Comparator.comparingDouble(
                        pos -> mob.position().distanceToSqr(LevelSpaces.worldCenter(level, pos))))
                .orElse(null);
        if (target == null) return null;
        var targetFrame = LevelSpaces.frameOf(level, target);
        var sourceFrame = LevelSpaces.trackedFrame(mob);
        Vec3 worldTarget = LevelSpaces.worldCenter(level, target);
        boolean sameFrame = sourceFrame.equals(targetFrame);
        boolean ordinaryWorld = sourceFrame.isWorld()
                && targetFrame.isWorld()
                && LevelSpaces.framesTouching(level, new AABB(mob.position(), worldTarget).inflate(2.0)).stream()
                        .allMatch(frame -> frame.isWorld());
        boolean tilted =
                sameFrame && !sourceFrame.isWorld() && sourceFrame.directionToWorld(new Vec3(0, 1, 0)).y < 0.999;
        if (sameFrame && !tilted && (!sourceFrame.isWorld() || ordinaryWorld)) {
            Path nativePath = nativeNavigation.createPath(target, accuracy);
            if (ordinaryWorld || nativePath != null && nativePath.canReach()) return nativePath;
        }
        if (path instanceof SpacePath current
                && !current.isDone()
                && current.getTarget().equals(target)
                && valid(current, flying || mob.onGround())) {
            return current;
        }
        return findPath(target, worldTarget, accuracy, followRange);
    }

    @Override
    public @Nullable BlockPos getTargetPos() {
        return path == null ? null : path.getTarget();
    }

    @Override
    public boolean moveTo(@Nullable Path route, double speed) {
        if (route == null) {
            stop();
            return false;
        }
        speedModifier = speed;
        if (route != path) {
            path = route;
            routeAge = 0;
            stalled = 0;
            progressNode = -1;
            bestDistance = Double.POSITIVE_INFINITY;
        }
        if (route instanceof SpacePath) {
            nativeNavigation.stop();
            return !route.isDone();
        }
        return nativeNavigation.moveTo(route, speed);
    }

    @Override
    public void tick() {
        if (!(path instanceof SpacePath route)) {
            nativeNavigation.tick();
            path = nativeNavigation.getPath();
            return;
        }
        if (route.isDone()) return;
        routeAge++;
        // Frame transforms can change every tick. Check route continuity immediately,
        // but only repeat expensive collision/surface sweeps periodically.
        // A jump temporarily overlaps the edge being climbed, so check collision after landing.
        if (!valid(route, routeAge % VALIDATE_EVERY == 0 && (flying || mob.onGround()))) {
            BlockPos target = route.getTarget();
            stop();
            moveTo(createPath(target, 1), speedModifier);
            return;
        }
        Vec3 next = route.points.get(route.getNextNodeIndex()).worldFeet();
        if (mob.position().distanceToSqr(next) < 0.12) {
            route.advance();
            if (route.isDone()) return;
            next = route.points.get(route.getNextNodeIndex()).worldFeet();
        }
        double remaining = flying
                ? mob.position().distanceToSqr(next)
                : mob.position().subtract(next).horizontalDistanceSqr();
        if (progressNode != route.getNextNodeIndex() || remaining < bestDistance - 0.01) {
            progressNode = route.getNextNodeIndex();
            bestDistance = remaining;
            stalled = 0;
        } else if (++stalled > 40) {
            avoided = route.points.get(route.getNextNodeIndex());
            avoidedTarget = route.getTarget();
            avoidUntil = level.getGameTime() + 100;
            stop();
            return;
        }
        if (!flying
                && mob.onGround()
                && (next.y - mob.getY() > mob.maxUpStep()
                        || stalled > 10 && next.y - mob.getY() > 0.2
                        || next.y >= mob.getY() - 0.25
                                && mob.position().subtract(next).horizontalDistanceSqr() > 0.75 * 0.75)) {
            mob.getJumpControl().jump();
        }
        var frame = LevelSpaces.trackedFrame(mob);
        double speed = !flying && !frame.isWorld() && frame.directionToWorld(new Vec3(0, 1, 0)).y < 0.98
                ? speedModifier * 0.65
                : speedModifier;
        mob.getMoveControl().setWantedPosition(next.x, next.y, next.z, speed);
    }

    @Override
    public void recomputePath() {
        if (path instanceof SpacePath route) {
            BlockPos target = route.getTarget();
            path = null;
            moveTo(createPath(target, 1), speedModifier);
        } else {
            nativeNavigation.recomputePath();
        }
    }

    @Override
    public void stop() {
        super.stop();
        nativeNavigation.stop();
        mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
    }

    private boolean valid(SpacePath route, boolean checkCollision) {
        Vec3 previous = mob.position();
        for (int i = route.getNextNodeIndex(); i < route.points.size(); i++) {
            Vec3 next = route.points.get(i).worldFeet();
            Vec3 delta = next.subtract(previous);
            if (flying
                    ? delta.lengthSqr() > 3.0
                    : delta.horizontalDistanceSqr() > 1.3
                            || delta.y > route.jumpHeight + 0.01
                            || delta.y < -Math.min(3, mob.getMaxFallDistance()) - 0.01) return false;
            if (checkCollision
                    && i == route.getNextNodeIndex()
                    && !edgeClear(previous, next)
                    && !jumpClear(previous, next, route.jumpHeight)) return false;
            previous = next;
        }
        return previous.distanceToSqr(LevelSpaces.worldCenter(level, route.getTarget())) <= route.reachSqr;
    }

    private @Nullable SpacePath findPath(BlockPos target, Vec3 goal, int accuracy, float range) {
        Vec3 start = mob.position();
        if (level instanceof ServerLevel
                && target.equals(failedTarget)
                && level.getGameTime() < retryAfter
                && start.distanceToSqr(failedStart) < 0.04
                && goal.distanceToSqr(failedGoal) < 0.04) return null;
        // Include re-pathing from the navigator, not just task-claim checks, in the budget.
        if (level instanceof ServerLevel server && !GolemPathSearchBudget.tryAcquire(server, mob.getUUID())) {
            // Thaumaturge.LOGGER.debug(
            // "[golem-task] tick={} golem={} path target={} deferred by budget",
            // level.getGameTime(),
            // mob.getUUID(),
            // target);
            return null;
        }
        if (start.distanceToSqr(goal) > range * range) {
            // Thaumaturge.LOGGER.debug(
            // "[golem-task] tick={} golem={} path target={} outside range={} start={} goal={}",
            // level.getGameTime(),
            // mob.getUUID(),
            // target,
            // range,
            // start,
            // goal);
            return null;
        }
        var first = flying
                ? new GolemPathGeometry.Surface(WorldFrame.INSTANCE, start)
                : GolemPathGeometry.surface(level, mob, start.x, start.z, start.y - 1.0, start.y + 0.1);
        if (first == null && !flying) {
            first = GolemPathGeometry.startingSurface(
                    level, mob, start.x, start.z, start.y - 1.0, start.y + mob.maxUpStep() + 0.1);
            if (Thaumaturge.LOGGER.isDebugEnabled()) {
                AABB body = GolemPathGeometry.body(mob, start);
                // Thaumaturge.LOGGER.debug(
                // "[golem-task] tick={} golem={} start={} body={} grounded={} bodyClear={} frames={} edgeRecovery={}",
                // level.getGameTime(),
                // mob.getUUID(),
                // start,
                // body,
                // mob.onGround(),
                // GolemPathGeometry.clear(level, mob, body),
                // LevelSpaces.framesTouching(level, body),
                // first != null);
            }
        }
        if (first == null && !flying && mob.onGround()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    var recovery = GolemPathGeometry.startingSurface(
                            level,
                            mob,
                            start.x + dx * 0.25,
                            start.z + dz * 0.25,
                            start.y - 1.0,
                            start.y + mob.maxUpStep() + 0.1);
                    if (recovery != null
                            && (first == null
                                    || recovery.worldFeet().distanceToSqr(goal)
                                            > first.worldFeet().distanceToSqr(goal))) first = recovery;
                }
            }
        }
        if (first == null) {
            // Thaumaturge.LOGGER.debug(
            // "[golem-task] tick={} golem={} path target={} no starting surface at {}",
            // level.getGameTime(),
            // mob.getUUID(),
            // target,
            // start);
            return null;
        }
        SpacePath path = search(first, target, goal, accuracy, range, GRID, 1.0, false);
        if (path != null || flying || accuracy == 0) return path;
        Vec3 up = first.frame().directionToWorld(new Vec3(0, 1, 0));
        double jump = !first.frame().isWorld() && up.y > 0.5 ? 1.0 + Math.min(0.5, up.horizontalDistance()) : 1.0;
        if (jump > 1.01) {
            path = search(first, target, goal, accuracy, range, GRID, jump, false);
            if (path != null) return path;
        }
        path = search(
                first,
                target,
                goal,
                accuracy,
                range,
                GRID / 2,
                jump,
                !first.frame().isWorld());
        if (path == null && !first.frame().isWorld())
            path = search(first, target, goal, accuracy, range, GRID / 2, jump, false);
        if (path == null && level instanceof ServerLevel) {
            failedTarget = target.immutable();
            failedStart = start;
            failedGoal = goal;
            retryAfter = level.getGameTime() + 20;
        }
        return path;
    }

    private @Nullable SpacePath search(
            GolemPathGeometry.Surface first,
            BlockPos target,
            Vec3 goal,
            int accuracy,
            float range,
            double grid,
            double jumpHeight,
            boolean localGrid) {
        Vec3 start = mob.position();
        double reachSqr = Math.max(1.0, accuracy * accuracy + 0.5);
        // Favor progress toward the target so tall stairs fit within the search budget.
        PriorityQueue<Step> open =
                new PriorityQueue<>(Comparator.comparingDouble(step -> step.cost + 1.5 * step.estimate));
        Map<BlockPos, Double> best = new HashMap<>();
        // Cache only within this synchronous search: no stale collision state across ticks.
        Map<SurfaceQuery, Optional<GolemPathGeometry.Surface>> surfaces = new HashMap<>();
        Step root = new Step(first, null, 0.0, first.worldFeet().distanceTo(goal));
        open.add(root);
        best.put(key(localGrid ? first.localFeet() : first.worldFeet(), grid), 0.0);
        int visited = 0;
        while (!open.isEmpty() && visited++ < (grid == GRID ? MAX_VISITED : MAX_VISITED / 4)) {
            Step step = open.remove();
            Vec3 from = step.surface.worldFeet();
            if (step.cost
                    > best.getOrDefault(
                            key(localGrid ? first.frame().toLocal(from) : from, grid), Double.POSITIVE_INFINITY))
                continue;
            if (from.distanceToSqr(goal) <= reachSqr) {
                List<GolemPathGeometry.Surface> points = new ArrayList<>();
                for (Step cursor = step; cursor != null; cursor = cursor.previous) points.addFirst(cursor.surface);
                // Thaumaturge.LOGGER.debug(
                // "[golem-task] tick={} golem={} path target={} found visited={} points={}",
                // level.getGameTime(),
                // mob.getUUID(),
                // target,
                // visited,
                // points.size());
                return new SpacePath(points, target, reachSqr, jumpHeight);
            }
            int limit = grid == GRID ? 2 : 4;
            for (int dx = -limit; dx <= limit; dx++) {
                for (int dz = -limit; dz <= limit; dz++) {
                    if (Math.abs(dx) == limit && Math.abs(dz) == limit) continue;
                    if (flying && (Math.abs(dx) > 1 || Math.abs(dz) > 1)) continue;
                    for (int dy = flying ? -1 : 0; dy <= (flying ? 1 : 0); dy++) {
                        if (dx == 0 && dz == 0 && dy == 0) continue;
                        double spacing = flying ? GRID : grid;
                        Vec3 offset = localGrid
                                ? first.frame()
                                        .toWorld(first.frame().toLocal(from).add(dx * spacing, 0, dz * spacing))
                                : from.add(dx * spacing, 0, dz * spacing);
                        double x = offset.x;
                        double z = offset.z;
                        var surface = flying
                                ? new GolemPathGeometry.Surface(
                                        WorldFrame.INSTANCE, new Vec3(x, from.y + dy * spacing, z))
                                : surface(
                                        surfaces,
                                        x,
                                        z,
                                        from.y - Math.min(3, mob.getMaxFallDistance()),
                                        from.y + Math.max(jumpHeight, mob.maxUpStep()) + SURFACE_RAY_MARGIN);
                        if (surface == null) continue;
                        Vec3 to = surface.worldFeet();
                        if (step.previous == null
                                && avoided != null
                                && target.equals(avoidedTarget)
                                && level.getGameTime() < avoidUntil
                                && to.distanceToSqr(avoided.worldFeet()) < 0.09) continue;
                        if (!flying && to.y - from.y > Math.max(jumpHeight, mob.maxUpStep()) + 0.001) continue;
                        if (to.distanceToSqr(start) > range * range) continue;
                        double cost = step.cost + from.distanceTo(to);
                        BlockPos node = key(localGrid ? first.frame().toLocal(to) : to, grid);
                        // Check the best-known cost before the very expensive edge collision sweep.
                        if (cost >= best.getOrDefault(node, Double.POSITIVE_INFINITY)) continue;
                        boolean longEdge = Math.max(Math.abs(dx), Math.abs(dz)) * grid >= 0.75;
                        boolean supported = edgeClear(from, to, surfaces);
                        if (longEdge ? supported || !jumpClear(from, to, jumpHeight) : !supported) continue;
                        best.put(node, cost);
                        open.add(new Step(surface, step, cost, to.distanceTo(goal)));
                    }
                }
            }
        }
        // Thaumaturge.LOGGER.debug(
        // "[golem-task] tick={} golem={} path target={} failed visited={} remaining={} start={} goal={}",
        // level.getGameTime(),
        // mob.getUUID(),
        // target,
        // visited,
        // open.size(),
        // start,
        // goal);
        return null;
    }

    private GolemPathGeometry.@Nullable Surface surface(
            Map<SurfaceQuery, Optional<GolemPathGeometry.Surface>> cache,
            double x,
            double z,
            double minY,
            double maxY) {
        return cache.computeIfAbsent(
                        new SurfaceQuery(x, z, minY, maxY),
                        ignored -> Optional.ofNullable(GolemPathGeometry.surface(level, mob, x, z, minY, maxY)))
                .orElse(null);
    }

    private boolean edgeClear(Vec3 from, Vec3 to) {
        return edgeClear(from, to, new HashMap<>());
    }

    boolean jumpClear(Vec3 from, Vec3 to) {
        return jumpClear(from, to, 1.0);
    }

    private boolean jumpClear(Vec3 from, Vec3 to, double jumpHeight) {
        double horizontal = from.subtract(to).horizontalDistanceSqr();
        if (horizontal <= 0.75 * 0.75 || horizontal > 1.3 || to.y - from.y > jumpHeight || to.y - from.y < -1)
            return false;
        for (int i = 1; i < 8; i++) {
            double t = (double) i / 8;
            double height =
                    Math.min(jumpHeight + 0.2, (to.y - from.y) * t + (jumpHeight - 0.3) * Math.sin(Math.PI * t));
            Vec3 point = new Vec3(from.x + (to.x - from.x) * t, from.y + height + 0.001, from.z + (to.z - from.z) * t);
            if (!GolemPathGeometry.clear(level, mob, GolemPathGeometry.body(mob, point))) return false;
        }
        return true;
    }

    private boolean edgeClear(Vec3 from, Vec3 to, Map<SurfaceQuery, Optional<GolemPathGeometry.Surface>> cache) {
        int samples = Math.max(1, (int) Math.ceil(from.distanceTo(to) / 0.2));
        for (int i = 1; i <= samples; i++) {
            Vec3 point = from.lerp(to, (double) i / samples);
            if (!flying) {
                var surface = surface(
                        cache,
                        point.x,
                        point.z,
                        Math.min(from.y, to.y) - 0.1,
                        Math.max(from.y, to.y) + mob.maxUpStep());
                if (surface == null) return false;
                point = surface.worldFeet();
            }
            double half = mob.getBbWidth() / 2.0;
            if (!GolemPathGeometry.clear(
                    level,
                    mob,
                    new AABB(
                            point.x - half,
                            point.y + 1.0E-4,
                            point.z - half,
                            point.x + half,
                            point.y + mob.getBbHeight(),
                            point.z + half))) return false;
        }
        return true;
    }

    private static BlockPos key(Vec3 pos, double grid) {
        return BlockPos.containing(pos.x / grid, pos.y / grid, pos.z / grid);
    }

    private record SurfaceQuery(double x, double z, double minY, double maxY) {}

    private record Step(
            GolemPathGeometry.Surface surface, @Nullable Step previous, double cost, double estimate) {}

    private static final class SpacePath extends Path {
        private final List<GolemPathGeometry.Surface> points;
        private final double reachSqr;
        private final double jumpHeight;

        private SpacePath(List<GolemPathGeometry.Surface> points, BlockPos target, double reachSqr, double jumpHeight) {
            super(
                    points.stream()
                            .map(point -> {
                                BlockPos pos = BlockPos.containing(point.worldFeet());
                                return new Node(pos.getX(), pos.getY(), pos.getZ());
                            })
                            .toList(),
                    target,
                    true);
            this.points = points;
            this.reachSqr = reachSqr;
            this.jumpHeight = jumpHeight;
        }
    }
}
