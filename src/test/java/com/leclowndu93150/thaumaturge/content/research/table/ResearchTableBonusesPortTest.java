package com.leclowndu93150.thaumaturge.content.research.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class ResearchTableBonusesPortTest {
    private ServerLevel level;
    private RandomSource random;
    private Holder<IAspect> aqua;
    private Holder<IAspect> ignis;
    private Holder<IAspect> ordo;
    private final Map<BlockPos, BlockState> states = new HashMap<>();
    private final List<ChunkPos> checkedChunks = new ArrayList<>();
    private final List<BlockPos> readPositions = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.AQUA, mock(IAspect.class));
        Registry.register(registry, TTAspects.IGNIS, mock(IAspect.class));
        Registry.register(registry, TTAspects.ORDO, mock(IAspect.class));
        RegistryAccess registries = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();

        level = mock(ServerLevel.class);
        random = mock(RandomSource.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.getBrightness(any(LightLayer.class), any(BlockPos.class))).thenReturn(15);
        when(level.canSeeSky(any(BlockPos.class))).thenReturn(true);
        when(level.getHeight()).thenReturn(256);
        when(level.getRandom()).thenReturn(random);
        when(level.hasChunk(anyInt(), anyInt())).thenAnswer(call -> {
            checkedChunks.add(new ChunkPos(call.getArgument(0), call.getArgument(1)));
            return true;
        });
        when(level.getBlockState(any(BlockPos.class))).thenAnswer(call -> {
            BlockPos position = ((BlockPos) call.getArgument(0)).immutable();
            readPositions.add(position);
            return states.getOrDefault(position, Blocks.AIR.defaultBlockState());
        });

        aqua = registry.getHolderOrThrow(TTAspects.AQUA);
        ignis = registry.getHolderOrThrow(TTAspects.IGNIS);
        ordo = registry.getHolderOrThrow(TTAspects.ORDO);
    }

    @Test
    void overlappingFluidAndOrdoCandidatesAreRolledIndependentlyThenRandomlySelected() {
        BlockPos first = scanStart();
        BlockState overlap = mock(BlockState.class);
        FluidState water = mock(FluidState.class);
        when(overlap.getFluidState()).thenReturn(water);
        when(water.is(FluidTags.WATER)).thenReturn(true);
        when(overlap.is(TTBlockTags.RESEARCH_BONUS_ORDO)).thenReturn(true);
        states.put(first, overlap);
        when(random.nextInt(2)).thenReturn(1);

        AspectList result = ResearchTableBonuses.recalculate(level, BlockPos.ZERO, AspectList.EMPTY, random);

        assertEquals(AspectList.of(new AspectInstance(ordo, 1)), result);
        verify(random).nextInt(15);
        verify(random).nextInt(20);
        verify(random).nextInt(2);
    }

    @Test
    void choosingAnExistingCandidateContinuesToTheNextLoadedPosition() {
        BlockPos first = scanStart();
        BlockPos second = first.offset(0, 0, 1);
        BlockState overlap = mock(BlockState.class);
        FluidState water = mock(FluidState.class);
        when(overlap.getFluidState()).thenReturn(water);
        when(water.is(FluidTags.WATER)).thenReturn(true);
        when(overlap.is(TTBlockTags.RESEARCH_BONUS_ORDO)).thenReturn(true);
        states.put(first, overlap);
        states.put(second, Blocks.FIRE.defaultBlockState());
        AspectList current = AspectList.of(new AspectInstance(aqua, 1));
        when(random.nextInt(2)).thenReturn(0);
        when(random.nextInt(1)).thenReturn(0);

        AspectList result = ResearchTableBonuses.recalculate(level, BlockPos.ZERO, current, random);

        assertEquals(AspectList.of(new AspectInstance(aqua, 1), new AspectInstance(ignis, 1)), result);
        assertTrue(checkedChunks.contains(new ChunkPos(second)));
        assertTrue(readPositions.contains(second));
    }

    @Test
    void unloadedScanPositionsAreNeverRead() {
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);

        AspectList result = ResearchTableBonuses.recalculate(level, BlockPos.ZERO, AspectList.EMPTY, random);

        assertEquals(AspectList.EMPTY, result);
        verify(level, never()).getBlockState(any(BlockPos.class));
    }

    private static BlockPos scanStart() {
        return new BlockPos(-8, -8, -8);
    }
}
