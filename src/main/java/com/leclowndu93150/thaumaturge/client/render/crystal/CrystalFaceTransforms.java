package com.leclowndu93150.thaumaturge.client.render.crystal;

import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CrystalFaceTransforms {
    private static final Matrix4f UP = applyRotation(new Matrix4f(), (float) Math.PI, new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(0.0F, 1.0F, 1.0F));
    private static final Matrix4f DOWN = new Matrix4f();
    private static final Matrix4f EAST = applyRotation(applyRotation(new Matrix4f(), (float) (Math.PI * 0.5), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F)), (float) (Math.PI * 1.5),
            new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F));
    private static final Matrix4f WEST = applyRotation(applyRotation(new Matrix4f(), (float) (Math.PI * 0.5), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F)), (float) (Math.PI * 0.5),
            new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f(0.0F, 1.0F, 1.0F));
    private static final Matrix4f NORTH = applyRotation(new Matrix4f(), (float) (Math.PI * 0.5), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(0.0F, 1.0F, 0.0F));
    private static final Matrix4f SOUTH = applyRotation(applyRotation(new Matrix4f(), (float) (Math.PI * 0.5), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F)), (float) Math.PI,
            new Vector3f(0.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 1.0F));

    private CrystalFaceTransforms() {}

    public static Matrix4f forFace(Direction direction) {
        return switch (direction) {
            case UP -> UP;
            case DOWN -> DOWN;
            case EAST -> EAST;
            case WEST -> WEST;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
        };
    }

    public static long seedOffset(Direction direction) {
        return switch (direction) {
            case UP -> 0L;
            case DOWN -> 5L;
            case EAST -> 10L;
            case WEST -> 15L;
            case NORTH -> 20L;
            case SOUTH -> 25L;
        };
    }

    private static Matrix4f applyRotation(Matrix4f base, float angle, Vector3f axis, Vector3f offset) {
        Matrix4f result = new Matrix4f();
        result.translate(offset.x, offset.y, offset.z);
        result.rotate(angle, axis.x, axis.y, axis.z);
        result.mul(base);
        return result;
    }
}
