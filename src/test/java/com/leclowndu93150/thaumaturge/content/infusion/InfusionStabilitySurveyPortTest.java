package com.leclowndu93150.thaumaturge.content.infusion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.leclowndu93150.thaumaturge.api.infusion.AltarStabiliser;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

final class InfusionStabilitySurveyPortTest {
    @Test
    void mirroredInlaysUseModernAltarStabiliserContract() {
        Level level = mock(Level.class);
        BlockPos matrix = new BlockPos(0, 64, 0);
        BlockPos near = matrix.east();
        BlockPos mirror = matrix.west();
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        doReturn(Blocks.AIR.defaultBlockState()).when(level).getBlockState(any(BlockPos.class));
        doReturn(TTBlocks.INLAY.get().defaultBlockState()).when(level).getBlockState(near);
        doReturn(TTBlocks.INLAY.get().defaultBlockState()).when(level).getBlockState(mirror);

        InfusionStabilitySurvey.Result result = InfusionStabilitySurvey.survey(level, matrix);

        assertTrue(TTBlocks.INLAY.get() instanceof AltarStabiliser);
        assertEquals(0.025F, result.stabilityReplenish(), 0.0001F);
    }

    @Test
    void unloadedMirrorUsesDefaultPairValueWithoutReadingItsChunk() {
        Level level = mock(Level.class);
        BlockPos matrix = new BlockPos(0, 64, 0);
        BlockPos near = matrix.east();
        BlockPos mirror = matrix.west();
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.hasChunk(-1, 0)).thenReturn(false);
        doReturn(Blocks.AIR.defaultBlockState()).when(level).getBlockState(any(BlockPos.class));
        doReturn(TTBlocks.INLAY.get().defaultBlockState()).when(level).getBlockState(near);

        InfusionStabilitySurvey.Result result = InfusionStabilitySurvey.survey(level, matrix);

        assertEquals(-AltarStabiliser.STANDARD_PAIR_VALUE, result.stabilityReplenish(), 0.0001F);
        assertTrue(result.problemBlocks().contains(near));
        verify(level, never()).getBlockState(mirror);
    }

    @Test
    void directStabiliserCheckSkipsUnloadedPositions() {
        Level level = mock(Level.class);
        BlockPos pos = new BlockPos(80, 64, 0);
        when(level.hasChunk(5, 0)).thenReturn(false);

        assertFalse(InfusionStabilitySurvey.isStabiliser(level, pos));
        verify(level, never()).getBlockState(pos);
    }

    @Test
    void modernMatchingPairUsesTheLargerReportedSymmetryPenalty() {
        Level level = mock(Level.class);
        BlockPos matrix = new BlockPos(0, 64, 0);
        BlockPos west = matrix.west();
        BlockPos east = matrix.east();
        Block modernBlock = mock(Block.class, withSettings().extraInterfaces(AltarStabiliser.class));
        AltarStabiliser stabiliser = (AltarStabiliser) modernBlock;
        BlockState stabiliserState = mock(BlockState.class);
        when(stabiliserState.getBlock()).thenReturn(modernBlock);
        when(stabiliserState.is(TTBlockTags.INFUSION_STABILISERS)).thenReturn(false);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        doReturn(Blocks.AIR.defaultBlockState()).when(level).getBlockState(any(BlockPos.class));
        doReturn(stabiliserState).when(level).getBlockState(west);
        doReturn(stabiliserState).when(level).getBlockState(east);
        doReturn(true).when(stabiliser).stabilisesNow(level, west);
        doReturn(true).when(stabiliser).stabilisesNow(level, east);
        doReturn(Blocks.GOLD_BLOCK).when(stabiliser).countsAs(level, west);
        doReturn(Blocks.GOLD_BLOCK).when(stabiliser).countsAs(level, east);
        doReturn(0.2F).when(stabiliser).pairValue(level, west);
        doReturn(0.2F).when(stabiliser).pairValue(level, east);
        doReturn(true).when(stabiliser).penalisesPairWith(level, west, east);
        doReturn(0.1F).when(stabiliser).penaltyValue(level, west);
        doReturn(0.3F).when(stabiliser).penaltyValue(level, east);

        InfusionStabilitySurvey.Result result = InfusionStabilitySurvey.survey(level, matrix);

        assertEquals(-0.3F, result.stabilityReplenish(), 0.0001F);
        assertTrue(result.problemBlocks().contains(west));
        verify(stabiliser).penalisesPairWith(level, west, east);
    }

    @Test
    void matchingBlockOppositeAnInactiveStabiliserStillCountsAsAProblem() {
        Level level = mock(Level.class);
        BlockPos matrix = new BlockPos(0, 64, 0);
        BlockPos west = matrix.west();
        BlockPos east = matrix.east();
        Block stabiliserBlock = mock(Block.class, withSettings().extraInterfaces(AltarStabiliser.class));
        AltarStabiliser stabiliser = (AltarStabiliser) stabiliserBlock;
        BlockState state = mock(BlockState.class);
        when(state.getBlock()).thenReturn(stabiliserBlock);
        when(state.is(TTBlockTags.INFUSION_STABILISERS)).thenReturn(false);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        doReturn(Blocks.AIR.defaultBlockState()).when(level).getBlockState(any(BlockPos.class));
        doReturn(state).when(level).getBlockState(west);
        doReturn(state).when(level).getBlockState(east);
        when(stabiliser.stabilisesNow(level, west)).thenReturn(true);
        when(stabiliser.stabilisesNow(level, east)).thenReturn(false);

        InfusionStabilitySurvey.Result result = InfusionStabilitySurvey.survey(level, matrix);

        assertEquals(-AltarStabiliser.STANDARD_PAIR_VALUE, result.stabilityReplenish(), 0.0001F);
        assertTrue(result.problemBlocks().contains(west));
    }
}
