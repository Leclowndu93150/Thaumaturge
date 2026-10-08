package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public final class MazeCells {
    public static final int NO_DIRECTION = 7;
    public static final int NO_ROOM = -1;
    public static final int TARGET_ARRIVAL = 0;
    public static final int TARGET_KEY = 1;
    public static final int TARGET_DOOR = 2;
    public static final List<ResourceLocation> TARGET_LANDMARKS =
            List.of(LabyrinthLandmarks.ARRIVAL, LabyrinthLandmarks.KEY, LabyrinthLandmarks.BOSS_DOOR);
    public static final int TARGETS = TARGET_LANDMARKS.size();

    private static final int MASK_BITS = 0xF;
    private static final int ROOM_SHIFT = 4;
    private static final int ROOM_BITS = 0xFFF;
    private static final int DIRECTION_SHIFT = 16;
    private static final int DIRECTION_WIDTH = 3;
    private static final int DIRECTION_BITS = 0x7;

    private static final long PALETTE_BITS = 0xFFFL;
    private static final int TEMPLATE_SHIFT = 12;
    private static final int TRANSFORM_SHIFT = 24;
    private static final long TRANSFORM_BITS = 0x7L;
    private static final int ANCHOR_X_SHIFT = 27;
    private static final int ANCHOR_Z_SHIFT = 34;
    private static final long ANCHOR_BITS = 0x7FL;
    private static final int MIRROR_FLAG = 4;

    private MazeCells() {}

    public static int bit(Direction direction) {
        return 1 << direction.get2DDataValue();
    }

    public static boolean open(int mask, Direction direction) {
        return (mask & bit(direction)) != 0;
    }

    public static int packCell(int mask, int roomIndex, int[] directions) {
        int packed = (mask & MASK_BITS) | (((roomIndex + 1) & ROOM_BITS) << ROOM_SHIFT);
        for (int target = 0; target < TARGETS; target++) {
            packed |= (directions[target] & DIRECTION_BITS) << (DIRECTION_SHIFT + target * DIRECTION_WIDTH);
        }
        return packed;
    }

    public static int mask(int cell) {
        return cell & MASK_BITS;
    }

    public static int room(int cell) {
        return ((cell >>> ROOM_SHIFT) & ROOM_BITS) - 1;
    }

    public static Optional<Direction> direction(int cell, int target) {
        int value = (cell >>> (DIRECTION_SHIFT + target * DIRECTION_WIDTH)) & DIRECTION_BITS;
        return value == NO_DIRECTION ? Optional.empty() : Optional.of(Direction.from2DDataValue(value));
    }

    public static long packRoom(int paletteIndex, int templateIndex, int transform, int anchorX, int anchorZ) {
        return (paletteIndex & PALETTE_BITS)
                | ((templateIndex & PALETTE_BITS) << TEMPLATE_SHIFT)
                | ((transform & TRANSFORM_BITS) << TRANSFORM_SHIFT)
                | ((anchorX & ANCHOR_BITS) << ANCHOR_X_SHIFT)
                | ((anchorZ & ANCHOR_BITS) << ANCHOR_Z_SHIFT);
    }

    public static int palette(long room) {
        return (int) (room & PALETTE_BITS);
    }

    public static int template(long room) {
        return (int) ((room >>> TEMPLATE_SHIFT) & PALETTE_BITS);
    }

    public static int transform(long room) {
        return (int) ((room >>> TRANSFORM_SHIFT) & TRANSFORM_BITS);
    }

    public static int anchorX(long room) {
        return (int) ((room >>> ANCHOR_X_SHIFT) & ANCHOR_BITS);
    }

    public static int anchorZ(long room) {
        return (int) ((room >>> ANCHOR_Z_SHIFT) & ANCHOR_BITS);
    }

    public static int transform(Rotation rotation, Mirror mirror) {
        return rotation.ordinal() | (mirror == Mirror.NONE ? 0 : MIRROR_FLAG);
    }

    public static Rotation rotation(int transform) {
        return Rotation.values()[transform & (MIRROR_FLAG - 1)];
    }

    public static Mirror mirror(int transform) {
        return (transform & MIRROR_FLAG) != 0 ? Mirror.LEFT_RIGHT : Mirror.NONE;
    }

    public static Direction orient(int transform, Direction local) {
        return rotation(transform).rotate(mirror(transform).mirror(local));
    }
}
