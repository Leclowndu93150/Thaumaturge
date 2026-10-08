package com.leclowndu93150.thaumaturge.client.model.connected;

import net.minecraft.resources.ResourceLocation;

public final class FaceCorners {
    public static final int COUNT = 4;
    public static final int TOP_LEFT = 0;
    public static final int TOP_RIGHT = 1;
    public static final int BOTTOM_LEFT = 2;
    public static final int BOTTOM_RIGHT = 3;
    public static final int STATES = 5;
    public static final int UNCONNECTED = 0;
    public static final int VERTICAL = 1;
    public static final int HORIZONTAL = 2;
    public static final int INNER_CORNER = 3;
    public static final int SURROUNDED = 4;

    private static final int[] VERTICAL_NEIGHBOUR = {
        FaceConnections.TOP, FaceConnections.TOP, FaceConnections.BOTTOM, FaceConnections.BOTTOM
    };
    private static final int[] HORIZONTAL_NEIGHBOUR = {
        FaceConnections.LEFT, FaceConnections.RIGHT, FaceConnections.LEFT, FaceConnections.RIGHT
    };
    private static final int[] DIAGONAL_NEIGHBOUR = {
        FaceConnections.TOP_LEFT, FaceConnections.TOP_RIGHT, FaceConnections.BOTTOM_LEFT, FaceConnections.BOTTOM_RIGHT
    };
    private static final String[] CORNER_NAMES = {"tl", "tr", "bl", "br"};
    private static final String[][] STATE_NAMES = {
        {"none", "top", "left", "inner", "full"},
        {"none", "top", "right", "inner", "full"},
        {"none", "bottom", "left", "inner", "full"},
        {"none", "bottom", "right", "inner", "full"}
    };

    private FaceCorners() {}

    public static int state(int connections, int corner) {
        boolean vertical = FaceConnections.has(connections, VERTICAL_NEIGHBOUR[corner]);
        boolean horizontal = FaceConnections.has(connections, HORIZONTAL_NEIGHBOUR[corner]);
        if (vertical && horizontal) {
            return FaceConnections.has(connections, DIAGONAL_NEIGHBOUR[corner]) ? SURROUNDED : INNER_CORNER;
        }
        if (vertical) {
            return VERTICAL;
        }
        return horizontal ? HORIZONTAL : UNCONNECTED;
    }

    public static int slot(int corner, int state) {
        return corner * STATES + state;
    }

    public static int column(int corner) {
        return corner & 1;
    }

    public static int row(int corner) {
        return corner >> 1;
    }

    public static ResourceLocation[] sprites(ResourceLocation directory) {
        ResourceLocation[] sprites = new ResourceLocation[COUNT * STATES];
        for (int corner = 0; corner < COUNT; corner++) {
            for (int state = 0; state < STATES; state++) {
                sprites[slot(corner, state)] =
                        directory.withSuffix("/" + CORNER_NAMES[corner] + "_" + STATE_NAMES[corner][state]);
            }
        }
        return sprites;
    }
}
