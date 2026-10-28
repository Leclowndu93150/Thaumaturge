package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

class NodeGeneratorPortTest {
    @Test
    void guaranteedTaintedNodeEligibilityUsesTheExtensibleBiomeTag() {
        ServerLevelAccessor level = mock(ServerLevelAccessor.class);
        Holder<Biome> biome = mock(Holder.class);
        BlockPos pos = new BlockPos(8, 64, 8);
        when(level.getBiome(pos)).thenReturn(biome);
        when(biome.is(TTBiomeTags.IS_TAINTED)).thenReturn(true);

        assertTrue(NodeGenerator.isTaintedBiome(level, pos));
        verify(biome).is(TTBiomeTags.IS_TAINTED);
    }

    @Test
    void biomeAspectSeedKeepsEveryAspectAndAddsOneWeightedPick() {
        Holder<IAspect> first = Holder.direct(mock(IAspect.class));
        Holder<IAspect> selected = Holder.direct(mock(IAspect.class));
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(2)).thenReturn(1);

        Map<Holder<IAspect>, Integer> seeded =
                NodeGenerator.seedAspects(List.of(first, selected), List.of(), List.of(), random);

        assertEquals(Map.of(first, 1, selected, 2), seeded);
    }

    @Test
    void missingBiomeAspectsSeedOnePrimalAndOneCompound() {
        Holder<IAspect> firstPrimal = Holder.direct(mock(IAspect.class));
        Holder<IAspect> selectedPrimal = Holder.direct(mock(IAspect.class));
        Holder<IAspect> selectedCompound = Holder.direct(mock(IAspect.class));
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(2)).thenReturn(1, 0);

        Map<Holder<IAspect>, Integer> seeded = NodeGenerator.seedAspects(
                List.of(), List.of(firstPrimal, selectedPrimal), List.of(selectedCompound, firstPrimal), random);

        assertEquals(Map.of(selectedPrimal, 1, selectedCompound, 1), seeded);
    }

    @Test
    void terrainSurveyUsesTheFirstMatchingCategory() {
        List<NodeGenerator.TerrainRule> rules = List.of(
                new NodeGenerator.TerrainRule(state -> true, 1, List.of()),
                new NodeGenerator.TerrainRule(state -> true, 1, List.of()),
                new NodeGenerator.TerrainRule(state -> true, 1, List.of()));
        int[] counts = new int[rules.size()];

        NodeGenerator.classify(Blocks.WATER.defaultBlockState(), counts, rules);

        assertEquals(1, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(0, counts[2]);
    }

    @Test
    void guaranteedNodeRollUsesTheFirstMatchingTypeAndStops() {
        NodeData normal = new NodeData(NodeType.NORMAL, Optional.empty(), AspectList.EMPTY, AspectList.EMPTY);
        NodeData tainted = new NodeData(NodeType.TAINTED, Optional.empty(), AspectList.EMPTY, AspectList.EMPTY);
        AtomicInteger attempts = new AtomicInteger();

        Optional<NodeData> selected = NodeGenerator.firstRollOfType(
                () -> switch (attempts.getAndIncrement()) {
                    case 0 -> null;
                    case 1 -> normal;
                    default -> tainted;
                },
                NodeType.TAINTED);

        assertTrue(selected.isPresent());
        assertEquals(NodeType.TAINTED, selected.get().type());
        assertEquals(3, attempts.get());
    }

    @Test
    void biomeAuraStrengthRetainsPrecisionUntilAfterTaintedMultipliers() {
        assertEquals(75.75F, NodeGenerator.scaleBiomeStrength(101, 0.75F), 0.0F);
        assertEquals(151.5F, NodeGenerator.scaleBiomeStrength(101, 1.5F), 0.0F);
    }

    @Test
    void oddStrengthBudgetCanReachOneBelowItsInput() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(51)).thenReturn(50);

        assertEquals(100, NodeGenerator.chooseStrengthBudget(101, random));
    }

    @Test
    void weightedBudgetAddsStrengthAccordingToAspectSeeds() throws Exception {
        Holder<IAspect> first = Holder.direct(mock(IAspect.class));
        Holder<IAspect> second = Holder.direct(mock(IAspect.class));
        Holder<IAspect> third = Holder.direct(mock(IAspect.class));
        Map<Holder<IAspect>, Integer> tally = new LinkedHashMap<>();
        tally.put(first, 2);
        tally.put(second, 1);
        tally.put(third, 1);
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(25)).thenReturn(0);
        when(random.nextInt(50)).thenReturn(0);

        Method allocate =
                NodeGenerator.class.getDeclaredMethod("allocateBudget", Map.class, int.class, RandomSource.class);
        allocate.setAccessible(true);
        allocate.invoke(null, tally, 17, random);

        assertEquals(Map.of(first, 7, second, 5, third, 5), tally);
    }

    @Test
    void budgetBelowSeedStrengthDoesNotAddMorePoints() throws Exception {
        Holder<IAspect> first = Holder.direct(mock(IAspect.class));
        Holder<IAspect> second = Holder.direct(mock(IAspect.class));
        Map<Holder<IAspect>, Integer> tally = new LinkedHashMap<>();
        tally.put(first, 2);
        tally.put(second, 1);
        RandomSource random = mock(RandomSource.class);

        Method allocate =
                NodeGenerator.class.getDeclaredMethod("allocateBudget", Map.class, int.class, RandomSource.class);
        allocate.setAccessible(true);
        allocate.invoke(null, tally, 2, random);

        assertEquals(3, tally.values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(2, tally.get(first));
        assertEquals(1, tally.get(second));
    }

    @Test
    void aspectListConversionKeepsOnlyPositiveGeneratedCounts() throws Exception {
        Holder<IAspect> positive = Holder.direct(mock(IAspect.class));
        Holder<IAspect> zero = Holder.direct(mock(IAspect.class));
        Map<Holder<IAspect>, Integer> tally = new LinkedHashMap<>();
        tally.put(positive, 3);
        tally.put(zero, 0);

        Method convert = NodeGenerator.class.getDeclaredMethod("fromMap", Map.class);
        convert.setAccessible(true);
        AspectList result = (AspectList) convert.invoke(null, tally);

        assertEquals(3, result.amountOf(positive));
        assertEquals(0, result.amountOf(zero));
        assertEquals(1, result.entries().size());
        assertEquals(positive, result.entries().getFirst().aspect());
    }

    @Test
    void configuredRarityScalesSpecialNodeOddsAndWussModeRemovesTaintedRolls() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextDouble()).thenReturn(0.03, 0.12, 0.12);

        assertEquals(NodeType.DARK, rollType(random, 9, false));
        assertEquals(NodeType.TAINTED, rollType(random, 18, false));
        assertEquals(NodeType.HUNGRY, rollType(random, 18, true));
    }

    @Test
    void terrainSurveySkipsUnloadedColumnsAtChunkBoundaries() throws Exception {
        ServerLevelAccessor level = mock(ServerLevelAccessor.class);
        when(level.hasChunkAt(anyInt(), anyInt())).thenAnswer(invocation -> {
            int x = invocation.getArgument(0);
            int z = invocation.getArgument(1);
            return x < 16 && z < 16;
        });
        when(level.getBlockState(any(BlockPos.class))).thenAnswer(invocation -> {
            BlockPos pos = invocation.getArgument(0);
            assertTrue(pos.getX() < 16 && pos.getZ() < 16, "Survey must not read an unloaded column");
            return Blocks.AIR.defaultBlockState();
        });

        Class<?> poolsType = Class.forName(NodeGenerator.class.getName() + "$AspectPools");
        Constructor<?> poolsConstructor =
                poolsType.getDeclaredConstructor(HolderLookup.RegistryLookup.class, List.class, List.class, List.class);
        poolsConstructor.setAccessible(true);
        Object pools =
                poolsConstructor.newInstance(mock(HolderLookup.RegistryLookup.class), List.of(), List.of(), List.of());

        Class<?> contextType = Class.forName(NodeGenerator.class.getName() + "$RollContext");
        Constructor<?> contextConstructor = contextType.getDeclaredConstructor(
                ServerLevelAccessor.class,
                BlockPos.class,
                RandomSource.class,
                Holder.class,
                float.class,
                poolsType,
                boolean.class,
                boolean.class,
                boolean.class,
                int.class);
        contextConstructor.setAccessible(true);
        Object context = contextConstructor.newInstance(
                level,
                new BlockPos(15, 64, 15),
                mock(RandomSource.class),
                mock(Holder.class),
                100,
                pools,
                false,
                false,
                false,
                18);

        Class<?> draftType = Class.forName(NodeGenerator.class.getName() + "$Draft");
        var draftConstructor = draftType.getDeclaredConstructor();
        draftConstructor.setAccessible(true);
        Object draft = draftConstructor.newInstance();
        Method survey = NodeGenerator.class.getDeclaredMethod("surveySurroundings", contextType, draftType);
        survey.setAccessible(true);
        survey.invoke(null, context, draft);

        verify(level, times(121)).hasChunkAt(anyInt(), anyInt());
        verify(level, times(396)).getBlockState(any(BlockPos.class));
    }

    private static NodeType rollType(RandomSource random, int rarity, boolean wussMode) {
        return NodeGenerator.rollType(random, rarity, 2.0, 3.0, 5.0, 7.0, 11.0, wussMode);
    }
}
