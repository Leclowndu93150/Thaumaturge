package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class BlockFluxGasSpreadTest {
    @Test
    void sidewaysSpreadSkipsUnloadedNeighborsBeforeReadingTheirStates() throws Exception {
        BlockFluxGas gas = (BlockFluxGas) BlockFluxGas.gasBlockState(1).getBlock();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        when(level.hasChunkAt(any(BlockPos.class))).thenReturn(false);

        invokeSpread(gas, level, BlockPos.ZERO, 4, random);

        verify(level).hasChunkAt(BlockPos.ZERO.north());
        verify(level).hasChunkAt(BlockPos.ZERO.south());
        verify(level).hasChunkAt(BlockPos.ZERO.west());
        verify(level).hasChunkAt(BlockPos.ZERO.east());
        verify(level, never()).getBlockState(any(BlockPos.class));
        verify(level, never()).setBlock(any(BlockPos.class), any(), anyInt());
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    void loadedPositiveNeighborStillParticipatesInTheExistingSpread() throws Exception {
        BlockFluxGas gas = (BlockFluxGas) BlockFluxGas.gasBlockState(1).getBlock();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos target = BlockPos.ZERO.north();
        when(level.hasChunkAt(target)).thenReturn(true);
        when(level.getBlockState(target)).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(Blocks.AIR.defaultBlockState());

        invokeSpread(gas, level, BlockPos.ZERO, 4, random);

        verify(level, times(2)).getBlockState(target);
        verify(level, never()).getBlockState(BlockPos.ZERO.south());
        verify(level, never()).getBlockState(BlockPos.ZERO.west());
        verify(level, never()).getBlockState(BlockPos.ZERO.east());
        verify(level).setBlock(BlockPos.ZERO, BlockFluxGas.gasBlockState(2), Block.UPDATE_ALL);
        verify(level).setBlock(target, BlockFluxGas.gasBlockState(2), Block.UPDATE_ALL);
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    void sidewaysSpreadDisplacesOrdinaryLiquidNeighbors() throws Exception {
        BlockFluxGas gas = (BlockFluxGas) BlockFluxGas.gasBlockState(1).getBlock();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos target = BlockPos.ZERO.north();
        when(level.hasChunkAt(target)).thenReturn(true);
        when(level.getBlockState(target)).thenReturn(Blocks.WATER.defaultBlockState());
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(BlockFluxGas.gasBlockState(8));

        invokeSpread(gas, level, BlockPos.ZERO, 8, random);

        verify(level, times(2)).getBlockState(target);
        verify(level).setBlock(BlockPos.ZERO, BlockFluxGas.gasBlockState(4), Block.UPDATE_ALL);
        verify(level, never()).destroyBlock(any(BlockPos.class), anyBoolean());
        verify(level).setBlock(target, BlockFluxGas.gasBlockState(4), Block.UPDATE_ALL);
        verify(random, never()).nextInt(anyInt());
    }

    @Test
    void verticalFlowStillSwapsWithAnOrdinaryLiquidAbove() throws Exception {
        BlockFluxGas gas = (BlockFluxGas) BlockFluxGas.gasBlockState(1).getBlock();
        ServerLevel level = mock(ServerLevel.class);
        BlockPos above = BlockPos.ZERO.above();
        BlockState waterState = Blocks.WATER.defaultBlockState();
        when(level.getMaxBuildHeight()).thenReturn(320);
        when(level.getBlockState(above)).thenReturn(waterState);

        assertEquals(0, invokeFlowUp(gas, level, BlockPos.ZERO, 4));

        verify(level).setBlock(above, BlockFluxGas.gasBlockState(4), Block.UPDATE_ALL);
        verify(level).setBlock(BlockPos.ZERO, waterState, Block.UPDATE_ALL);
    }

    @Test
    void existingGasNeighborStillParticipatesInSidewaysSpread() throws Exception {
        BlockFluxGas gas = (BlockFluxGas) BlockFluxGas.gasBlockState(1).getBlock();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos target = BlockPos.ZERO.north();
        when(level.hasChunkAt(target)).thenReturn(true);
        when(level.getBlockState(target)).thenReturn(BlockFluxGas.gasBlockState(1));
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(BlockFluxGas.gasBlockState(4));

        invokeSpread(gas, level, BlockPos.ZERO, 4, random);

        verify(level).setBlock(BlockPos.ZERO, BlockFluxGas.gasBlockState(3), Block.UPDATE_ALL);
        verify(level).setBlock(target, BlockFluxGas.gasBlockState(2), Block.UPDATE_ALL);
    }

    @Test
    void remainderBonusUsesSourceRandomChoiceWithoutShufflingCandidates() throws Exception {
        BlockFluxGas gas = (BlockFluxGas) BlockFluxGas.gasBlockState(1).getBlock();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos target = BlockPos.ZERO.north();
        when(level.hasChunkAt(target)).thenReturn(true);
        when(level.getBlockState(target)).thenReturn(BlockFluxGas.gasBlockState(1));
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(BlockFluxGas.gasBlockState(4));
        when(random.nextInt(2)).thenReturn(1);

        invokeSpread(gas, level, BlockPos.ZERO, 4, random);

        verify(random).nextInt(2);
        verify(random, never()).nextInt(3);
        verify(level).setBlock(BlockPos.ZERO, BlockFluxGas.gasBlockState(2), Block.UPDATE_ALL);
        verify(level).setBlock(target, BlockFluxGas.gasBlockState(3), Block.UPDATE_ALL);
    }

    @Test
    void repeatedBonusRollsKeepDrawingUntilEachRemainderIsAssignedOnce() throws Exception {
        BlockFluxGas gas = (BlockFluxGas) BlockFluxGas.gasBlockState(1).getBlock();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos north = BlockPos.ZERO.north();
        BlockPos south = BlockPos.ZERO.south();
        when(level.hasChunkAt(north)).thenReturn(true);
        when(level.hasChunkAt(south)).thenReturn(true);
        when(level.getBlockState(north)).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getBlockState(south)).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(BlockFluxGas.gasBlockState(5));
        when(random.nextInt(3)).thenReturn(1, 1, 2);

        invokeSpread(gas, level, BlockPos.ZERO, 5, random);

        verify(random, times(3)).nextInt(3);
        verify(level).setBlock(BlockPos.ZERO, BlockFluxGas.gasBlockState(1), Block.UPDATE_ALL);
        verify(level).setBlock(north, BlockFluxGas.gasBlockState(2), Block.UPDATE_ALL);
        verify(level).setBlock(south, BlockFluxGas.gasBlockState(2), Block.UPDATE_ALL);
    }

    private static void invokeSpread(BlockFluxGas gas, ServerLevel level, BlockPos pos, int amount, RandomSource random)
            throws Exception {
        Method spread = BlockFluxGas.class.getDeclaredMethod(
                "spreadSideways", ServerLevel.class, BlockPos.class, int.class, RandomSource.class);
        spread.setAccessible(true);
        spread.invoke(gas, level, pos, amount, random);
    }

    private static int invokeFlowUp(BlockFluxGas gas, ServerLevel level, BlockPos pos, int amount) throws Exception {
        Method flowUp = BlockFluxGas.class.getDeclaredMethod("flowUp", ServerLevel.class, BlockPos.class, int.class);
        flowUp.setAccessible(true);
        return (int) flowUp.invoke(gas, level, pos, amount);
    }
}
