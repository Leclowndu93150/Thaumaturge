package com.leclowndu93150.thaumaturge.client.warding;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public final class WardConnectedTexture {
    public static final int CORNERS = 4;
    public static final int STATES = 5;

    private static final int STATE_NONE = 0;
    private static final int STATE_EDGE_A = 1;
    private static final int STATE_EDGE_B = 2;
    private static final int STATE_INNER = 3;
    private static final int STATE_FULL = 4;

    private static final String[] CORNER_NAMES = {"tl", "tr", "bl", "br"};
    private static final String[][] STATE_NAMES = {{"none", "top", "left", "inner", "full"}, {"none", "top", "right", "inner", "full"}, {"none", "bottom", "left", "inner", "full"},
            {"none", "bottom", "right", "inner", "full"}};

    private static final int[] EDGE_A_BIT = {1, 1, 6, 6};
    private static final int[] EDGE_B_BIT = {3, 4, 3, 4};
    private static final int[] DIAGONAL_BIT = {0, 2, 5, 7};

    private static final int[] COLUMN = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final int[] ROW = {-1, -1, -1, 0, 0, 1, 1, 1};

    private static final Identifier[][] SPRITES = new Identifier[CORNERS][STATES];

    static {
        for (int corner = 0; corner < CORNERS; corner++) {
            for (int state = 0; state < STATES; state++) {
                SPRITES[corner][state] = TCIds.rl("block/ward/" + CORNER_NAMES[corner] + "_" + STATE_NAMES[corner][state]);
            }
        }
    }

    private WardConnectedTexture() {}

    public static Identifier sprite(int corner, int state) {
        return SPRITES[corner][state];
    }

    public static int stateFor(BlockPos pos, Direction face, int corner, BlockPos.MutableBlockPos cursor, Predicate<BlockPos> connected) {
        boolean edgeA = probe(pos, face, EDGE_A_BIT[corner], cursor, connected);
        boolean edgeB = probe(pos, face, EDGE_B_BIT[corner], cursor, connected);
        if (!edgeA && !edgeB) {
            return STATE_NONE;
        }
        if (edgeA && !edgeB) {
            return STATE_EDGE_A;
        }
        if (!edgeA) {
            return STATE_EDGE_B;
        }
        return probe(pos, face, DIAGONAL_BIT[corner], cursor, connected) ? STATE_FULL : STATE_INNER;
    }

    private static boolean probe(BlockPos pos, Direction face, int bit, BlockPos.MutableBlockPos cursor, Predicate<BlockPos> connected) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        switch (face.getAxis()) {
            case Y -> cursor.set(x + COLUMN[bit], y, z + ROW[bit]);
            case Z -> {
                int near = face == Direction.NORTH ? -1 : 1;
                cursor.set(x + COLUMN[bit] * near, y - ROW[bit], z);
            }
            case X -> {
                int near = face == Direction.WEST ? 1 : -1;
                cursor.set(x, y - ROW[bit], z + COLUMN[bit] * near);
            }
        }
        return connected.test(cursor);
    }
}
