package com.leclowndu93150.thaumaturge.content.effect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class StreamPathfinder {
    private static final int NODE_BUDGET = 4096;
    private static final int MAX_RANGE = 40;
    private static final double LOS_STEP = 0.3;
    private static final double HEURISTIC_WEIGHT = 1.5;
    private static final double WALL_CLEARANCE = 0.4;
    private static final double MIN_PUSH_SQ = 1.0E-4;

    private StreamPathfinder() {}

    public record Result(@Nullable List<Vec3> waypoints, boolean directSight, int expanded) {
    }

    public static @Nullable List<Vec3> findRoute(BlockGetter level, Vec3 from, Vec3 to) {
        return explore(level, from, to).waypoints();
    }

    public static Result explore(BlockGetter level, Vec3 from, Vec3 to) {
        if (hasLineOfSight(level, from, to)) {
            return new Result(List.of(), true, 0);
        }
        BlockPos start = BlockPos.containing(from);
        BlockPos goal = BlockPos.containing(to);
        if (start.distManhattan(goal) > MAX_RANGE) {
            return new Result(null, false, 0);
        }
        Search search = new Search(level, start, goal);
        List<BlockPos> path = search.run();
        List<Vec3> waypoints = path == null ? null : smooth(level, from, to, path);
        return new Result(waypoints, false, search.expanded);
    }

    private record Node(BlockPos pos, int walked, double score, @Nullable Node parent) {
    }

    private static final class Search {
        private final BlockGetter level;
        private final BlockPos start;
        private final BlockPos goal;
        int expanded;

        Search(BlockGetter level, BlockPos start, BlockPos goal) {
            this.level = level;
            this.start = start;
            this.goal = goal;
        }

        @Nullable
        List<BlockPos> run() {
            PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::score));
            Map<BlockPos, Integer> best = new HashMap<>();
            open.add(new Node(start, 0, heuristic(start, goal), null));
            best.put(start, 0);
            while (!open.isEmpty() && expanded++ < NODE_BUDGET) {
                Node current = open.poll();
                if (current.pos().equals(goal)) {
                    return unwind(current);
                }
                for (Direction dir : Direction.values()) {
                    BlockPos next = current.pos().relative(dir);
                    if (!next.equals(goal) && !passable(level, next)) {
                        continue;
                    }
                    int walked = current.walked() + 1;
                    Integer known = best.get(next);
                    if (known != null && known <= walked) {
                        continue;
                    }
                    best.put(next, walked);
                    open.add(new Node(next, walked, walked + heuristic(next, goal), current));
                }
            }
            return null;
        }
    }

    private static double heuristic(BlockPos from, BlockPos goal) {
        return Math.sqrt(from.distSqr(goal)) * HEURISTIC_WEIGHT;
    }

    private static List<BlockPos> unwind(Node node) {
        List<BlockPos> path = new ArrayList<>();
        for (Node cursor = node; cursor != null; cursor = cursor.parent()) {
            path.add(cursor.pos());
        }
        Collections.reverse(path);
        return path;
    }

    private static List<Vec3> smooth(BlockGetter level, Vec3 from, Vec3 to, List<BlockPos> path) {
        List<Vec3> points = new ArrayList<>(path.size() + 1);
        for (BlockPos pos : path) {
            points.add(Vec3.atCenterOf(pos));
        }
        points.add(to);
        List<Vec3> waypoints = new ArrayList<>();
        Vec3 anchor = from;
        int index = 0;
        while (true) {
            int reach = index;
            for (int j = points.size() - 1; j > index; j--) {
                if (hasLineOfSight(level, anchor, points.get(j))) {
                    reach = j;
                    break;
                }
            }
            if (reach == points.size() - 1) {
                return addClearance(level, from, waypoints, to);
            }
            anchor = points.get(reach);
            waypoints.add(anchor);
            index = reach + 1;
        }
    }

    private static List<Vec3> addClearance(BlockGetter level, Vec3 from, List<Vec3> waypoints, Vec3 to) {
        if (waypoints.isEmpty()) {
            return waypoints;
        }
        List<Vec3> adjusted = new ArrayList<>(waypoints.size());
        Vec3 previous = from;
        for (int i = 0; i < waypoints.size(); i++) {
            Vec3 raw = waypoints.get(i);
            Vec3 next = i + 1 < waypoints.size() ? waypoints.get(i + 1) : to;
            Vec3 pushed = pushOffWalls(level, raw);
            if (pushed != raw && hasLineOfSight(level, previous, pushed) && hasLineOfSight(level, pushed, next)) {
                adjusted.add(pushed);
                previous = pushed;
            } else {
                adjusted.add(raw);
                previous = raw;
            }
        }
        return adjusted;
    }

    private static Vec3 pushOffWalls(BlockGetter level, Vec3 point) {
        BlockPos cell = BlockPos.containing(point);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        double pushX = 0.0;
        double pushY = 0.0;
        double pushZ = 0.0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    cursor.setWithOffset(cell, dx, dy, dz);
                    if (passable(level, cursor)) {
                        continue;
                    }
                    Vec3 away = point.subtract(Vec3.atCenterOf(cursor));
                    double falloff = 1.0 / Math.max(0.25, away.lengthSqr());
                    away = away.normalize().scale(falloff);
                    pushX += away.x;
                    pushY += away.y;
                    pushZ += away.z;
                }
            }
        }
        Vec3 push = new Vec3(pushX, pushY, pushZ);
        if (push.lengthSqr() < MIN_PUSH_SQ) {
            return point;
        }
        Vec3 target = point.add(push.normalize().scale(WALL_CLEARANCE));
        return passable(level, BlockPos.containing(target)) ? target : point;
    }

    private static boolean passable(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isCollisionShapeFullBlock(level, pos);
    }

    public static boolean hasLineOfSight(BlockGetter level, Vec3 a, Vec3 b) {
        double distance = a.distanceTo(b);
        int steps = Math.max(1, Mth.ceil(distance / LOS_STEP));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            cursor.set(Mth.floor(Mth.lerp(t, a.x, b.x)), Mth.floor(Mth.lerp(t, a.y, b.y)), Mth.floor(Mth.lerp(t, a.z, b.z)));
            BlockState state = level.getBlockState(cursor);
            if (state.isCollisionShapeFullBlock(level, cursor)) {
                return false;
            }
        }
        return true;
    }
}
