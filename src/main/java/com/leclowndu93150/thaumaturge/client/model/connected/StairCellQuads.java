package com.leclowndu93150.thaumaturge.client.model.connected;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public final class StairCellQuads {
    public static final int FRAME_TOP = 1;
    public static final int FRAME_BOTTOM = 2;
    public static final int FRAME_LEFT = 4;
    public static final int FRAME_RIGHT = 8;
    public static final int NOTCH_TOP_LEFT = 1;
    public static final int NOTCH_TOP_RIGHT = 2;
    public static final int NOTCH_BOTTOM_LEFT = 4;
    public static final int NOTCH_BOTTOM_RIGHT = 8;
    public static final int DEPTHS = 2;
    public static final int CELLS = 4;

    private static final int CELL = 8;
    private static final int BANDS = 3;
    private static final int REGIONS = BANDS * BANDS;
    private static final int FIELD = 0;
    private static final int VERTICAL_STRIP = 1;
    private static final int HORIZONTAL_STRIP = 2;
    private static final int CONVEX = 3;
    private static final int NOTCH = 4;
    private static final int VARIANTS = 5;
    private static final float KIT_SCALE = ConnectedQuadBaker.FACE_SIZE / 32.0F;
    private static final int TOP_ROW = 16;
    private static final int BOTTOM_ROW = 20;
    private static final int LEFT_COLUMN = 16;
    private static final int RIGHT_COLUMN = 20;
    private static final int CORNER_COLUMN = 24;
    private static final int CONVEX_ROW = 0;
    private static final int NOTCH_ROW = 8;
    private static final int SLOT = 4;

    private final Direction face;
    private final @Nullable BakedQuad[][][][] regions;
    private final BakedQuad[][] cells;
    private final BakedQuad[] faces;

    private StairCellQuads(
            Direction face, @Nullable BakedQuad[][][][] regions, BakedQuad[][] cells, BakedQuad[] faces) {
        this.face = face;
        this.regions = regions;
        this.cells = cells;
        this.faces = faces;
    }

    public static StairCellQuads bake(ConnectedBakeContext baker, FrameKit kit, Direction face) {
        TextureAtlasSprite material = ConnectedQuadBaker.material(baker, kit.texture());
        BakedQuad[][][][] regions = new BakedQuad[DEPTHS][CELLS][REGIONS][VARIANTS];
        BakedQuad[][] cells = new BakedQuad[DEPTHS][CELLS];
        BakedQuad[] faces = new BakedQuad[DEPTHS];
        for (int depth = 0; depth < DEPTHS; depth++) {
            float inset = depth * CELL;
            faces[depth] = quad(
                    baker,
                    material,
                    face,
                    inset,
                    0,
                    0,
                    ConnectedQuadBaker.FACE_SIZE,
                    ConnectedQuadBaker.FACE_SIZE,
                    0,
                    0,
                    ConnectedQuadBaker.FACE_SIZE,
                    ConnectedQuadBaker.FACE_SIZE);

            for (int cell = 0; cell < CELLS; cell++) {
                float u0 = (cell & 1) * CELL;
                float v0 = (cell >> 1) * CELL;
                cells[depth][cell] =
                        quad(baker, material, face, inset, u0, v0, u0 + CELL, v0 + CELL, u0, v0, u0 + CELL, v0 + CELL);
                float[] us = {u0, u0 + kit.left(), u0 + CELL - kit.right(), u0 + CELL};
                float[] vs = {v0, v0 + kit.top(), v0 + CELL - kit.bottom(), v0 + CELL};
                for (int row = 0; row < BANDS; row++) {
                    for (int column = 0; column < BANDS; column++) {
                        if (us[column + 1] <= us[column] || vs[row + 1] <= vs[row]) {
                            continue;
                        }
                        BakedQuad[] variants = regions[depth][cell][row * BANDS + column];
                        for (int variant = 0; variant < VARIANTS; variant++) {
                            float[] uv = kitRect(
                                    kit, variant, row, column, us[column], vs[row], us[column + 1], vs[row + 1]);
                            if (uv != null) {
                                variants[variant] = quad(
                                        baker,
                                        material,
                                        face,
                                        inset,
                                        us[column],
                                        vs[row],
                                        us[column + 1],
                                        vs[row + 1],
                                        uv[0],
                                        uv[1],
                                        uv[2],
                                        uv[3]);
                            }
                        }
                    }
                }
            }
        }
        return new StairCellQuads(face, regions, cells, faces);
    }

    public void addFace(int depth, ConnectedQuads.Builder builder) {
        add(depth, faces[depth], builder);
    }

    public void addCell(int depth, int cell, int frames, int notches, ConnectedQuads.Builder builder) {
        if (frames == 0 && notches == 0) {
            add(depth, cells[depth][cell], builder);
            return;
        }
        for (int row = 0; row < BANDS; row++) {
            for (int column = 0; column < BANDS; column++) {
                BakedQuad quad = regions[depth][cell][row * BANDS + column][variant(row, column, frames, notches)];
                if (quad != null) {
                    add(depth, quad, builder);
                }
            }
        }
    }

    private void add(int depth, BakedQuad quad, ConnectedQuads.Builder builder) {
        if (depth == 0) {
            builder.addCulledFace(face, quad);
        } else {
            builder.addUnculledFace(quad);
        }
    }

    private static int variant(int row, int column, int frames, int notches) {
        boolean vertical = row == 0 ? (frames & FRAME_TOP) != 0 : row == BANDS - 1 && (frames & FRAME_BOTTOM) != 0;
        boolean horizontal =
                column == 0 ? (frames & FRAME_LEFT) != 0 : column == BANDS - 1 && (frames & FRAME_RIGHT) != 0;
        if (vertical && horizontal) {
            return CONVEX;
        }
        if (vertical) {
            return VERTICAL_STRIP;
        }
        if (horizontal) {
            return HORIZONTAL_STRIP;
        }
        if (row != 1 && column != 1 && (notches & notchBit(row, column)) != 0) {
            return NOTCH;
        }
        return FIELD;
    }

    private static int notchBit(int row, int column) {
        if (row == 0) {
            return column == 0 ? NOTCH_TOP_LEFT : NOTCH_TOP_RIGHT;
        }
        return column == 0 ? NOTCH_BOTTOM_LEFT : NOTCH_BOTTOM_RIGHT;
    }

    private static float @Nullable [] kitRect(
            FrameKit kit, int variant, int row, int column, float u0, float v0, float u1, float v1) {
        boolean edgeRow = row != 1;
        boolean edgeColumn = column != 1;
        return switch (variant) {
            case FIELD -> new float[] {u0, v0, u1, v1};
            case VERTICAL_STRIP ->
                edgeRow ? rowStrip(row == 0 ? TOP_ROW : BOTTOM_ROW, row == 0 ? kit.top() : kit.bottom(), u0, u1) : null;
            case HORIZONTAL_STRIP ->
                edgeColumn
                        ? columnStrip(
                                column == 0 ? LEFT_COLUMN : RIGHT_COLUMN,
                                column == 0 ? kit.left() : kit.right(),
                                v0,
                                v1)
                        : null;
            case CONVEX -> edgeRow && edgeColumn ? corner(CONVEX_ROW, row, column, u1 - u0, v1 - v0) : null;
            case NOTCH -> edgeRow && edgeColumn ? corner(NOTCH_ROW, row, column, u1 - u0, v1 - v0) : null;
            default -> null;
        };
    }

    private static float[] rowStrip(int row, int height, float u0, float u1) {
        return new float[] {u0, row, u1, row + height};
    }

    private static float[] columnStrip(int column, int width, float v0, float v1) {
        return new float[] {column, v0, column + width, v1};
    }

    private static float[] corner(int baseRow, int row, int column, float width, float height) {
        float u = CORNER_COLUMN + (column == 0 ? 0 : SLOT);
        float v = baseRow + (row == 0 ? 0 : SLOT);
        return new float[] {u, v, u + width, v + height};
    }

    private static BakedQuad quad(
            ConnectedBakeContext baker,
            TextureAtlasSprite material,
            Direction face,
            float inset,
            float minU,
            float minV,
            float maxU,
            float maxV,
            float kitU0,
            float kitV0,
            float kitU1,
            float kitV1) {
        ConnectedQuadBaker.Uvs uvs =
                new ConnectedQuadBaker.Uvs(kitU0 * KIT_SCALE, kitV0 * KIT_SCALE, kitU1 * KIT_SCALE, kitV1 * KIT_SCALE);
        return ConnectedQuadBaker.bake(baker, material, face, inset, minU, minV, maxU, maxV, uvs);
    }
}
