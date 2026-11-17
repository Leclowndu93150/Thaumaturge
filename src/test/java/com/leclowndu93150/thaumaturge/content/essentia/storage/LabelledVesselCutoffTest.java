package com.leclowndu93150.thaumaturge.content.essentia.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJar;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockAlembic;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.content.item.LabelItem;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LabelledVesselCutoffTest {
    @Test
    void storedAspectTakesPriorityOverDifferentWrittenFilterWithoutChangingLabel() {
        ItemStack label = LabelItem.withAspect(TTAspects.IGNIS);
        assertEquals(TTAspects.ORDO, LabelledVesselActions.aspectToLabel(label, null, TTAspects.ORDO, 7));
        assertEquals(TTAspects.IGNIS, label.get(TTDataComponents.ASPECT_FILTER));
        assertEquals(1, label.getCount());
    }

    @Test
    void emptyVesselUsesWrittenFilterAndPositiveStockWithoutKeyAlsoFallsBack() {
        ItemStack label = LabelItem.withAspect(TTAspects.IGNIS);
        assertEquals(TTAspects.IGNIS, LabelledVesselActions.aspectToLabel(label, null, TTAspects.ORDO, 0));
        assertEquals(TTAspects.IGNIS, LabelledVesselActions.aspectToLabel(label, null, null, 7));
        assertNull(LabelledVesselActions.aspectToLabel(new ItemStack(TTItems.LABEL.get()), null, TTAspects.ORDO, 0));
    }

    @Test
    void existingFilterAndNonLabelItemsRefuseReplacement() {
        ItemStack label = LabelItem.withAspect(TTAspects.IGNIS);
        assertNull(LabelledVesselActions.aspectToLabel(label, TTAspects.ORDO, TTAspects.ORDO, 7));
        assertNull(LabelledVesselActions.aspectToLabel(new ItemStack(Items.PAPER), null, TTAspects.ORDO, 7));
    }

    @Test
    void labelFacingFollowsOppositePlayerDirection() {
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        for (Direction direction : new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            when(player.getDirection()).thenReturn(direction);
            assertEquals(direction.getOpposite(), LabelledVesselActions.labelSideFacing(level, BlockPos.ZERO, player));
        }
    }

    @Test
    void jarAcceptsVerticalClickAndFacesPlayerWhileRetainingStock() {
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        when(player.level()).thenReturn(level);
        when(player.getDirection()).thenReturn(Direction.EAST);
        BlockJar block = (BlockJar) TTBlocks.JAR_NORMAL.get();
        BlockEntityJar jar = new BlockEntityJar(BlockPos.ZERO, block.defaultBlockState());
        jar.setLevel(level);
        jar.setStorageContents(TTAspects.ORDO, 7);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(jar);
        ItemStack label = LabelItem.withAspect(TTAspects.IGNIS);

        assertTrue(block.attachLabel(level, BlockPos.ZERO, Direction.UP, player, label));
        assertEquals(TTAspects.ORDO, jar.aspectFilterKey());
        assertEquals(Direction.WEST, jar.facing());
        assertEquals(TTAspects.ORDO, jar.aspectKey());
        assertEquals(7, jar.amount());
        assertEquals(TTAspects.IGNIS, label.get(TTDataComponents.ASPECT_FILTER));
        verify(level, atLeastOnce()).sendBlockUpdated(any(), any(), any(), anyInt());
        verify(level).playSound(null, BlockPos.ZERO, TTSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Test
    void alembicAcceptsHorizontalClickAndAimsSpoutAtClickedFace() {
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        when(player.level()).thenReturn(level);
        when(player.getDirection()).thenReturn(Direction.SOUTH);
        BlockAlembic block = (BlockAlembic) TTBlocks.ALEMBIC.get();
        BlockEntityAlembic alembic = new BlockEntityAlembic(BlockPos.ZERO, block.defaultBlockState());
        alembic.setLevel(level);
        alembic.setStorageContents(TTAspects.ORDO, 7);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(alembic);
        ItemStack label = LabelItem.withAspect(TTAspects.IGNIS);

        assertTrue(block.attachLabel(level, BlockPos.ZERO, Direction.EAST, player, label));
        assertEquals(TTAspects.ORDO, alembic.aspectFilterKey());
        assertEquals(Direction.EAST, alembic.spoutSide());
        assertEquals(TTAspects.ORDO, alembic.aspectKey());
        assertEquals(7, alembic.amount());
        verify(level).invalidateCapabilities(BlockPos.ZERO);
        verify(level, atLeastOnce()).sendBlockUpdated(any(), any(), any(), anyInt());
    }

    @ParameterizedTest
    @EnumSource(
            value = Direction.class,
            names = {"UP", "DOWN"})
    void alembicRejectsVerticalFacesWithoutApplyingFilterOrChangingSpout(Direction face) {
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        BlockAlembic block = (BlockAlembic) TTBlocks.ALEMBIC.get();
        BlockEntityAlembic alembic = new BlockEntityAlembic(BlockPos.ZERO, block.defaultBlockState());
        alembic.setLevel(level);
        alembic.setStorageContents(TTAspects.ORDO, 7);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(alembic);
        Direction originalSpout = alembic.spoutSide();
        ItemStack label = LabelItem.withAspect(TTAspects.IGNIS);
        clearInvocations(level);

        assertFalse(block.attachLabel(level, BlockPos.ZERO, face, player, label));
        assertNull(alembic.aspectFilterKey());
        assertEquals(originalSpout, alembic.spoutSide());
        assertEquals(TTAspects.ORDO, alembic.aspectKey());
        assertEquals(7, alembic.amount());
        assertEquals(1, label.getCount());
        verify(level, never()).invalidateCapabilities(BlockPos.ZERO);
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
        verify(level, never()).playSound(null, BlockPos.ZERO, TTSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Test
    void positivePourAddsFluxAndUsesCutoffSoundNumbersWithOneRandomSample() {
        Level level = mock(Level.class);
        RandomSource random = mock(RandomSource.class);
        when(level.getRandom()).thenReturn(random);
        when(random.nextFloat()).thenReturn(0.5F);
        try (var aura = mockStatic(AuraHelper.class)) {
            LabelledVesselActions.pourOut(level, BlockPos.ZERO, 7);
            aura.verify(() -> AuraHelper.addFlux(level, BlockPos.ZERO, 7.0F));
            aura.verifyNoMoreInteractions();
        }
        verify(level).playSound(null, BlockPos.ZERO, TTSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
        verify(level).playSound(null, BlockPos.ZERO, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.6F, 1.0F);
        verify(random, times(1)).nextFloat();
    }

    @Test
    void zeroAndNegativePourStillPlaySoundsWithoutAddingFlux() {
        Level level = mock(Level.class);
        RandomSource random = mock(RandomSource.class);
        when(level.getRandom()).thenReturn(random);
        when(random.nextFloat()).thenReturn(0.0F);
        try (var aura = mockStatic(AuraHelper.class)) {
            LabelledVesselActions.pourOut(level, BlockPos.ZERO, 0);
            LabelledVesselActions.pourOut(level, BlockPos.ZERO, -7);
            aura.verifyNoInteractions();
        }
        verify(level, times(2)).playSound(null, BlockPos.ZERO, TTSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
        verify(level, times(2))
                .playSound(null, BlockPos.ZERO, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.6F, 0.85F);
        verify(random, times(2)).nextFloat();
    }
}
