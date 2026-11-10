package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import org.junit.jupiter.api.Test;

class FluxGooChunkBoundaryTest {
    @Test
    void unloadedBelowAndSidesAreCheckedBeforeAnyNeighborRead() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        BlockPos origin = new BlockPos(0, 64, 0);
        when(level.getMinBuildHeight()).thenReturn(0);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);

        invokeSpread(goo, level, origin, FluxGooFluid.gooBlockState(4).getFluidState());

        verify(level, times(3)).hasChunk(0, 0);
        verify(level).hasChunk(0, -1);
        verify(level).hasChunk(-1, 0);
        verify(level, never()).getBlockState(any(BlockPos.class));
        verify(level, never()).getFluidState(any(BlockPos.class));
        verify(level, never()).setBlock(any(BlockPos.class), any(), anyInt());
    }

    @Test
    void unloadedBelowDoesNotPreventSpreadIntoLoadedSide() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos origin = new BlockPos(0, 64, 0);
        BlockPos below = origin.below();
        BlockPos north = origin.north();
        FluidState twoQuanta = FluxGooFluid.gooBlockState(2).getFluidState();
        when(level.getMinBuildHeight()).thenReturn(0);
        when(level.getRandom()).thenReturn(random);
        when(random.nextInt(2)).thenReturn(1);
        when(level.hasChunk(0, 0)).thenReturn(false);
        when(level.hasChunk(0, -1)).thenReturn(true);
        when(level.getBlockState(origin)).thenReturn(FluxGooFluid.gooBlockState(4));
        when(level.getBlockState(north)).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getFluidState(origin)).thenReturn(twoQuanta);
        when(level.getFluidState(north)).thenReturn(twoQuanta);

        invokeSpread(goo, level, origin, FluxGooFluid.gooBlockState(4).getFluidState());

        verify(level, never()).getBlockState(below);
        verify(level, never()).getFluidState(below);
        verify(level, never()).getBlockState(origin.south());
        verify(level, never()).getBlockState(origin.west());
        verify(level, never()).getBlockState(origin.east());
        verify(level).setBlock(origin, FluxGooFluid.gooBlockState(2), Block.UPDATE_CLIENTS);
        verify(level).setBlock(north, FluxGooFluid.gooBlockState(2), Block.UPDATE_ALL);
    }

    private static void invokeSpread(FluxGooFluid goo, ServerLevel level, BlockPos pos, FluidState state)
            throws Exception {
        Method spread =
                FluxGooFluid.class.getDeclaredMethod("spreadTick", ServerLevel.class, BlockPos.class, FluidState.class);
        spread.setAccessible(true);
        spread.invoke(goo, level, pos, state);
    }
}
