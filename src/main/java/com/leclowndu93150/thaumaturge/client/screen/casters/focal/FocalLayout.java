package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

public record FocalLayout(int width, int height, int panelX0, int panelY0, int panelX1, int panelY1, int innerX0, int innerY0, int innerX1, int innerY1, int bodyY0, int bodyY1, int leftX0,
        int canvasX0, int canvasX1, int rightX0, int rightX1, int gridY0) {
    public static final int MARGIN = 4;
    public static final int CABINET_X = MARGIN;
    public static final int CABINET_Y = MARGIN - 2;
    public static final int PANEL_LEFT = MARGIN + 72;
    public static final int FRAME_OUTSET = 4;
    public static final int INSET = 14;
    public static final int HEADER_H = 24;
    public static final int LEFT_W = 108;
    public static final int RIGHT_W = 124;
    public static final int COLUMN_GAP = 6;

    public static final int NAME_X = 22;
    public static final int NAME_Y = 2;
    public static final int NAME_H = 14;
    public static final int NAME_RIGHT_GAP = 30;
    public static final int NAME_TEXT_X = 5;
    public static final int NAME_TEXT_Y = 3;
    public static final int CONFIRM_W = 24;
    public static final int CONFIRM_H = 16;
    public static final int CONFIRM_Y = 1;
    public static final int CHECK_X = 7;
    public static final int CHECK_Y = 3;

    public static final int TAB_PITCH = 27;
    public static final int TAB_ICON_X = 7;
    public static final int TAB_ICON_Y = 4;
    public static final int SEARCH_Y = 23;
    public static final int SEARCH_H = 14;
    public static final int SEARCH_TEXT_X = 5;
    public static final int SEARCH_TEXT_Y = 3;
    public static final int GRID_Y = 41;
    public static final int GRID_W = 100;
    public static final int SCROLL_X = 102;
    public static final int SCROLL_W = 6;
    public static final int GRID_PAD = 2;
    public static final int GRID_PITCH = 24;
    public static final int GRID_COLUMNS = 4;
    public static final int PALETTE_BACK = 20;
    public static final int PALETTE_GLYPH = 13;

    public static final int CANVAS_CLIP = 3;
    public static final int ROOT_Y = 24;
    public static final int COLUMN_PITCH = 24;
    public static final int ROW_PITCH = 32;
    public static final int NODE_BACK = 29;
    public static final int NODE_GLYPH = 16;
    public static final int FLOW_GLYPH = 32;
    public static final int ZOOM_BUTTON = 12;
    public static final int ZOOM_MINUS_RIGHT = 31;
    public static final int ZOOM_PLUS_RIGHT = 17;
    public static final int ZOOM_Y = 5;
    public static final int ZOOM_GLYPH = 2;
    public static final int ZOOM_TEXT_RIGHT = 35;
    public static final int ZOOM_TEXT_Y = 7;

    public static FocalLayout of(int width, int height) {
        int panelX0 = PANEL_LEFT;
        int panelY0 = MARGIN;
        int panelX1 = width - MARGIN;
        int panelY1 = height - MARGIN;
        int innerX0 = panelX0 + INSET;
        int innerY0 = panelY0 + INSET;
        int innerX1 = panelX1 - INSET;
        int innerY1 = panelY1 - INSET;
        int bodyY0 = innerY0 + HEADER_H;
        int rightX0 = innerX1 - RIGHT_W;
        int canvasX0 = innerX0 + LEFT_W + COLUMN_GAP;
        int canvasX1 = rightX0 - COLUMN_GAP;
        return new FocalLayout(width, height, panelX0, panelY0, panelX1, panelY1, innerX0, innerY0, innerX1, innerY1, bodyY0, innerY1, innerX0, canvasX0, canvasX1, rightX0, innerX1, bodyY0 + GRID_Y);
    }

    public int nameX0() {
        return innerX0 + NAME_X;
    }

    public int nameX1() {
        return innerX1 - NAME_RIGHT_GAP;
    }

    public int nameY() {
        return innerY0 + NAME_Y;
    }

    public int confirmX() {
        return innerX1 - CONFIRM_W;
    }

    public int confirmY() {
        return innerY0 + CONFIRM_Y;
    }

    public int gridHeight() {
        return bodyY1 - gridY0;
    }

    public int gridRows() {
        return Math.max(1, (gridHeight() - GRID_PAD * 2) / GRID_PITCH);
    }

    public int canvasWidth() {
        return canvasX1 - canvasX0;
    }

    public int canvasHeight() {
        return bodyY1 - bodyY0;
    }

    public boolean inCanvas(double x, double y) {
        return x >= canvasX0 + CANVAS_CLIP && x < canvasX1 - CANVAS_CLIP && y >= bodyY0 + CANVAS_CLIP && y < bodyY1 - CANVAS_CLIP;
    }
}
