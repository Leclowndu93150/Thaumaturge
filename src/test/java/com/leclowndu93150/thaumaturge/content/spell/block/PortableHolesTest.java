package com.leclowndu93150.thaumaturge.content.spell.block;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class PortableHolesTest {
    @Test
    void cannotInspectAnUnloadedChunk() {
        Level level = mock(Level.class);
        when(level.hasChunk(0, 0)).thenReturn(false);

        assertFalse(PortableHoles.canOpen(level, BlockPos.ZERO));
        verify(level, never()).getBlockState(BlockPos.ZERO);
    }

    @Test
    void canOpenAirWithoutTreatingItAsAnUnbreakableBlock() {
        Level level = mock(Level.class);
        when(level.hasChunk(0, 0)).thenReturn(true);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(Blocks.AIR.defaultBlockState());

        assertTrue(PortableHoles.canOpen(level, BlockPos.ZERO));
    }
}
