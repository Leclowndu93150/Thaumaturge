package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.junit.jupiter.api.Test;

class FluxGooVerticalFlowPortTest {
    @Test
    void partialVerticalFlowWritesAndSchedulesResidualOfOne() throws Exception {
        WorldFixture world = new WorldFixture(2, FluxGooFluid.gooBlockState(7));

        invokeSpread(world, 2);

        assertEquals(1, world.state(world.origin).getFluidState().getAmount());
        assertEquals(8, world.state(world.below).getFluidState().getAmount());
        verify(world.level).setBlock(world.below, FluxGooFluid.gooBlockState(8), Block.UPDATE_CLIENTS);
        verify(world.level).setBlock(world.origin, FluxGooFluid.gooBlockState(1), Block.UPDATE_CLIENTS);
        verifyTick(world, world.below, gooFluid(8));
        verifyTick(world, world.origin, gooFluid(1));
    }

    @Test
    void partialVerticalFlowWritesAndSchedulesResidualAboveOneWithBlockedSides() throws Exception {
        WorldFixture world = new WorldFixture(8, FluxGooFluid.gooBlockState(7));

        invokeSpread(world, 8);

        assertEquals(7, world.state(world.origin).getFluidState().getAmount());
        assertEquals(8, world.state(world.below).getFluidState().getAmount());
        verify(world.level).setBlock(world.below, FluxGooFluid.gooBlockState(8), Block.UPDATE_CLIENTS);
        verify(world.level).setBlock(world.origin, FluxGooFluid.gooBlockState(7), Block.UPDATE_CLIENTS);
        verifyTick(world, world.below, gooFluid(8));
        verifyTick(world, world.origin, gooFluid(7));
        verify(world.level, never()).setBlock(eq(world.origin.north()), any(BlockState.class), anyInt());
    }

    @Test
    void partialResidualIsCommittedBeforeTheNextSidewaysFlowTick() throws Exception {
        WorldFixture world = new WorldFixture(8, FluxGooFluid.gooBlockState(7));
        BlockPos north = world.origin.north();
        world.states.put(north, FluxGooFluid.gooBlockState(3));

        invokeSpread(world, 8);

        assertEquals(8, world.state(world.below).getFluidState().getAmount());
        assertEquals(7, world.state(world.origin).getFluidState().getAmount());
        assertEquals(3, world.state(north).getFluidState().getAmount());
        verify(world.level).setBlock(world.origin, FluxGooFluid.gooBlockState(7), Block.UPDATE_CLIENTS);
        verify(world.level, never()).setBlock(eq(world.origin), eq(FluxGooFluid.gooBlockState(5)), anyInt());
        verify(world.level, never()).setBlock(eq(north), any(BlockState.class), anyInt());
        verifyTick(world, world.below, gooFluid(8));
        verifyTick(world, world.origin, gooFluid(7));
        verifyNoTick(world, north);
        int total = world.state(world.below).getFluidState().getAmount()
                + world.state(world.origin).getFluidState().getAmount()
                + world.state(north).getFluidState().getAmount();
        assertEquals(18, total);
    }

    @Test
    void fullVerticalTransferClearsOriginAndSchedulesNewGoo() throws Exception {
        WorldFixture world = new WorldFixture(4, Blocks.AIR.defaultBlockState());

        invokeSpread(world, 4);

        assertEquals(4, world.state(world.below).getFluidState().getAmount());
        assertEquals(Blocks.AIR.defaultBlockState(), world.state(world.origin));
        verify(world.level).setBlock(world.below, FluxGooFluid.gooBlockState(4), Block.UPDATE_ALL);
        verify(world.level).setBlock(world.origin, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        verifyTick(world, world.below, gooFluid(4));
        verifyNoTick(world, world.origin);
    }

    @Test
    void fullExistingGooBelowDoesNotChangeEitherBlockOrScheduleTicks() throws Exception {
        WorldFixture world = new WorldFixture(8, FluxGooFluid.gooBlockState(8));

        invokeSpread(world, 8);

        verify(world.level, never()).setBlock(any(BlockPos.class), any(BlockState.class), anyInt());
        verify(world.level, never()).scheduleTick(any(BlockPos.class), any(Fluid.class), anyInt());
    }

    @Test
    void verticalFlowSwapsAndSchedulesOrdinaryLiquid() throws Exception {
        Fluid water = Blocks.WATER.defaultBlockState().getFluidState().getType();
        FluidState waterFluidState = mock(FluidState.class);
        BlockState waterState = mock(BlockState.class);
        when(waterFluidState.isEmpty()).thenReturn(false);
        when(waterFluidState.is(FluidTags.WATER)).thenReturn(true);
        when(waterFluidState.getType()).thenReturn(water);
        when(waterFluidState.getFluidType()).thenReturn(water.getFluidType());
        when(waterState.isAir()).thenReturn(false);
        when(waterState.getFluidState()).thenReturn(waterFluidState);
        WorldFixture world = new WorldFixture(4, waterState);

        invokeSpread(world, 4);

        assertEquals(4, world.state(world.below).getFluidState().getAmount());
        assertEquals(waterState, world.state(world.origin));
        verify(world.level).setBlock(world.below, FluxGooFluid.gooBlockState(4), Block.UPDATE_ALL);
        verify(world.level).setBlock(world.origin, waterState, Block.UPDATE_ALL);
        verifyTick(world, world.below, gooFluid(4));
        verifyTick(world, world.origin, water);
    }

    @Test
    void worldBottomClearsOriginWithoutReadingOrSchedulingBelow() throws Exception {
        WorldFixture world = new WorldFixture(4, Blocks.STONE.defaultBlockState());
        when(world.level.getMinBuildHeight()).thenReturn(world.origin.getY());

        invokeSpread(world, 4);

        assertEquals(Blocks.AIR.defaultBlockState(), world.state(world.origin));
        verify(world.level).setBlock(world.origin, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        verify(world.level, never()).getBlockState(world.below);
        verifyNoTick(world, world.origin);
    }

    private static void invokeSpread(WorldFixture world, int amount) throws Exception {
        Method spread =
                FluxGooFluid.class.getDeclaredMethod("spreadTick", ServerLevel.class, BlockPos.class, FluidState.class);
        spread.setAccessible(true);
        spread.invoke(
                world.goo,
                world.level,
                world.origin,
                FluxGooFluid.gooBlockState(amount).getFluidState());
    }

    private static void verifyTick(WorldFixture world, BlockPos pos, Fluid fluid) {
        verify(world.level).scheduleTick(eq(pos), eq(fluid), eq(fluid.getTickDelay(world.level)));
    }

    private static Fluid gooFluid(int amount) {
        return FluxGooFluid.gooBlockState(amount).getFluidState().getType();
    }

    private static void verifyNoTick(WorldFixture world, BlockPos pos) {
        verify(world.level, never()).scheduleTick(eq(pos), any(Fluid.class), anyInt());
    }

    private static final class WorldFixture {
        private final FluxGooFluid goo = (FluxGooFluid) FluxGooRefs.sourceFluid();
        private final ServerLevel level = mock(ServerLevel.class);
        private final RandomSource random = mock(RandomSource.class);
        private final BlockPos origin = new BlockPos(0, 64, 0);
        private final BlockPos below = origin.below();
        private final Map<BlockPos, BlockState> states = new HashMap<>();

        private WorldFixture(int amount, BlockState belowState) {
            states.put(origin, FluxGooFluid.gooBlockState(amount));
            states.put(below, belowState);
            states.put(origin.above(), Blocks.STONE.defaultBlockState());
            states.put(origin.north(), Blocks.STONE.defaultBlockState());
            states.put(origin.east(), Blocks.STONE.defaultBlockState());
            states.put(origin.south(), Blocks.STONE.defaultBlockState());
            states.put(origin.west(), Blocks.STONE.defaultBlockState());
            when(level.getMinBuildHeight()).thenReturn(0);
            when(level.hasChunkAt(any(BlockPos.class))).thenReturn(true);
            when(level.getRandom()).thenReturn(random);
            when(level.getBlockState(any(BlockPos.class))).thenAnswer(invocation -> {
                BlockPos queried = invocation.getArgument(0);
                return state(queried);
            });
            when(level.getFluidState(any(BlockPos.class))).thenAnswer(invocation -> {
                BlockPos queried = invocation.getArgument(0);
                return state(queried).getFluidState();
            });
            doAnswer(invocation -> {
                        BlockPos changedPos = invocation.getArgument(0);
                        BlockState changedState = invocation.getArgument(1);
                        states.put(changedPos.immutable(), changedState);
                        return true;
                    })
                    .when(level)
                    .setBlock(any(BlockPos.class), any(BlockState.class), anyInt());
        }

        private BlockState state(BlockPos pos) {
            return states.getOrDefault(pos, Blocks.STONE.defaultBlockState());
        }
    }
}
