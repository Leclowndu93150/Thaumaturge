package com.leclowndu93150.thaumaturge.content.device;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ItemGrateTest {
    @Test
    void manualAndRedstoneTransitionsPlaySoundButPowerOnlyChangeDoesNot() {
        Level level = mock(Level.class);
        BlockItemGrate block = TCBlocks.ITEM_GRATE.get();
        BlockState closed = block.defaultBlockState();
        block.useWithoutItem(closed, level, BlockPos.ZERO, null, null);
        verify(level).playSound(null, BlockPos.ZERO, TCSounds.CREAK.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        clearInvocations(level);
        BlockState open = closed.setValue(BlockItemGrate.OPEN, true);
        block.useWithoutItem(open, level, BlockPos.ZERO, null, null);
        verify(level).playSound(null, BlockPos.ZERO, TCSounds.CREAK.get(), SoundSource.BLOCKS, 0.5F, 0.9F);
        clearInvocations(level);
        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
        block.neighborChanged(closed, level, BlockPos.ZERO, block, BlockPos.ZERO, false);
        verify(level).playSound(null, BlockPos.ZERO, TCSounds.CREAK.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        clearInvocations(level);
        block.neighborChanged(open, level, BlockPos.ZERO, block, BlockPos.ZERO, false);
        verify(level, never())
                .playSound(
                        any(),
                        any(BlockPos.class),
                        any(net.minecraft.sounds.SoundEvent.class),
                        any(),
                        anyFloat(),
                        anyFloat());
    }
}
