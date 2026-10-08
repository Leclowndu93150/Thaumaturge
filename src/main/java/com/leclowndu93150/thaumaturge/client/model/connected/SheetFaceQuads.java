package com.leclowndu93150.thaumaturge.client.model.connected;

import java.util.Map;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public final class SheetFaceQuads implements ConnectedFaceQuads {
    private static final float HALF = ConnectedQuadBaker.FACE_SIZE / 2.0F;
    private static final float SHEET_TILE = HALF;
    private static final float SHEET_QUARTER = SHEET_TILE / 2.0F;
    private static final int[] SHEET_COLUMN = {0, 1, 0, 1, 0};
    private static final int[] SHEET_ROW = {0, 0, 1, 1, 0};
    private static final Direction[] FACES = Direction.values();

    private final BakedQuad[][] whole;
    private final BakedQuad[][][] corners;
    private final boolean[] culled;

    private SheetFaceQuads(BakedQuad[][] whole, BakedQuad[][][] corners, boolean[] culled) {
        this.whole = whole;
        this.corners = corners;
        this.culled = culled;
    }

    public static SheetFaceQuads bake(
            ConnectedBakeContext baker, Map<Direction, ConnectedTexture> faces, float[] insets) {
        BakedQuad[][] whole = new BakedQuad[FACES.length][];
        BakedQuad[][][] corners = new BakedQuad[FACES.length][][];
        boolean[] culled = new boolean[FACES.length];
        for (Map.Entry<Direction, ConnectedTexture> entry : faces.entrySet()) {
            Direction face = entry.getKey();
            ConnectedTexture spec = entry.getValue();
            TextureAtlasSprite texture = ConnectedQuadBaker.material(baker, spec.texture());
            TextureAtlasSprite sheet = ConnectedQuadBaker.material(baker, spec.sheet());
            TextureAtlasSprite unconnected = ConnectedQuadBaker.material(baker, spec.unconnectedCorner());
            float inset = insets[face.ordinal()];
            BakedQuad[] faceWhole = new BakedQuad[FaceCorners.STATES];
            BakedQuad[][] faceCorners = new BakedQuad[FaceCorners.COUNT][FaceCorners.STATES];
            for (int state = 0; state < FaceCorners.STATES; state++) {
                faceWhole[state] = ConnectedQuadBaker.bakeWhole(
                        baker, state == FaceCorners.UNCONNECTED ? texture : sheet, face, inset, wholeUvs(state));

                for (int corner = 0; corner < FaceCorners.COUNT; corner++) {
                    float minU = HALF * FaceCorners.column(corner);
                    float minV = HALF * FaceCorners.row(corner);
                    faceCorners[corner][state] = ConnectedQuadBaker.bake(
                            baker,
                            state == FaceCorners.UNCONNECTED ? unconnected : sheet,
                            face,
                            inset,
                            minU,
                            minV,
                            minU + HALF,
                            minV + HALF,
                            cornerUvs(state, corner));
                }
            }
            whole[face.ordinal()] = faceWhole;
            corners[face.ordinal()] = faceCorners;
            culled[face.ordinal()] = inset <= 0.0F;
        }
        return new SheetFaceQuads(whole, corners, culled);
    }

    @Override
    public boolean connects(Direction face) {
        return whole[face.ordinal()] != null;
    }

    @Override
    public void addFace(Direction face, int connections, ConnectedQuads.Builder builder) {
        int topLeft = FaceCorners.state(connections, FaceCorners.TOP_LEFT);
        int topRight = FaceCorners.state(connections, FaceCorners.TOP_RIGHT);
        int bottomLeft = FaceCorners.state(connections, FaceCorners.BOTTOM_LEFT);
        int bottomRight = FaceCorners.state(connections, FaceCorners.BOTTOM_RIGHT);
        if (topLeft == topRight && topLeft == bottomLeft && topLeft == bottomRight) {
            add(face, whole[face.ordinal()][topLeft], builder);
            return;
        }
        BakedQuad[][] faceCorners = corners[face.ordinal()];
        add(face, faceCorners[FaceCorners.TOP_LEFT][topLeft], builder);
        add(face, faceCorners[FaceCorners.TOP_RIGHT][topRight], builder);
        add(face, faceCorners[FaceCorners.BOTTOM_LEFT][bottomLeft], builder);
        add(face, faceCorners[FaceCorners.BOTTOM_RIGHT][bottomRight], builder);
    }

    private void add(Direction face, BakedQuad quad, ConnectedQuads.Builder builder) {
        if (culled[face.ordinal()]) {
            builder.addCulledFace(face, quad);
        } else {
            builder.addUnculledFace(quad);
        }
    }

    private static ConnectedQuadBaker.Uvs wholeUvs(int state) {
        if (state == FaceCorners.UNCONNECTED) {
            return new ConnectedQuadBaker.Uvs(0.0F, 0.0F, ConnectedQuadBaker.FACE_SIZE, ConnectedQuadBaker.FACE_SIZE);
        }
        float minU = SHEET_TILE * SHEET_COLUMN[state];
        float minV = SHEET_TILE * SHEET_ROW[state];
        return new ConnectedQuadBaker.Uvs(minU, minV, minU + SHEET_TILE, minV + SHEET_TILE);
    }

    private static ConnectedQuadBaker.Uvs cornerUvs(int state, int corner) {
        if (state == FaceCorners.UNCONNECTED) {
            float minU = HALF * FaceCorners.column(corner);
            float minV = HALF * FaceCorners.row(corner);
            return new ConnectedQuadBaker.Uvs(minU, minV, minU + HALF, minV + HALF);
        }
        float minU = SHEET_TILE * SHEET_COLUMN[state] + SHEET_QUARTER * FaceCorners.column(corner);
        float minV = SHEET_TILE * SHEET_ROW[state] + SHEET_QUARTER * FaceCorners.row(corner);
        return new ConnectedQuadBaker.Uvs(minU, minV, minU + SHEET_QUARTER, minV + SHEET_QUARTER);
    }
}
