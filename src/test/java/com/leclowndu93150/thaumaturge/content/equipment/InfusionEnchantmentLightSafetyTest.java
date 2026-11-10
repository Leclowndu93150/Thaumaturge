package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

class InfusionEnchantmentLightSafetyTest {
    @Test
    void settledLightSkipsNeighborsInUnloadedChunks() {
        ServerLevel level = mock(ServerLevel.class);
        BlockPos center = new BlockPos(15, 64, 0);
        BlockPos loadedNeighbor = center.relative(Direction.UP);
        BlockPos unloadedNeighbor = center.relative(Direction.EAST);
        List<BlockPos> queriedPositions = new ArrayList<>();
        when(level.getRawBrightness(any(BlockPos.class), eq(0))).thenAnswer(invocation -> {
            BlockPos queried = invocation.getArgument(0);
            queriedPositions.add(queried.immutable());
            if (queried.equals(center)) {
                return 3;
            }
            return queried.equals(loadedNeighbor) ? 9 : 0;
        });
        when(level.hasChunk(0, 0)).thenReturn(true);

        assertEquals(8, InfusionEnchantmentEvents.settledLight(level, center));
        assertFalse(queriedPositions.contains(unloadedNeighbor));
    }
}
