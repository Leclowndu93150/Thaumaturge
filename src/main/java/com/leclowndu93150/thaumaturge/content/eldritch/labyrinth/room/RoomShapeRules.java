package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeGeometry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomSocket;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class RoomShapeRules {
    private static final int MAX_PROBLEMS = 8;

    private RoomShapeRules() {}

    public static List<String> check(RoomVoxels voxels, int width, int depth, List<RoomSocket> sockets) {
        List<String> problems = new ArrayList<>();
        if (voxels.sizeX() != width * MazeGeometry.CELL
                || voxels.sizeZ() != depth * MazeGeometry.CELL
                || voxels.sizeY() != SocketProfile.HEIGHT) {
            problems.add("size " + voxels.sizeX() + "x" + voxels.sizeY() + "x" + voxels.sizeZ() + " does not match a "
                    + width + "x" + depth + " footprint");
            return problems;
        }
        checkPerimeter(voxels, width, depth, sockets, problems);
        checkSealed(voxels, problems);
        if (problems.isEmpty()) {
            checkConnected(voxels, sockets, problems);
        }
        return problems;
    }

    private static void checkPerimeter(
            RoomVoxels voxels, int width, int depth, List<RoomSocket> sockets, List<String> problems) {
        for (int cellX = 0; cellX < width; cellX++) {
            for (int cellZ = 0; cellZ < depth; cellZ++) {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    int nextX = cellX + side.getStepX();
                    int nextZ = cellZ + side.getStepZ();
                    if (nextX >= 0 && nextZ >= 0 && nextX < width && nextZ < depth) {
                        continue;
                    }
                    boolean socket = hasSocket(sockets, cellX, cellZ, side);
                    for (int u = 0; u < MazeGeometry.CELL; u++) {
                        for (int y = 0; y < SocketProfile.HEIGHT; y++) {
                            BlockPos pos = SocketProfile.planePos(cellX, cellZ, side, u, y);
                            boolean open = voxels.kind(pos.getX(), pos.getY(), pos.getZ())
                                    .open();
                            boolean expected = socket && SocketProfile.open(u, y);
                            if (open != expected && problems.size() < MAX_PROBLEMS) {
                                problems.add((expected ? "socket gap closed" : "unexpected opening") + " at "
                                        + pos.toShortString() + " on the " + side + " side of cell " + cellX + ","
                                        + cellZ);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void checkSealed(RoomVoxels voxels, List<String> problems) {
        for (int x = 0; x < voxels.sizeX(); x++) {
            for (int y = 0; y < voxels.sizeY(); y++) {
                for (int z = 0; z < voxels.sizeZ(); z++) {
                    if (voxels.kind(x, y, z) != RoomVoxels.Kind.AIR) {
                        continue;
                    }
                    for (Direction direction : Direction.values()) {
                        int nx = x + direction.getStepX();
                        int ny = y + direction.getStepY();
                        int nz = z + direction.getStepZ();
                        boolean leaks = ny < 0
                                || ny >= voxels.sizeY()
                                || nx >= 0
                                        && nz >= 0
                                        && nx < voxels.sizeX()
                                        && nz < voxels.sizeZ()
                                        && voxels.kind(nx, ny, nz) == RoomVoxels.Kind.UNSPECIFIED;
                        if (leaks && problems.size() < MAX_PROBLEMS) {
                            problems.add(
                                    "air at " + x + "," + y + "," + z + " touches the void fill toward " + direction);
                        }
                    }
                }
            }
        }
    }

    private static void checkConnected(RoomVoxels voxels, List<RoomSocket> sockets, List<String> problems) {
        int sx = voxels.sizeX();
        int sy = voxels.sizeY();
        int sz = voxels.sizeZ();
        boolean[] seen = new boolean[sx * sy * sz];
        IntArrayFIFOQueue queue = new IntArrayFIFOQueue();
        RoomSocket first = sockets.getFirst();
        BlockPos start = SocketProfile.planePos(
                first.cellX(), first.cellZ(), first.side(), SocketProfile.MIN_U, SocketProfile.BOTTOM);
        int startIndex = RoomVoxels.index(start.getX(), start.getY(), start.getZ(), sy, sz);
        seen[startIndex] = true;
        queue.enqueue(startIndex);
        while (!queue.isEmpty()) {
            int current = queue.dequeueInt();
            int x = current / (sy * sz);
            int y = (current / sz) % sy;
            int z = current % sz;
            for (Direction direction : Direction.values()) {
                int nx = x + direction.getStepX();
                int ny = y + direction.getStepY();
                int nz = z + direction.getStepZ();
                if (nx < 0 || ny < 0 || nz < 0 || nx >= sx || ny >= sy || nz >= sz) {
                    continue;
                }
                int next = RoomVoxels.index(nx, ny, nz, sy, sz);
                if (!seen[next] && voxels.kind(nx, ny, nz).open()) {
                    seen[next] = true;
                    queue.enqueue(next);
                }
            }
        }
        for (RoomSocket socket : sockets) {
            BlockPos pos = SocketProfile.planePos(
                    socket.cellX(), socket.cellZ(), socket.side(), SocketProfile.MIN_U, SocketProfile.BOTTOM);
            if (!seen[RoomVoxels.index(pos.getX(), pos.getY(), pos.getZ(), sy, sz)]) {
                problems.add("socket " + socket.side() + " of cell " + socket.cellX() + "," + socket.cellZ()
                        + " is not reachable from the first socket");
            }
        }
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                for (int z = 0; z < sz; z++) {
                    if (voxels.kind(x, y, z) == RoomVoxels.Kind.MARKER
                            && !seen[RoomVoxels.index(x, y, z, sy, sz)]
                            && problems.size() < MAX_PROBLEMS) {
                        problems.add("marker at " + x + "," + y + "," + z + " is not reachable from the first socket");
                    }
                }
            }
        }
    }

    private static boolean hasSocket(List<RoomSocket> sockets, int cellX, int cellZ, Direction side) {
        for (RoomSocket socket : sockets) {
            if (socket.cellX() == cellX && socket.cellZ() == cellZ && socket.side() == side) {
                return true;
            }
        }
        return false;
    }
}
