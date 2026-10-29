package com.leclowndu93150.thaumaturge.client.hud;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

final class AspectReadoutPlacement {
    private AspectReadoutPlacement() {}

    static Direction visibleSide(Level level, BlockPos pos, Direction preferred) {
        Direction side = preferred.getAxis().isHorizontal() ? preferred : Direction.NORTH;
        for (int i = 0; i < 4; i++) {
            BlockPos neighborPos = pos.relative(side);
            if (level.hasChunkAt(neighborPos)) {
                BlockState neighbor = level.getBlockState(neighborPos);
                if (neighbor.isAir() || !neighbor.isSolidRender(level, neighborPos)) {
                    return side;
                }
            }
            side = side.getClockWise();
        }
        return preferred;
    }
}
