package com.leclowndu93150.thaumaturge.content.device;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class BlockInlayNetworkTest {
    @Test
    void networkRefreshDoesNotReadNeighborBlocksFromUnloadedChunks() {
        Level level = mock(Level.class);
        BlockState inlay = TTBlocks.INLAY.get().defaultBlockState();
        when(level.hasChunk(0, 0)).thenReturn(true);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(inlay);

        BlockInlay.updateNetwork(level, BlockPos.ZERO);

        verify(level, never()).getBlockState(argThat(pos -> pos.getX() < 0 || pos.getZ() < 0));
    }
}
