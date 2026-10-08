package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeCells;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;

final class LayoutWork {
    static final int NONE = -1;

    final int width;
    final int depth;
    final int[] edges;
    final int[] forbidden;
    final boolean[] open;
    final boolean[] reserved;
    final int[] room;
    final int[][] directions;
    final List<RoomPlacement> placements = new ArrayList<>();
    final Reference2IntMap<Holder<RoomType>> roomCounts = new Reference2IntOpenHashMap<>();
    int portalCell = NONE;
    int keyCell = NONE;
    int hallCell = NONE;
    int doorCell = NONE;
    Optional<Holder<LabyrinthEncounter>> encounter = Optional.empty();

    LayoutWork(int width, int depth) {
        this.width = width;
        this.depth = depth;
        int cells = width * depth;
        this.edges = new int[cells];
        this.forbidden = new int[cells];
        this.open = new boolean[cells];
        this.reserved = new boolean[cells];
        this.room = new int[cells];
        Arrays.fill(room, NONE);
        this.directions = new int[MazeCells.TARGETS][cells];
        for (int[] target : directions) {
            Arrays.fill(target, MazeCells.NO_DIRECTION);
        }
    }

    int count(Holder<RoomType> room) {
        return roomCounts.getInt(room);
    }

    int cells() {
        return width * depth;
    }

    int index(int x, int z) {
        return x + z * width;
    }

    int x(int cell) {
        return cell % width;
    }

    int z(int cell) {
        return cell / width;
    }

    int neighbor(int cell, Direction direction) {
        int x = x(cell) + direction.getStepX();
        int z = z(cell) + direction.getStepZ();
        return x >= 0 && z >= 0 && x < width && z < depth ? index(x, z) : NONE;
    }

    boolean hasEdge(int cell, Direction direction) {
        return MazeCells.open(edges[cell], direction);
    }

    boolean canConnect(int cell, Direction direction) {
        int next = neighbor(cell, direction);
        return next != NONE
                && !MazeCells.open(forbidden[cell], direction)
                && !MazeCells.open(forbidden[next], direction.getOpposite());
    }

    boolean canGrow(int cell, Direction direction) {
        int next = neighbor(cell, direction);
        return canConnect(cell, direction) && !open[next] && !reserved[next];
    }

    void connect(int cell, Direction direction) {
        int next = neighbor(cell, direction);
        edges[cell] |= MazeCells.bit(direction);
        edges[next] |= MazeCells.bit(direction.getOpposite());
        open[cell] = true;
        open[next] = true;
    }

    void forbid(int cell, Direction direction) {
        forbidden[cell] |= MazeCells.bit(direction);
        int next = neighbor(cell, direction);
        if (next != NONE) {
            forbidden[next] |= MazeCells.bit(direction.getOpposite());
        }
    }

    void sealExcept(int cell, int keepMask) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if ((keepMask & MazeCells.bit(direction)) == 0) {
                forbid(cell, direction);
            }
        }
    }

    int place(RoomPlacement placement, int[] cells) {
        int index = placements.size();
        placements.add(placement);
        roomCounts.mergeInt(placement.room(), 1, Integer::sum);
        for (int cell : cells) {
            room[cell] = index;
            reserved[cell] = true;
            open[cell] = true;
        }
        return index;
    }

    void placeSingle(LayoutContext context, Holder<RoomType> room, int transform, int cell) {
        place(new RoomPlacement(room, context.template(room), transform, x(cell), z(cell)), new int[] {cell});
    }

    int degree(int cell) {
        return Integer.bitCount(edges[cell]);
    }

    int reservedCount() {
        int count = 0;
        for (boolean value : reserved) {
            if (value) {
                count++;
            }
        }
        return count;
    }
}
