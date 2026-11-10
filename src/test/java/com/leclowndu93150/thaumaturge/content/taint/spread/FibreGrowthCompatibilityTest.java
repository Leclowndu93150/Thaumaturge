package com.leclowndu93150.thaumaturge.content.taint.spread;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class FibreGrowthCompatibilityTest {
    @Test
    void crossingIntoACleanColumnRequiresAnExtraRoll() {
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos target = new BlockPos(32, 72, -48);
        when(level.getRandom()).thenReturn(random);
        when(random.nextInt(10)).thenReturn(9, 0);
        try (var biomes = mockStatic(TaintBiomeManager.class, CALLS_REAL_METHODS)) {
            biomes.when(() -> TaintBiomeManager.taintColumn(level, target)).thenReturn(true);
            biomes.clearInvocations();
            assertFalse(TaintBiomeManager.trySpreadColumn(level, target));
            biomes.verify(() -> TaintBiomeManager.taintColumn(level, target), never());
            assertTrue(TaintBiomeManager.trySpreadColumn(level, target));
        }
    }

    @Test
    void growthDoesNotSampleRandomnessOrReadBlocksWhenOriginChunkIsNotLoaded() {
        ServerLevel level = mock(ServerLevel.class);
        BlockPos origin = new BlockPos(32, 72, -48);
        when(level.hasChunk(2, -3)).thenReturn(false);

        TaintHelper.attemptFibreGrowth(level, origin, true);

        verify(level).hasChunk(2, -3);
        verify(level, never()).getRandom();
        verify(level, never()).getBlockState(origin);
    }
}
