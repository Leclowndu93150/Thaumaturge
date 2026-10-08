package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class SocketProfile {
    public static final int HEIGHT = 48;
    public static final int FLOOR = 18;
    public static final int MIN_U = 5;
    public static final int MAX_U = 10;
    public static final int CROWN_MIN_U = 6;
    public static final int CROWN_MAX_U = 9;
    public static final int BOTTOM = FLOOR + 1;
    public static final int TOP = FLOOR + 5;
    public static final int CROWN = FLOOR + 6;
    public static final int LAST = MazeGeometry.CELL - 1;

    private SocketProfile() {}

    public static boolean open(int u, int y) {
        if (y >= BOTTOM && y <= TOP) {
            return u >= MIN_U && u <= MAX_U;
        }
        return y == CROWN && u >= CROWN_MIN_U && u <= CROWN_MAX_U;
    }

    public static BlockPos planePos(int cellX, int cellZ, Direction side, int u, int y) {
        int baseX = cellX * MazeGeometry.CELL;
        int baseZ = cellZ * MazeGeometry.CELL;
        return switch (side) {
            case NORTH -> new BlockPos(baseX + u, y, baseZ);
            case SOUTH -> new BlockPos(baseX + u, y, baseZ + LAST);
            case WEST -> new BlockPos(baseX, y, baseZ + u);
            default -> new BlockPos(baseX + LAST, y, baseZ + u);
        };
    }
}
