package com.leclowndu93150.thaumaturge.content.essentia.reservoir;

import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

final class FluxPocketScatter {
    private static final int SPOT_ATTEMPTS = 50;
    private static final double REACH = 4.0;

    private FluxPocketScatter() {}

    static int scatter(ServerLevel level, BlockPos origin, int pockets) {
        RandomSource random = level.getRandom();
        BlockPos.MutableBlockPos spot = new BlockPos.MutableBlockPos();
        int placed = 0;
        for (int attempt = 0; attempt < SPOT_ATTEMPTS && placed < pockets; attempt++) {
            spot.setWithOffset(origin, clusteredOffset(random), clusteredOffset(random), clusteredOffset(random));
            if (!level.hasChunkAt(spot) || !level.getBlockState(spot).isAir()) {
                continue;
            }
            BlockPos target = spot.immutable();
            boolean filled = target.getY() < origin.getY() ? PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA) : PhysicalFlux.placeGas(level, target, PhysicalFlux.MAX_QUANTA);
            if (filled) {
                placed++;
            }
        }
        return placed;
    }

    private static int clusteredOffset(RandomSource random) {
        return (int) Math.round(random.triangle(0.0, REACH));
    }
}
