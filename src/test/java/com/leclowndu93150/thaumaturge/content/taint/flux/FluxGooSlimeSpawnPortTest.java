package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class FluxGooSlimeSpawnPortTest {
    @Test
    void failedSlimeFactoryLeavesTheGooBlockUntouched() {
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos pos = new BlockPos(3, 4, 5);

        boolean spawned = FluxGooFluid.spawnSlime(level, pos, 1, null, random);

        assertFalse(spawned);
        verifyNoInteractions(level, random);
    }
}
