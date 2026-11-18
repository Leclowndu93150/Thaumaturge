package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import net.minecraft.core.BlockPos;
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
    private static final int MAX_VISITED = 4096;
    private static final int REPATH_TICKS = 10;
    private final PathNavigation nativeNavigation;
    private final boolean flying;
    private int routeAge;
    private Vec3 lastProgress = Vec3.ZERO;
    private int stalled;

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
        if (sameFrame && (!sourceFrame.isWorld() || ordinaryWorld)) {
            Path nativePath = nativeNavigation.createPath(target, accuracy);
            if (ordinaryWorld || nativePath != null && nativePath.canReach()) return nativePath;
        }
        if (path instanceof SpacePath current
                && !current.isDone()
                && current.getTarget().equals(target)
                && routeAge < REPATH_TICKS
                && valid(current)) {
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
            lastProgress = mob.position();
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
        if (!valid(route)) {
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
        if (mob.position().distanceToSqr(lastProgress) > 0.04) {
            lastProgress = mob.position();
            stalled = 0;
        } else if (++stalled > 40) {
            stop();
            return;
        }
        mob.getMoveControl().setWantedPosition(next.x, next.y, next.z, speedModifier);
    }

    @Override
    public void recomputePath() {
        if (path instanceof SpacePath route) {
            BlockPos target = route.getTarget();
            routeAge = REPATH_TICKS;
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

    private boolean valid(SpacePath route) {
        Vec3 previous = mob.position();
        for (int i = route.getNextNodeIndex(); i < route.points.size(); i++) {
            Vec3 next = route.points.get(i).worldFeet();
            if (previous.distanceToSqr(next) > 3.0) return false;
            if (i == route.getNextNodeIndex() && !edgeClear(previous, next)) return false;
            previous = next;
        }
        return previous.distanceToSqr(LevelSpaces.worldCenter(level, route.getTarget())) <= route.reachSqr;
    }

    private @Nullable SpacePath findPath(BlockPos target, Vec3 goal, int accuracy, float range) {
        Vec3 start = mob.position();
        if (start.distanceToSqr(goal) > range * range) return null;
        var first = flying
                ? new GolemPathGeometry.Surface(WorldFrame.INSTANCE, start)
                : GolemPathGeometry.surface(level, mob, start.x, start.z, start.y - 1.0, start.y + 0.1);
        if (first == null) return null;
        double reachSqr = Math.max(1.0, accuracy * accuracy + 0.5);
        PriorityQueue<Step> open = new PriorityQueue<>(Comparator.comparingDouble(step -> step.cost + step.estimate));
        Map<BlockPos, Double> best = new HashMap<>();
        Step root = new Step(first, null, 0.0, first.worldFeet().distanceTo(goal));
        open.add(root);
        best.put(key(first.worldFeet()), 0.0);
        int visited = 0;
        while (!open.isEmpty() && visited++ < MAX_VISITED) {
            Step step = open.remove();
            Vec3 from = step.surface.worldFeet();
            if (step.cost > best.getOrDefault(key(from), Double.POSITIVE_INFINITY)) continue;
            if (from.distanceToSqr(goal) <= reachSqr) {
                List<GolemPathGeometry.Surface> points = new ArrayList<>();
                for (Step cursor = step; cursor != null; cursor = cursor.previous) points.addFirst(cursor.surface);
                return new SpacePath(points, target, reachSqr);
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = flying ? -1 : 0; dy <= (flying ? 1 : 0); dy++) {
                        if (dx == 0 && dz == 0 && dy == 0) continue;
                        double x = from.x + dx * GRID;
                        double z = from.z + dz * GRID;
                        var surface = flying
                                ? new GolemPathGeometry.Surface(WorldFrame.INSTANCE, new Vec3(x, from.y + dy * GRID, z))
                                : GolemPathGeometry.surface(
                                        level,
                                        mob,
                                        x,
                                        z,
                                        from.y - Math.min(3, mob.getMaxFallDistance()),
                                        from.y + mob.maxUpStep());
                        if (surface == null) continue;
                        Vec3 to = surface.worldFeet();
                        if (to.distanceToSqr(start) > range * range || !edgeClear(from, to)) continue;
                        double cost = step.cost + from.distanceTo(to);
                        BlockPos key = key(to);
                        if (cost >= best.getOrDefault(key, Double.POSITIVE_INFINITY)) continue;
                        best.put(key, cost);
                        open.add(new Step(surface, step, cost, to.distanceTo(goal)));
                    }
                }
            }
        }
        return null;
    }

    private boolean edgeClear(Vec3 from, Vec3 to) {
        int samples = Math.max(1, (int) Math.ceil(from.distanceTo(to) / 0.2));
        for (int i = 1; i <= samples; i++) {
            Vec3 point = from.lerp(to, (double) i / samples);
            if (!flying) {
                var surface = GolemPathGeometry.surface(
                        level,
                        mob,
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

    private static BlockPos key(Vec3 pos) {
        return BlockPos.containing(pos.x / GRID, pos.y / GRID, pos.z / GRID);
    }

    private record Step(
            GolemPathGeometry.Surface surface, @Nullable Step previous, double cost, double estimate) {}

    private static final class SpacePath extends Path {
        private final List<GolemPathGeometry.Surface> points;
        private final double reachSqr;

        private SpacePath(List<GolemPathGeometry.Surface> points, BlockPos target, double reachSqr) {
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
        }
    }
}
