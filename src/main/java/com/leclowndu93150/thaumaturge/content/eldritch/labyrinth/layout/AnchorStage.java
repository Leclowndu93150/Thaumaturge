package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeCells;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.EncounterEntry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomSocket;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;

final class AnchorStage implements LayoutStage {
    private static final int CORNERS = 4;
    private static final int MIN_KEY_DISTANCE = 2;

    @Override
    public void apply(LayoutWork work, LayoutContext context) {
        placePortal(work, context);
        placeHall(work, context);
        chooseKeyCell(work, context);
    }

    private static void placePortal(LayoutWork work, LayoutContext context) {
        List<Holder<RoomType>> portals =
                singleCell(RoomCatalog.sorted(context.definition().rooms().portal(), context.enabled()));
        Holder<RoomType> portal = WeightedPick.byRoomWeight(portals, room -> room, context.random())
                .orElseThrow(() -> new IllegalStateException("No usable portal room in the labyrinth definition"));
        int transform = context.randomTransform(portal);
        int cell = work.index(work.width / 2, work.depth / 2);
        work.portalCell = cell;
        work.placeSingle(context, portal, transform, cell);
        int sockets = Dihedral.mask(portal.value(), transform);
        work.sealExcept(cell, sockets);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (MazeCells.open(sockets, direction)) {
                work.connect(cell, direction);
            }
        }
    }

    private static void placeHall(LayoutWork work, LayoutContext context) {
        LabyrinthDefinition definition = context.definition();
        Optional<EncounterEntry> entry = WeightedPick.pick(
                definition.encounters().stream()
                        .filter(candidate -> candidate.encounter().isBound())
                        .toList(),
                EncounterEntry::weight,
                context.random());
        work.encounter = entry.map(EncounterEntry::encounter);
        HolderSet<RoomType> hallSet =
                entry.flatMap(EncounterEntry::halls).orElse(definition.rooms().bossHalls());
        List<Holder<RoomType>> halls = singleDoor(RoomCatalog.sorted(hallSet, context.enabled()));
        if (halls.isEmpty()) {
            halls = singleDoor(RoomCatalog.sorted(definition.rooms().bossHalls(), context.enabled()));
        }
        Holder<RoomType> hall = WeightedPick.byRoomWeight(halls, room -> room, context.random())
                .orElseThrow(() -> new IllegalStateException("No usable boss hall in the labyrinth definition"));
        int[] corners = {0, 1, 2, 3};
        WeightedPick.shuffle(corners, context.random());
        int[] order = Dihedral.allowedTransforms(hall.value().transforms()).toIntArray();
        WeightedPick.shuffle(order, context.random());
        for (int corner : corners) {
            for (int transform : order) {
                if (tryHall(work, context, hall, corner, transform)) {
                    return;
                }
            }
        }
        throw new IllegalStateException("The boss hall " + RoomType.id(hall) + " fits no corner of a " + work.width
                + "x" + work.depth + " maze");
    }

    private static boolean tryHall(
            LayoutWork work, LayoutContext context, Holder<RoomType> hall, int corner, int transform) {
        RoomType type = hall.value();
        int width = Dihedral.width(type.width(), type.depth(), transform);
        int depth = Dihedral.depth(type.width(), type.depth(), transform);
        int originX = (corner & 1) == 0 ? 0 : work.width - width;
        int originZ = corner < CORNERS / 2 ? 0 : work.depth - depth;
        int[] cells = new int[width * depth];
        int i = 0;
        for (int dz = 0; dz < depth; dz++) {
            for (int dx = 0; dx < width; dx++) {
                int cell = work.index(originX + dx, originZ + dz);
                if (work.reserved[cell]) {
                    return false;
                }
                cells[i++] = cell;
            }
        }
        IntList socketCells = new IntArrayList();
        List<Direction> socketSides = new ArrayList<>();
        for (RoomSocket socket : type.sockets()) {
            BlockPos moved = Dihedral.cell(socket.cellX(), socket.cellZ(), type.width(), type.depth(), transform);
            int cell = work.index(originX + moved.getX(), originZ + moved.getZ());
            Direction side = Dihedral.side(socket.side(), transform);
            int door = work.neighbor(cell, side);
            if (door == LayoutWork.NONE
                    || work.reserved[door]
                    || contains(cells, door)
                    || adjacentToReserved(work, door, cells)) {
                return false;
            }
            socketCells.add(cell);
            socketSides.add(side);
        }
        work.place(new RoomPlacement(hall, context.template(hall), transform, originX, originZ), cells);
        for (int cell : cells) {
            work.sealExcept(cell, 0);
        }
        for (int s = 0; s < socketCells.size(); s++) {
            int cell = socketCells.getInt(s);
            Direction side = socketSides.get(s);
            work.forbidden[cell] &= ~MazeCells.bit(side);
            int door = work.neighbor(cell, side);
            work.forbidden[door] &= ~MazeCells.bit(side.getOpposite());
            work.connect(cell, side);
            work.hallCell = cell;
            work.doorCell = door;
        }
        return true;
    }

    private static boolean adjacentToReserved(LayoutWork work, int door, int[] hallCells) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int next = work.neighbor(door, direction);
            if (next != LayoutWork.NONE && work.reserved[next] && !contains(hallCells, next)) {
                return true;
            }
        }
        return false;
    }

    private static void chooseKeyCell(LayoutWork work, LayoutContext context) {
        int[] distance = GridSearch.gridDistances(work, work.portalCell, cell -> !work.reserved[cell]);
        int radius = 0;
        for (int cell = 0; cell < work.cells(); cell++) {
            if (distance[cell] != Integer.MAX_VALUE) {
                radius = Math.max(radius, distance[cell]);
            }
        }
        int hallQuadrant = quadrant(work, work.hallCell);
        int threshold = Math.max(
                MIN_KEY_DISTANCE, Math.round(context.definition().layout().keyDistance() * radius));
        int bonus = Math.max(1, radius / 2);
        while (threshold >= 1) {
            IntList candidates = new IntArrayList();
            for (int cell = 0; cell < work.cells(); cell++) {
                if (distance[cell] != Integer.MAX_VALUE && distance[cell] >= threshold && keyEligible(work, cell)) {
                    candidates.add(cell);
                }
            }
            if (!candidates.isEmpty()) {
                int opposite = (hallQuadrant + 2) % CORNERS;
                Optional<Integer> chosen = WeightedPick.pick(
                        new ArrayList<>(candidates),
                        cell -> distance[cell] + (quadrant(work, cell) == opposite ? bonus : 0),
                        context.random());
                work.keyCell = chosen.orElseThrow();
                work.reserved[work.keyCell] = true;
                return;
            }
            threshold--;
        }
        throw new IllegalStateException("No cell can hold the key room");
    }

    private static boolean keyEligible(LayoutWork work, int cell) {
        if (work.reserved[cell] || cell == work.doorCell || work.open[cell]) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int next = work.neighbor(cell, direction);
            if (next != LayoutWork.NONE && (next == work.doorCell || work.reserved[next] && next != work.portalCell)) {
                return false;
            }
        }
        return true;
    }

    private static int quadrant(LayoutWork work, int cell) {
        boolean east = work.x(cell) * 2 >= work.width;
        boolean south = work.z(cell) * 2 >= work.depth;
        return (east ? 1 : 0) + (south ? 2 : 0);
    }

    private static boolean contains(int[] cells, int cell) {
        for (int value : cells) {
            if (value == cell) {
                return true;
            }
        }
        return false;
    }

    private static List<Holder<RoomType>> singleCell(List<Holder<RoomType>> rooms) {
        return rooms.stream()
                .filter(room -> room.value().width() == 1 && room.value().depth() == 1)
                .toList();
    }

    private static List<Holder<RoomType>> singleDoor(List<Holder<RoomType>> rooms) {
        return rooms.stream().filter(room -> room.value().sockets().size() == 1).toList();
    }
}
