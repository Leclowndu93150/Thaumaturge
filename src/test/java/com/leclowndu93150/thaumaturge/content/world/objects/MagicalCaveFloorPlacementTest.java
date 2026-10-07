package com.leclowndu93150.thaumaturge.content.world.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.junit.jupiter.api.Test;

class MagicalCaveFloorPlacementTest {
    private static final BlockPos ORIGIN = new BlockPos(-5, 0, 33);

    @Test
    void skipsUniformSectionsWithoutLosingFloorsAtTheirBoundaries() {
        BlockState[] states = states();
        Arrays.fill(states, 48, 64, states[1]); // Air from -16 to -1.
        states[0] = states[1]; // Air at the world bottom has no supporting block.
        states[143] = states[1]; // The last scanned position, y=79.
        AtomicInteger reads = new AtomicInteger();

        assertEquals(referenceFloors(states, 80), floors(states, 80, reads));
        assertTrue(reads.get() < 48, "uniform sections should not require reading every block");
    }

    @Test
    void matchesTheOriginalScanAcrossMixedColumnsAndHeightLimits() {
        Random random = new Random(37);
        for (int column = 0; column < 16; column++) {
            BlockState[] states = states();
            BlockState[] choices = {states[0], states[1], states[2]};
            for (int y = 0; y < states.length; ) {
                BlockState state = choices[random.nextInt(choices.length)];
                int end = Math.min(states.length, y + 1 + random.nextInt(48));
                Arrays.fill(states, y, end, state);
                y = end;
            }
            int top = column == 0 ? -64 : column == 1 ? 320 : random.nextInt(384) - 63;
            assertEquals(referenceFloors(states, top), floors(states, top, new AtomicInteger()));
        }
    }

    private static BlockState[] states() {
        BlockState ground = mock(BlockState.class);
        when(ground.is(TTBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)).thenReturn(true);
        BlockState air = mock(BlockState.class);
        when(air.isAir()).thenReturn(true);
        BlockState[] states = new BlockState[384];
        Arrays.fill(states, ground);
        states[1] = air;
        states[2] = mock(BlockState.class);
        return states;
    }

    private static List<BlockPos> referenceFloors(BlockState[] states, int top) {
        List<BlockPos> result = new ArrayList<>();
        for (int y = top - 1; y >= Math.max(-64, top - 384); y--) {
            if (y > -64 && states[y + 64].isAir() && states[y + 63].is(TTBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)) {
                result.add(ORIGIN.atY(y));
            }
        }
        return result;
    }

    private static List<BlockPos> floors(BlockState[] states, int top, AtomicInteger reads) {
        PlacementContext context = mock(PlacementContext.class);
        WorldGenLevel level = mock(WorldGenLevel.class);
        ChunkAccess chunk = mock(ChunkAccess.class);
        LevelChunkSection[] sections = new LevelChunkSection[24];
        for (int index = 0; index < sections.length; index++) {
            int start = index * 16;
            BlockState[] palette = Arrays.copyOfRange(states, start, start + 16);
            LevelChunkSection section = sections[index] = mock(LevelChunkSection.class);
            boolean onlyAir = Arrays.stream(palette).allMatch(BlockState::isAir);
            when(section.hasOnlyAir()).thenReturn(onlyAir);
            when(section.maybeHas(any())).thenAnswer(call -> {
                Predicate<BlockState> predicate = call.getArgument(0);
                return Arrays.stream(palette).anyMatch(predicate);
            });
            when(section.getBlockState(anyInt(), anyInt(), anyInt())).thenAnswer(call -> {
                reads.incrementAndGet();
                return palette[(int) call.getArgument(1)];
            });
        }
        when(chunk.getMinBuildHeight()).thenReturn(-64);
        when(chunk.getHeight()).thenReturn(384);
        when(chunk.getSectionIndex(anyInt())).thenAnswer(call -> ((int) call.getArgument(0) + 64) >> 4);
        when(chunk.getSection(anyInt())).thenAnswer(call -> sections[(int) call.getArgument(0)]);
        when(context.getLevel()).thenReturn(level);
        when(level.getChunk(ORIGIN)).thenReturn(chunk);
        when(context.getHeight(Heightmap.Types.WORLD_SURFACE_WG, ORIGIN.getX(), ORIGIN.getZ()))
                .thenReturn(top);
        when(context.getBlockState(any())).thenAnswer(call -> states[((BlockPos) call.getArgument(0)).getY() + 64]);
        return MagicalCaveFloorPlacement.INSTANCE
                .getPositions(context, RandomSource.create(37), ORIGIN)
                .toList();
    }
}
