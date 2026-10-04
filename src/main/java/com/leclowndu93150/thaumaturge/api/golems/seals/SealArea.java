package com.leclowndu93150.thaumaturge.api.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * Geometry of a seal's work area. The area starts at the block in front of the seal, runs {@code area} blocks deep along the facing
 * and spreads {@code area - 1} blocks to each side on the other two axes.
 *
 * @since 1.0.0
 */
public final class SealArea {
    private SealArea() {}

    /**
     * Maps a running counter onto the cells of the area, so that a seal stepping the counter by one each scan visits every cell in
     * turn. The Z offset changes fastest, then X, then Y.
     *
     * @param seal  the seal
     * @param index the counter; any int, wrapping is handled
     * @return the cell for this step
     */
    public static BlockPos cell(ISealEntity seal, int index) {
        Direction face = seal.pos().face();
        BlockPos size = seal.area();
        int spanX = span(size.getX(), face.getStepX());
        int spanY = span(size.getY(), face.getStepY());
        int spanZ = span(size.getZ(), face.getStepZ());
        int x = lane(face.getStepX(), index / spanZ, spanX);
        int y = lane(face.getStepY(), index / spanZ / spanX, spanY);
        int z = lane(face.getStepZ(), index, spanZ);
        return seal.pos().pos().offset(x, y, z);
    }

    /**
     * @param seal the seal
     * @return the box covering every cell of the area
     */
    public static AABB bounds(ISealEntity seal) {
        Direction face = seal.pos().face();
        BlockPos size = seal.area();
        return new AABB(seal.pos().pos().relative(face)).expandTowards(depth(face.getStepX(), size.getX()), depth(face.getStepY(), size.getY()), depth(face.getStepZ(), size.getZ()))
                .inflate(reach(face.getStepX(), size.getX()), reach(face.getStepY(), size.getY()), reach(face.getStepZ(), size.getZ()));
    }

    private static int span(int size, int step) {
        return step == 0 ? size * 2 - 1 : size;
    }

    private static int lane(int step, int counter, int span) {
        return step == 0 ? counter % span - span / 2 : step * counter % span + step;
    }

    private static double depth(int step, int size) {
        return step == 0 ? 0.0 : (size - 1) * step;
    }

    private static double reach(int step, int size) {
        return step == 0 ? size - 1 : 0.0;
    }
}
