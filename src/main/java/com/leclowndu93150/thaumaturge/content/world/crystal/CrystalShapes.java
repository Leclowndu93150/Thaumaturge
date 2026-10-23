package com.leclowndu93150.thaumaturge.content.world.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class CrystalShapes {
    private static final float PIXELS_PER_BLOCK = 16.0F;
    private static final float FOOTPRINT_MIN = 1.0F;
    private static final float FOOTPRINT_MAX = 15.0F;
    private static final float FOOTPRINT_HEIGHT = 9.0F;
    private static final Direction[] FACES = Direction.values();
    private static final int MASKS = 1 << FACES.length;
    private static final VoxelShape[] BY_SUPPORT = buildShapes();

    private CrystalShapes() {}

    static VoxelShape shapeFor(BlockGetter level, BlockPos pos) {
        int mask = 0;
        for (Direction face : FACES) {
            if (CrystalShards.supports(level, pos, face)) {
                mask |= 1 << face.ordinal();
            }
        }
        return BY_SUPPORT[mask];
    }

    private static VoxelShape[] buildShapes() {
        VoxelShape[] faceBoxes = new VoxelShape[FACES.length];
        for (Direction face : FACES) {
            faceBoxes[face.ordinal()] = faceBox(CrystalFaceTransforms.forFace(face));
        }
        VoxelShape[] shapes = new VoxelShape[MASKS];
        shapes[0] = faceBoxes[Direction.DOWN.ordinal()];
        for (int mask = 1; mask < MASKS; mask++) {
            VoxelShape union = Shapes.empty();
            for (Direction face : FACES) {
                if ((mask & 1 << face.ordinal()) != 0) {
                    union = Shapes.joinUnoptimized(union, faceBoxes[face.ordinal()], BooleanOp.OR);
                }
            }
            shapes[mask] = union.optimize();
        }
        return shapes;
    }

    private static VoxelShape faceBox(Matrix4f transform) {
        Vector3f low = transform.transformPosition(scaled(FOOTPRINT_MIN, 0.0F, FOOTPRINT_MIN), new Vector3f());
        Vector3f high =
                transform.transformPosition(scaled(FOOTPRINT_MAX, FOOTPRINT_HEIGHT, FOOTPRINT_MAX), new Vector3f());
        return Shapes.box(
                Math.min(low.x, high.x),
                Math.min(low.y, high.y),
                Math.min(low.z, high.z),
                Math.max(low.x, high.x),
                Math.max(low.y, high.y),
                Math.max(low.z, high.z));
    }

    private static Vector3f scaled(float x, float y, float z) {
        return new Vector3f(x / PIXELS_PER_BLOCK, y / PIXELS_PER_BLOCK, z / PIXELS_PER_BLOCK);
    }
}
