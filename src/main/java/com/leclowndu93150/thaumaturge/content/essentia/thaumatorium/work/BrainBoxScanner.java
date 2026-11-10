package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work;

import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BrainBoxScanner {
    private final NeighbourRing ring = new NeighbourRing();

    public int count(ServerLevel level, BlockPos origin, Direction front) {
        int found = 0;
        for (int slot = 0; slot < ring.slots(); slot++) {
            if (ring.aim(origin, front, slot)
                    && LoadedChunks.contains(level, ring.pos())
                    && isBrainBoxFacing(level.getBlockState(ring.pos()), ring.side())) {
                found++;
            }
        }
        return found;
    }

    private static boolean isBrainBoxFacing(BlockState state, Direction fromMachine) {
        return state.is(TTBlocks.BRAIN_BOX) && state.getValue(BlockStateProperties.FACING) == fromMachine.getOpposite();
    }
}
