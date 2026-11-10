package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.junit.jupiter.api.Test;

class FluxGooLevelingPortTest {
    @Test
    void shuffledPartiesReceiveRemainderInSourceOrderAndOnlyChangedStatesAreWritten() throws Exception {
        FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        ServerLevel level = mock(ServerLevel.class);
        RandomSource spreadRandom = mock(RandomSource.class);
        RandomSource worldRandom = mock(RandomSource.class);
        BlockPos origin = new BlockPos(0, 64, 0);
        BlockPos north = origin.north();
        BlockPos east = origin.east();
        BlockPos south = origin.south();
        BlockPos west = origin.west();
        Map<BlockPos, BlockState> states = new HashMap<>();
        states.put(origin, FluxGooFluid.gooBlockState(8));
        states.put(north, FluxGooFluid.gooBlockState(1));
        states.put(east, Blocks.AIR.defaultBlockState());
        states.put(south, Blocks.AIR.defaultBlockState());
        states.put(west, Blocks.AIR.defaultBlockState());
        states.put(origin.below(), Blocks.STONE.defaultBlockState());
        states.put(origin.above(), Blocks.STONE.defaultBlockState());

        when(level.getMinBuildHeight()).thenReturn(0);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getRandom()).thenReturn(worldRandom);
        when(worldRandom.nextInt(5)).thenReturn(1);
        when(worldRandom.nextInt(4)).thenReturn(3);
        when(worldRandom.nextInt(3)).thenReturn(2);
        when(worldRandom.nextInt(2)).thenReturn(1);
        when(spreadRandom.nextInt(4)).thenReturn(1);
        when(level.getBlockState(any(BlockPos.class))).thenAnswer(invocation -> {
            BlockPos queried = invocation.getArgument(0);
            return states.getOrDefault(queried, Blocks.AIR.defaultBlockState());
        });
        when(level.getFluidState(any(BlockPos.class))).thenAnswer(invocation -> {
            BlockPos queried = invocation.getArgument(0);
            BlockState queriedState = states.getOrDefault(queried, Blocks.AIR.defaultBlockState());
            return queriedState.getFluidState();
        });
        doAnswer(invocation -> {
                    BlockPos changedPos = invocation.getArgument(0);
                    BlockState changedState = invocation.getArgument(1);
                    states.put(changedPos.immutable(), changedState);
                    return true;
                })
                .when(level)
                .setBlock(any(BlockPos.class), any(BlockState.class), anyInt());

        invokeLifecycle(goo, level, origin, FluxGooFluid.gooBlockState(8).getFluidState(), spreadRandom);

        assertEquals(2, states.get(origin).getFluidState().getAmount());
        assertEquals(1, states.get(north).getFluidState().getAmount());
        assertEquals(2, states.get(east).getFluidState().getAmount());
        assertEquals(2, states.get(south).getFluidState().getAmount());
        assertEquals(2, states.get(west).getFluidState().getAmount());
        int remainingQuanta = states.get(origin).getFluidState().getAmount()
                + states.get(north).getFluidState().getAmount()
                + states.get(east).getFluidState().getAmount()
                + states.get(south).getFluidState().getAmount()
                + states.get(west).getFluidState().getAmount();
        assertEquals(9, remainingQuanta);

        verify(worldRandom).nextInt(5);
        verify(worldRandom).nextInt(4);
        verify(worldRandom).nextInt(3);
        verify(worldRandom).nextInt(2);
        verify(spreadRandom).nextInt(4);
        verify(spreadRandom, never()).nextInt(5);
        verify(level).setBlock(origin, FluxGooFluid.gooBlockState(2), Block.UPDATE_CLIENTS);
        verify(level).setBlock(east, FluxGooFluid.gooBlockState(2), Block.UPDATE_ALL);
        verify(level).setBlock(south, FluxGooFluid.gooBlockState(2), Block.UPDATE_ALL);
        verify(level).setBlock(west, FluxGooFluid.gooBlockState(2), Block.UPDATE_ALL);
        verify(level, never()).setBlock(eq(north), any(BlockState.class), anyInt());
        verify(level, never()).setBlock(any(BlockPos.class), eq(Blocks.AIR.defaultBlockState()), anyInt());
        verify(level, times(4)).scheduleTick(any(BlockPos.class), any(Fluid.class), anyInt());
        verify(level).scheduleTick(eq(origin), any(Fluid.class), anyInt());
        verify(level, never()).scheduleTick(eq(north), any(Fluid.class), anyInt());
    }

    private static void invokeLifecycle(
            FluxGooFluid goo, ServerLevel level, BlockPos pos, FluidState state, RandomSource random) throws Exception {
        Method lifecycle = FluxGooFluid.class.getDeclaredMethod(
                "lifecycleTick", ServerLevel.class, BlockPos.class, FluidState.class, RandomSource.class);
        lifecycle.setAccessible(true);
        lifecycle.invoke(goo, level, pos, state, random);
    }
}
