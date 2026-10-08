package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongHeapPriorityQueue;
import java.util.Arrays;
import java.util.function.IntPredicate;
import net.minecraft.core.Direction;

final class GridSearch {
    private static final int INDEX_BITS = 20;
    private static final long INDEX_MASK = (1L << INDEX_BITS) - 1;

    private GridSearch() {}

    static IntList walk(LayoutWork work, int source, EdgeStep step) {
        IntList order = new IntArrayList();
        boolean[] seen = new boolean[work.cells()];
        IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
        seen[source] = true;
        queue.enqueue(source);
        while (!queue.isEmpty()) {
            int cell = queue.dequeueInt();
            order.add(cell);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                int next = work.neighbor(cell, direction);
                if (next != LayoutWork.NONE
                        && !seen[next]
                        && work.hasEdge(cell, direction)
                        && step.enter(cell, direction, next)) {
                    seen[next] = true;
                    queue.enqueue(next);
                }
            }
        }
        return order;
    }

    static int[] edgeDistances(LayoutWork work, int source) {
        int[] distance = unreached(work);
        distance[source] = 0;
        walk(work, source, (from, direction, to) -> {
            distance[to] = distance[from] + 1;
            return true;
        });
        return distance;
    }

    static int edgeDistance(LayoutWork work, int from, int to, int limit) {
        int[] distance = unreached(work);
        distance[from] = 0;
        walk(work, from, (cell, direction, next) -> {
            if (distance[cell] >= limit) {
                return false;
            }
            distance[next] = distance[cell] + 1;
            return true;
        });
        return distance[to];
    }

    static IntList component(LayoutWork work) {
        IntList cells = new IntArrayList();
        for (int cell :
                walk(work, work.portalCell, (from, direction, to) -> to != work.keyCell && to != work.hallCell)) {
            if (!work.reserved[cell]) {
                cells.add(cell);
            }
        }
        return cells;
    }

    private static int[] unreached(LayoutWork work) {
        int[] distance = new int[work.cells()];
        Arrays.fill(distance, Integer.MAX_VALUE);
        return distance;
    }

    static int[] gridDistances(LayoutWork work, int source, IntPredicate passable) {
        int[] distance = unreached(work);
        IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
        distance[source] = 0;
        queue.enqueue(source);
        while (!queue.isEmpty()) {
            int cell = queue.dequeueInt();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                int next = work.neighbor(cell, direction);
                if (next != LayoutWork.NONE
                        && distance[next] == Integer.MAX_VALUE
                        && passable.test(next)
                        && (cell != source || work.canConnect(cell, direction))) {
                    distance[next] = distance[cell] + 1;
                    queue.enqueue(next);
                }
            }
        }
        return distance;
    }

    static IntList cheapestPath(LayoutWork work, IntList sources, int target, CostField cost, IntPredicate enterable) {
        int cells = work.cells();
        long[] best = new long[cells];
        int[] parent = new int[cells];
        Arrays.fill(best, Long.MAX_VALUE);
        Arrays.fill(parent, LayoutWork.NONE);
        LongHeapPriorityQueue queue = new LongHeapPriorityQueue();
        for (int i = 0; i < sources.size(); i++) {
            int source = sources.getInt(i);
            best[source] = 0;
            queue.enqueue(source);
        }
        while (!queue.isEmpty()) {
            long entry = queue.dequeueLong();
            int cell = (int) (entry & INDEX_MASK);
            long distance = entry >>> INDEX_BITS;
            if (distance > best[cell]) {
                continue;
            }
            if (cell == target) {
                break;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                int next = work.neighbor(cell, direction);
                if (next == LayoutWork.NONE
                        || !work.canConnect(cell, direction)
                        || next != target && !enterable.test(next)) {
                    continue;
                }
                long candidate = distance + cost.cost(next);
                if (candidate < best[next]) {
                    best[next] = candidate;
                    parent[next] = cell;
                    queue.enqueue((candidate << INDEX_BITS) | next);
                }
            }
        }
        IntList path = new IntArrayList();
        if (best[target] == Long.MAX_VALUE) {
            return path;
        }
        for (int cell = target; cell != LayoutWork.NONE; cell = parent[cell]) {
            path.add(0, cell);
        }
        return path;
    }

    static Direction between(LayoutWork work, int from, int to) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (work.neighbor(from, direction) == to) {
                return direction;
            }
        }
        throw new IllegalStateException("Cells " + from + " and " + to + " are not adjacent");
    }

    @FunctionalInterface
    interface EdgeStep {
        boolean enter(int from, Direction direction, int to);
    }
}
