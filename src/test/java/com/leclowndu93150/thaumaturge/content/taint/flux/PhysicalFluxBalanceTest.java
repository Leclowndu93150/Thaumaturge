package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSprouting;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import org.junit.jupiter.api.Test;

class PhysicalFluxBalanceTest {
    @Test
    void auraConversionRequiresADueTickAndConsumesTheLastQuantumOnlyOnce() {
        ServerLevel level = mock(ServerLevel.class);
        LevelChunk chunk = mock(LevelChunk.class);
        BlockPos pos = BlockPos.ZERO;
        var goo = FluxGooFluid.gooBlockState(1);
        var air = Blocks.AIR.defaultBlockState();
        var state = new AtomicReference<>(goo);
        sample(chunk, pos);
        when(chunk.getBlockState(pos)).thenAnswer(call -> state.get());
        when(level.getBlockState(pos)).thenAnswer(call -> state.get());
        when(level.getGameTime()).thenReturn(20L, 200L, 400L, 600L);

        assertEquals(0.0F, PhysicalFluxAuraFloor.replenish(level, chunk, 100, 0));
        verify(level, never()).setBlock(pos, air, Block.UPDATE_ALL);
        assertEquals(0.0F, PhysicalFluxAuraFloor.replenish(level, chunk, 100, 0));
        when(level.setBlock(pos, air, Block.UPDATE_ALL)).thenAnswer(call -> {
            state.set(air);
            return true;
        });

        assertEquals(0.5F, PhysicalFluxAuraFloor.replenish(level, chunk, 100, 0));
        assertEquals(0.0F, PhysicalFluxAuraFloor.replenish(level, chunk, 100, 0));
        verify(level, times(2)).setBlock(pos, air, Block.UPDATE_ALL);
    }

    @Test
    void aggregateOutbreakRequiresTheFullPocketCost() {
        ServerLevel level = mock(ServerLevel.class);
        LevelChunk chunk = mock(LevelChunk.class);
        RandomSource random = mock(RandomSource.class);
        PhysicalFluxSamples samples = sample(chunk, BlockPos.ZERO);
        for (int x = 0; x < 4; x++) {
            BlockPos pos = new BlockPos(x, 0, 0);
            samples.add(pos);
            when(chunk.getBlockState(pos)).thenReturn(FluxGooFluid.gooBlockState(3));
            when(level.getBlockState(pos)).thenReturn(FluxGooFluid.gooBlockState(3));
        }
        try (var helper = mockStatic(TaintHelper.class);
                var sprouting = mockStatic(TaintSprouting.class);
                var ecology = mockStatic(TaintEcology.class)) {
            assertFalse(PhysicalFluxOutbreaks.tryOutbreak(level, chunk, random));
            sprouting.verifyNoInteractions();

            BlockPos source = BlockPos.ZERO;
            when(chunk.getBlockState(source)).thenReturn(FluxGooFluid.gooBlockState(5));
            when(level.getBlockState(source)).thenReturn(FluxGooFluid.gooBlockState(5));
            helper.when(() -> TaintHelper.hasSturdyNeighbour(level, source)).thenReturn(true);
            sprouting
                    .when(() -> TaintSprouting.plant(eq(level), eq(source), eq(0.0F), any(BooleanSupplier.class)))
                    .thenAnswer(call -> call.<BooleanSupplier>getArgument(3).getAsBoolean());
            var remainder = FluxGooFluid.gooBlockState(1);
            when(level.setBlock(source, remainder, Block.UPDATE_CLIENTS)).thenReturn(false, true);
            when(level.getFluidState(source))
                    .thenReturn(Blocks.AIR.defaultBlockState().getFluidState());

            assertFalse(PhysicalFluxOutbreaks.tryOutbreak(level, chunk, random));
            ecology.verifyNoInteractions();
            assertTrue(PhysicalFluxOutbreaks.tryOutbreak(level, chunk, random));
            verify(level, times(2)).setBlock(source, remainder, Block.UPDATE_CLIENTS);
        }
    }

    private static PhysicalFluxSamples sample(LevelChunk chunk, BlockPos pos) {
        PhysicalFluxSamples samples = new PhysicalFluxSamples();
        samples.add(pos);
        when(chunk.getExistingDataOrNull(TTAttachments.PHYSICAL_FLUX_SAMPLES.get()))
                .thenReturn(samples);
        return samples;
    }
}
