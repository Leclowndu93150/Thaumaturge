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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ItemGrateTest {
    @Test
    void insertionDropsWholeStackImmediatelyBelowGrateWithinItsOwnBlock() throws Exception {
        Level level = mock(Level.class);
        var randomField = Level.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(level, net.minecraft.util.RandomSource.create(0));
        BlockPos pos = new BlockPos(10, 20, 30);
        BlockState state = TCBlocks.ITEM_GRATE.get().defaultBlockState().setValue(BlockItemGrate.OPEN, true);
        BlockEntityItemGrate grate = new BlockEntityItemGrate(pos, state);
        when(level.getBlockState(pos)).thenReturn(state);
        when(level.getBlockState(pos.below())).thenReturn(Blocks.AIR.defaultBlockState());
        grate.setLevel(level);
        ItemStack stack = new ItemStack(Items.STONE, 64);
        assertTrue(grate.inventory().insertItem(0, stack, false).isEmpty());
        ArgumentCaptor<Entity> dropped = ArgumentCaptor.forClass(Entity.class);
        verify(level).addFreshEntity(dropped.capture());
        ItemEntity item = (ItemEntity) dropped.getValue();
        assertEquals(64, item.getItem().getCount());
        assertEquals(10.5, item.getX());
        assertEquals(20.625, item.getY());
        assertEquals(30.5, item.getZ());
        assertEquals(20.875, item.getBoundingBox().maxY, 0.001);
        assertTrue(item.getDeltaMovement().y < 0);
        assertTrue(grate.inventory().getStackInSlot(0).isEmpty());
    }

    @Test
    void cachedHandlerRejectsRealAndSimulatedInsertionAfterClosing() {
        Level level = mock(Level.class);
        BlockState open = TCBlocks.ITEM_GRATE.get().defaultBlockState().setValue(BlockItemGrate.OPEN, true);
        BlockEntityItemGrate grate = new BlockEntityItemGrate(BlockPos.ZERO, open);
        grate.setLevel(level);
        when(level.getBlockState(BlockPos.ZERO.below())).thenReturn(Blocks.AIR.defaultBlockState());
        var handler = grate.inventory();
        ItemStack stack = new ItemStack(Items.STONE, 64);
        assertTrue(handler.insertItem(0, stack, true).isEmpty());
        grate.setBlockState(open.setValue(BlockItemGrate.OPEN, false));
        assertSame(stack, handler.insertItem(0, stack, true));
        assertSame(stack, handler.insertItem(0, stack, false));
        assertTrue(handler.getStackInSlot(0).isEmpty());
        verify(level, never()).addFreshEntity(any());
    }

    @Test
    void blockedExitRejectsInsertionButFluidsGlassAndFurnaceRemainValid() {
        Level level = mock(Level.class);
        BlockState open = TCBlocks.ITEM_GRATE.get().defaultBlockState().setValue(BlockItemGrate.OPEN, true);
        BlockEntityItemGrate grate = new BlockEntityItemGrate(BlockPos.ZERO, open);
        grate.setLevel(level);
        ItemStack stack = new ItemStack(Items.STONE, 64);
        when(level.getBlockState(BlockPos.ZERO.below())).thenReturn(Blocks.STONE.defaultBlockState());
        assertSame(stack, grate.inventory().insertItem(0, stack, true));
        assertSame(stack, grate.inventory().insertItem(0, stack, false));
        assertTrue(grate.inventory().getStackInSlot(0).isEmpty());
        verify(level, never()).addFreshEntity(any());
        for (BlockState exit : new BlockState[] {
            Blocks.AIR.defaultBlockState(),
            Blocks.WATER.defaultBlockState(),
            Blocks.GLASS.defaultBlockState(),
            TCBlocks.INFERNAL_FURNACE.get().defaultBlockState()
        }) {
            when(level.getBlockState(BlockPos.ZERO.below())).thenReturn(exit);
            assertTrue(grate.inventory().insertItem(0, stack, true).isEmpty());
            assertTrue(grate.inventory().getStackInSlot(0).isEmpty());
        }
    }

    @Test
    void savedContentsWaitForExitToClearAndEjectOnNeighborUpdate() throws Exception {
        Level level = mock(Level.class);
        var randomField = Level.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(level, net.minecraft.util.RandomSource.create(0));
        BlockItemGrate block = TCBlocks.ITEM_GRATE.get();
        BlockState open = block.defaultBlockState().setValue(BlockItemGrate.OPEN, true);
        BlockEntityItemGrate grate = new BlockEntityItemGrate(BlockPos.ZERO, open);
        grate.setLevel(level);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(open);
        when(level.getBlockState(BlockPos.ZERO.below())).thenReturn(Blocks.STONE.defaultBlockState());
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(grate);
        ItemStack stack = new ItemStack(Items.STONE, 12);
        grate.inventory().setStackInSlot(0, stack);
        assertSame(stack, grate.inventory().getStackInSlot(0));
        verify(level, never()).addFreshEntity(any());
        when(level.getBlockState(BlockPos.ZERO.below())).thenReturn(Blocks.AIR.defaultBlockState());
        block.neighborChanged(open, level, BlockPos.ZERO, Blocks.STONE, BlockPos.ZERO.below(), false);
        assertTrue(grate.inventory().getStackInSlot(0).isEmpty());
        verify(level).addFreshEntity(any(ItemEntity.class));
    }

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
