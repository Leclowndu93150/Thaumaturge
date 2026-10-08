package com.leclowndu93150.thaumaturge.client.model.connected;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public final class TileFaceQuads implements ConnectedFaceQuads {
    public static final int TILE_COUNT = 47;

    private static final int EDGE_TOP = 1;
    private static final int EDGE_RIGHT = 2;
    private static final int EDGE_BOTTOM = 4;
    private static final int EDGE_LEFT = 8;
    private static final int NOTCH_TOP_LEFT = 1;
    private static final int NOTCH_TOP_RIGHT = 2;
    private static final int NOTCH_BOTTOM_RIGHT = 4;
    private static final int NOTCH_BOTTOM_LEFT = 8;
    private static final int ALL_EDGES = EDGE_TOP | EDGE_RIGHT | EDGE_BOTTOM | EDGE_LEFT;
    private static final int ALL_NOTCHES = NOTCH_TOP_LEFT | NOTCH_TOP_RIGHT | NOTCH_BOTTOM_RIGHT | NOTCH_BOTTOM_LEFT;
    private static final int NOTCH_SHIFT = 4;
    private static final int QUARTER_TURNS = 4;
    private static final ConnectedQuadBaker.Uvs FULL_UVS =
            new ConnectedQuadBaker.Uvs(0.0F, 0.0F, ConnectedQuadBaker.FACE_SIZE, ConnectedQuadBaker.FACE_SIZE);
    private static final Direction[] FACES = Direction.values();
    private static final int[] TILE_BY_CONNECTIONS = buildConnectionTable();

    private final BakedQuad[][] quads;

    private TileFaceQuads(BakedQuad[][] quads) {
        this.quads = quads;
    }

    public static TileFaceQuads bake(ConnectedBakeContext baker, List<TextureAtlasSprite> tiles) {
        BakedQuad[][] quads = new BakedQuad[FACES.length][TILE_COUNT];
        for (int tile = 0; tile < TILE_COUNT; tile++) {
            for (Direction face : FACES) {
                BakedQuad quad = ConnectedQuadBaker.bakeWhole(baker, tiles.get(tile), face, 0.0F, FULL_UVS);
                quads[face.ordinal()][tile] = quad;
            }
        }
        return new TileFaceQuads(quads);
    }

    @Override
    public boolean connects(Direction face) {
        return true;
    }

    @Override
    public void addFace(Direction face, int connections, ConnectedQuads.Builder builder) {
        builder.addCulledFace(face, quads[face.ordinal()][TILE_BY_CONNECTIONS[connections]]);
    }

    private static int[] buildConnectionTable() {
        int[] tiles = buildPatternTable();
        int[] table = new int[FaceConnections.MASKS];
        for (int connections = 0; connections < FaceConnections.MASKS; connections++) {
            table[connections] = tiles[pattern(connections)];
        }
        return table;
    }

    private static int pattern(int connections) {
        boolean top = FaceConnections.has(connections, FaceConnections.TOP);
        boolean right = FaceConnections.has(connections, FaceConnections.RIGHT);
        boolean bottom = FaceConnections.has(connections, FaceConnections.BOTTOM);
        boolean left = FaceConnections.has(connections, FaceConnections.LEFT);
        int edges =
                (top ? 0 : EDGE_TOP) | (right ? 0 : EDGE_RIGHT) | (bottom ? 0 : EDGE_BOTTOM) | (left ? 0 : EDGE_LEFT);
        int notches = 0;
        if (top && left && !FaceConnections.has(connections, FaceConnections.TOP_LEFT)) {
            notches |= NOTCH_TOP_LEFT;
        }
        if (top && right && !FaceConnections.has(connections, FaceConnections.TOP_RIGHT)) {
            notches |= NOTCH_TOP_RIGHT;
        }
        if (bottom && right && !FaceConnections.has(connections, FaceConnections.BOTTOM_RIGHT)) {
            notches |= NOTCH_BOTTOM_RIGHT;
        }
        if (bottom && left && !FaceConnections.has(connections, FaceConnections.BOTTOM_LEFT)) {
            notches |= NOTCH_BOTTOM_LEFT;
        }
        return edges | notches << NOTCH_SHIFT;
    }

    private static int[] buildPatternTable() {
        int[] tiles = new int[1 << (2 * NOTCH_SHIFT)];
        int[][] files = {
            {ALL_EDGES, 0},
            {EDGE_TOP | EDGE_BOTTOM | EDGE_LEFT, 0},
            {EDGE_TOP | EDGE_BOTTOM, 0},
            {EDGE_TOP | EDGE_BOTTOM | EDGE_RIGHT, 0},
            {EDGE_TOP | EDGE_LEFT | EDGE_RIGHT, 0},
            {EDGE_LEFT | EDGE_RIGHT, 0},
            {EDGE_BOTTOM | EDGE_LEFT | EDGE_RIGHT, 0},
            {EDGE_TOP | EDGE_LEFT, 0},
            {EDGE_TOP, 0},
            {EDGE_TOP | EDGE_RIGHT, 0},
            {EDGE_LEFT, 0},
            {0, 0},
            {EDGE_RIGHT, 0},
            {EDGE_BOTTOM | EDGE_LEFT, 0},
            {EDGE_BOTTOM, 0},
            {EDGE_BOTTOM | EDGE_RIGHT, 0},
            {EDGE_TOP | EDGE_LEFT, NOTCH_BOTTOM_RIGHT},
            {EDGE_TOP | EDGE_RIGHT, NOTCH_BOTTOM_LEFT},
            {EDGE_BOTTOM | EDGE_LEFT, NOTCH_TOP_RIGHT},
            {EDGE_BOTTOM | EDGE_RIGHT, NOTCH_TOP_LEFT}
        };
        int next = 0;
        for (int[] file : files) {
            tiles[file[0] | file[1] << NOTCH_SHIFT] = next++;
        }
        for (int[] start : new int[][] {
            {EDGE_LEFT, NOTCH_TOP_RIGHT | NOTCH_BOTTOM_RIGHT},
            {EDGE_LEFT, NOTCH_TOP_RIGHT},
            {EDGE_LEFT, NOTCH_BOTTOM_RIGHT}
        }) {
            for (int turn = 0; turn < QUARTER_TURNS; turn++) {
                tiles[rotate(start[0], turn) | rotate(start[1], turn) << NOTCH_SHIFT] = next++;
            }
        }
        for (int[] start : new int[][] {{NOTCH_BOTTOM_RIGHT}, {NOTCH_TOP_LEFT | NOTCH_TOP_RIGHT}}) {
            for (int turn = 0; turn < QUARTER_TURNS; turn++) {
                tiles[rotate(start[0], turn) << NOTCH_SHIFT] = next++;
            }
        }
        tiles[(NOTCH_TOP_LEFT | NOTCH_BOTTOM_RIGHT) << NOTCH_SHIFT] = next++;
        tiles[(NOTCH_TOP_RIGHT | NOTCH_BOTTOM_LEFT) << NOTCH_SHIFT] = next++;
        for (int turn = 0; turn < QUARTER_TURNS; turn++) {
            tiles[(ALL_NOTCHES & ~rotate(NOTCH_TOP_LEFT, turn)) << NOTCH_SHIFT] = next++;
        }
        tiles[ALL_NOTCHES << NOTCH_SHIFT] = next;
        return tiles;
    }

    private static int rotate(int bits, int turns) {
        int shifted = bits << turns;
        return (shifted | shifted >> QUARTER_TURNS) & ALL_EDGES;
    }
}
