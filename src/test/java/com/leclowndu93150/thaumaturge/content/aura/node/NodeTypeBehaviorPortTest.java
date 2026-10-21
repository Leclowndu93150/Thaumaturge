package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class NodeTypeBehaviorPortTest {
    private static BlockEntityNode node(NodeType type) {
        BlockEntityNode node =
                new BlockEntityNode(BlockPos.ZERO, TTBlocks.NODE.get().defaultBlockState());
        node.setNodeType(type);
        return node;
    }

    @Test
    void pureErosionOfFinalAspectReturnsTickStopSignal() {
        BlockEntityNode node = node(NodeType.PURE);
        Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));
        node.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(aspect, 1)));
        node.aspects = node.aspectsBase;
        ServerLevel level = mock(ServerLevel.class);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(Blocks.AIR.defaultBlockState());
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.0F);
        when(random.nextInt(1)).thenReturn(0);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.drainFlux(level, BlockPos.ZERO, 0.25F, false))
                    .thenReturn(1.0F);
            assertTrue(NodeTypeBehavior.tick(node, level, BlockPos.ZERO, 200, random));
        }
    }

    @Test
    void pureNodeWithAlreadyEmptyBaseIsRemovedAndStopsTick() {
        BlockEntityNode node = node(NodeType.PURE);
        ServerLevel level = mock(ServerLevel.class);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(Blocks.AIR.defaultBlockState());
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.0F);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.drainFlux(level, BlockPos.ZERO, 0.25F, false))
                    .thenReturn(1.0F);
            assertTrue(NodeTypeBehavior.tick(node, level, BlockPos.ZERO, 200, random));
        }

        verify(level).removeBlock(BlockPos.ZERO, false);
    }

    @Test
    void pureBiomeSpreadReplacesTheSingleSampledColumn() {
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        when(level.getRandom()).thenReturn(random);
        when(level.hasChunkAt(new BlockPos(2, 0, 2))).thenReturn(true);
        when(random.nextInt(8)).thenReturn(3, 1, 4, 2);
        try (MockedStatic<TaintBiomeManager> taint = mockStatic(TaintBiomeManager.class)) {
            taint.when(() -> TaintBiomeManager.isTainted(level, new BlockPos(2, 0, 2)))
                    .thenReturn(true);
            NodeBiomeSpread.tick(level, node(NodeType.PURE), BlockPos.ZERO, 50);
            taint.verify(() -> TaintBiomeManager.replaceColumn(level, new BlockPos(2, 0, 2), TTBiomes.MAGICAL_FOREST));
            verify(random, org.mockito.Mockito.times(4)).nextInt(8);
        }
    }

    @Test
    void darkBiomeSpreadSkipsColumnsAlreadyEerie() {
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        HolderOwner<Biome> owner = new HolderOwner<>() {};
        Holder<Biome> biome = Holder.Reference.createStandAlone(owner, TTBiomes.EERIE);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        when(level.getRandom()).thenReturn(random);
        when(level.hasChunkAt(BlockPos.ZERO)).thenReturn(true);
        when(level.getBiome(BlockPos.ZERO)).thenReturn(biome);
        when(random.nextInt(12)).thenReturn(0);

        try (MockedStatic<TaintBiomeManager> taint = mockStatic(TaintBiomeManager.class)) {
            NodeBiomeSpread.tick(level, node(NodeType.DARK), BlockPos.ZERO, 50);
            taint.verifyNoInteractions();
        }
    }

    @Test
    void darkBiomeSpreadReplacesOtherLoadedColumns() {
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        HolderOwner<Biome> owner = new HolderOwner<>() {};
        Holder<Biome> biome = Holder.Reference.createStandAlone(owner, TTBiomes.MAGICAL_FOREST);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        when(level.getRandom()).thenReturn(random);
        when(level.hasChunkAt(BlockPos.ZERO)).thenReturn(true);
        when(level.getBiome(BlockPos.ZERO)).thenReturn(biome);
        when(random.nextInt(12)).thenReturn(0);

        try (MockedStatic<TaintBiomeManager> taint = mockStatic(TaintBiomeManager.class)) {
            NodeBiomeSpread.tick(level, node(NodeType.DARK), BlockPos.ZERO, 50);
            taint.verify(() -> TaintBiomeManager.replaceColumn(level, BlockPos.ZERO, TTBiomes.EERIE));
        }
    }
}
