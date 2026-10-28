package com.leclowndu93150.thaumaturge.content.aura.node;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

final class NodeScatter {
    private NodeScatter() {}

    static BlockPos triangular(BlockPos origin, int reach, RandomSource random) {
        return origin.offset(
                offsetTriangular(reach, random), offsetTriangular(reach, random), offsetTriangular(reach, random));
    }

    private static int offsetTriangular(int reach, RandomSource random) {
        return random.nextInt(reach + 1) - random.nextInt(reach + 1);
    }
}
