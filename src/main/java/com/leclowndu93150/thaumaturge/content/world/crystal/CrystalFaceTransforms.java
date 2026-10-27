package com.leclowndu93150.thaumaturge.content.world.crystal;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

public final class CrystalFaceTransforms {
    private static final long FACE_SEED_STRIDE = 0x9E3779B97F4A7C15L;
    private static final Map<Direction, Matrix4f> TRANSFORMS = build();

    private CrystalFaceTransforms() {}

    public static Matrix4f forFace(Direction face) {
        return TRANSFORMS.get(face);
    }

    public static long seedOffset(Direction face) {
        return (face.ordinal() + 1) * FACE_SEED_STRIDE;
    }

    private static Map<Direction, Matrix4f> build() {
        Map<Direction, Matrix4f> transforms = new EnumMap<>(Direction.class);
        transforms.put(Direction.UP, rows(1, 0, 0, 0, 0, -1, 0, 1, 0, 0, -1, 1));
        transforms.put(Direction.DOWN, rows(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0));
        transforms.put(Direction.EAST, rows(0, -1, 0, 1, 0, 0, -1, 1, 1, 0, 0, 0));
        transforms.put(Direction.WEST, rows(0, 1, 0, 0, 0, 0, -1, 1, -1, 0, 0, 1));
        transforms.put(Direction.NORTH, rows(1, 0, 0, 0, 0, 0, -1, 1, 0, 1, 0, 0));
        transforms.put(Direction.SOUTH, rows(-1, 0, 0, 1, 0, 0, -1, 1, 0, -1, 0, 1));
        return transforms;
    }

    private static Matrix4f rows(float... values) {
        Matrix4f matrix = new Matrix4f();
        for (int index = 0; index < values.length; index++) {
            matrix.setRowColumn(index / 4, index % 4, values[index]);
        }
        return matrix;
    }
}
