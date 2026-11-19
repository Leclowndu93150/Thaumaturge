package com.leclowndu93150.thaumaturge.content.taint.ecology;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class PottedEtherealBloomTest {
    @Test
    void pottedBloomUsesTheSameCleansingBlockEntityAndServerTicker() {
        var pot = TTBlocks.POTTED_ETHEREAL_BLOOM.get();
        var state = pot.defaultBlockState();
        assertTrue(TTBlockEntities.ETHEREAL_BLOOM.get().isValid(state));
        assertInstanceOf(BlockEntityEtherealBloom.class, pot.newBlockEntity(BlockPos.ZERO, state));
        Level level = mock(Level.class);
        var ticker = pot.getTicker(level, state, TTBlockEntities.ETHEREAL_BLOOM.get());
        assertNotNull(ticker);
        var bloom = mock(BlockEntityEtherealBloom.class);
        ticker.tick(level, BlockPos.ZERO, state, bloom);
        verify(bloom).serverTick(level, BlockPos.ZERO);
    }
}
