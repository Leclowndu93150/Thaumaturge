package com.leclowndu93150.thaumaturge.content.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BlockVisBatteryTest {
    @Test
    void usesGlassSounds() {
        assertEquals(
                SoundType.GLASS,
                TTBlocks.VIS_BATTERY
                        .get()
                        .defaultBlockState()
                        .getSoundType(mock(LevelReader.class), BlockPos.ZERO, null));
    }

    @Test
    void redstoneWakeClearsDelayedBatteryTick() {
        ServerLevel level = mock(ServerLevel.class);
        LevelTicks<Block> ticks = new LevelTicks<>(ignored -> true, () -> mock(ProfilerFiller.class));
        LevelChunkTicks<Block> chunkTicks = new LevelChunkTicks<>();
        BlockVisBattery battery = TTBlocks.VIS_BATTERY.get();
        BlockState state = battery.defaultBlockState().setValue(BlockVisBattery.CHARGE, 2);
        ticks.addContainer(new ChunkPos(BlockPos.ZERO), chunkTicks);
        ticks.schedule(new ScheduledTick<>(battery, BlockPos.ZERO, 100L, 0L));
        assertTrue(ticks.hasScheduledTick(BlockPos.ZERO, battery));

        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
        when(level.getBlockTicks()).thenReturn(ticks);

        battery.neighborChanged(state, level, BlockPos.ZERO, battery, BlockPos.ZERO, false);

        assertFalse(ticks.hasScheduledTick(BlockPos.ZERO, battery));
        verify(level).scheduleTick(BlockPos.ZERO, battery, 1);
    }

    @Test
    void partialVisDrainIsRefundedWithoutCharging() {
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockVisBattery battery = TTBlocks.VIS_BATTERY.get();
        BlockState state = battery.defaultBlockState();

        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(false);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.getVis(level, BlockPos.ZERO)).thenReturn(20.0F);
            aura.when(() -> AuraHelper.getAuraBase(level, BlockPos.ZERO)).thenReturn(10);
            aura.when(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 1.0F, false))
                    .thenReturn(0.4F);

            battery.tick(state, level, BlockPos.ZERO, random);

            assertEquals(0, state.getValue(BlockVisBattery.CHARGE));
            aura.verify(() -> AuraHelper.addVis(level, BlockPos.ZERO, 0.4F));
            verify(level, never()).setBlockAndUpdate(BlockPos.ZERO, state.setValue(BlockVisBattery.CHARGE, 1));
            verify(level, never()).scheduleTick(eq(BlockPos.ZERO), eq(battery), anyInt());
        }
    }
}
