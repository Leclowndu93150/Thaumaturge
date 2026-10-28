package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Lifecycle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class NodeUpkeepPortTest {
    private static BlockEntityNode node() {
        return new BlockEntityNode(BlockPos.ZERO, TTBlocks.NODE.get().defaultBlockState());
    }

    private static void invokeDischarge(BlockEntityNode node, ServerLevel level) throws Exception {
        Method method = BlockEntityNode.class.getDeclaredMethod(
                "handleDischarge", ServerLevel.class, BlockPos.class, boolean.class);
        method.setAccessible(true);
        method.invoke(node, level, BlockPos.ZERO, false);
    }

    private static void invokeCollapse(BlockEntityNode node, ServerLevel level, BlockPos pos) throws Exception {
        Method method = BlockEntityNode.class.getDeclaredMethod("collapse", ServerLevel.class, BlockPos.class);
        method.setAccessible(true);
        method.invoke(node, level, pos);
    }

    private static void setInt(BlockEntityNode node, String name, int value) throws Exception {
        Field field = BlockEntityNode.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setInt(node, value);
    }

    private static ServerLevel generatedLevel(RandomSource random) {
        ServerLevel level = mock(ServerLevel.class);
        ServerChunkCache chunkSource = mock(ServerChunkCache.class);
        when(level.getChunkSource()).thenReturn(chunkSource);
        when(chunkSource.getChunkNow(anyInt(), anyInt())).thenReturn(mock(LevelChunk.class));
        when(level.getRandom()).thenReturn(random);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());
        return level;
    }

    private static int getInt(BlockEntityNode node, String name) throws Exception {
        Field field = BlockEntityNode.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(node);
    }

    private static void stubEastDischargeTarget(RandomSource random, boolean returnOneForNonUnitBounds) {
        int[] calls = {0};
        int[] offsets = {1, 0, 0, 0, 0, 0};
        when(random.nextInt(anyInt())).thenAnswer(invocation -> {
            int call = calls[0]++;
            if (call < offsets.length) {
                return offsets[call];
            }
            int bound = invocation.getArgument(0);
            return bound == 1 || !returnOneForNonUnitBounds ? 0 : 1;
        });
    }

    @Test
    void advancedLockedReceiverCanDischargeAndDonorShrinkClipsHeldVisAndRefreshesWait() throws Exception {
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.5F, 0.0F, 0.0F);
        when(random.nextBoolean()).thenReturn(true);
        stubEastDischargeTarget(random, false);
        ServerLevel level = generatedLevel(random);
        BlockEntityNode receiver = node();
        BlockEntityNode donor = node();
        Holder<IAspect> primal = Holder.direct(mock(IAspect.class));
        Holder<IAspect> reserve = Holder.direct(mock(IAspect.class));
        receiver.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(reserve, 10)));
        receiver.aspects = receiver.aspectsBase;
        donor.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(primal, 2)));
        donor.aspects = AspectList.ofEntries(List.of(new AspectInstance(primal, 3)));
        donor.setNodeModifier(NodeModifier.PALE);
        setInt(donor, "regeneration", 400);
        setInt(receiver, "lock", 2);
        when(level.getBlockEntity(new BlockPos(1, 0, 0))).thenReturn(donor);
        Effects.ArcBolt bolt = mock(Effects.ArcBolt.class);
        when(bolt.to(any(Vec3.class))).thenReturn(bolt);
        when(bolt.width(anyFloat())).thenReturn(bolt);
        try (MockedStatic<Effects> effects = mockStatic(Effects.class)) {
            effects.when(() -> Effects.arcBolt(level, Vec3.atCenterOf(new BlockPos(1, 0, 0))))
                    .thenReturn(bolt);
            invokeDischarge(receiver, level);
        }
        assertEquals(1, receiver.aspectsBase.amountOf(primal));
        assertEquals(1, donor.aspects.amountOf(primal));
        assertEquals(1, donor.aspectsBase.amountOf(primal));
        assertEquals(450, getInt(donor, "wait"));
        assertEquals(900, getInt(donor, "regeneration"));
    }

    @Test
    void basicLockedReceiverCannotDischarge() throws Exception {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(anyInt())).thenReturn(1, 0, 0, 0, 0, 0);
        ServerLevel level = generatedLevel(random);
        BlockEntityNode receiver = node();
        setInt(receiver, "lock", 1);
        invokeDischarge(receiver, level);
        verify(level, never()).getBlockEntity(any(BlockPos.class));
    }

    @Test
    void advancedLockedDonorCannotDischarge() throws Exception {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(anyInt())).thenReturn(1, 0, 0, 0, 0, 0);
        ServerLevel level = generatedLevel(random);
        BlockEntityNode receiver = node();
        BlockEntityNode donor = node();
        Holder<IAspect> donorAspect = Holder.direct(mock(IAspect.class));
        Holder<IAspect> receiverAspect = Holder.direct(mock(IAspect.class));
        donor.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(donorAspect, 2)));
        donor.aspects = donor.aspectsBase;
        receiver.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(receiverAspect, 20)));
        receiver.aspects = receiver.aspectsBase;
        setInt(donor, "lock", 2);
        when(level.getBlockEntity(new BlockPos(1, 0, 0))).thenReturn(donor);
        try (MockedStatic<Effects> effects = mockStatic(Effects.class)) {
            invokeDischarge(receiver, level);
            effects.verifyNoInteractions();
        }
        assertEquals(2, donor.aspects.amountOf(donorAspect));
        assertEquals(0, receiver.aspects.amountOf(donorAspect));
    }

    @Test
    void refillIntervalUsesAndCachesCurrentModifierAndLockFactor() throws Exception {
        BlockEntityNode node = node();
        node.setNodeModifier(NodeModifier.PALE);
        assertEquals(900, node.refillInterval());
        setInt(node, "lock", 2);
        setInt(node, "regeneration", -1);
        assertEquals(18_000, node.refillInterval());
        setInt(node, "lock", 0);
        node.setNodeModifier(NodeModifier.BRIGHT);
        assertEquals(400, node.refillInterval());
        node.setNodeModifier(null);
        assertEquals(600, node.refillInterval());
    }

    @Test
    void addonPrimalIsExcludedFromPearlReplacementRolls() {
        ResourceKey<IAspect> addonKey =
                ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("test", "addon_primal"));
        IAspect addon = mock(IAspect.class);
        when(addon.isPrimal()).thenReturn(true);
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        for (ResourceKey<IAspect> primalKey : TTAspects.PRIMALS) {
            IAspect primal = mock(IAspect.class);
            when(primal.isPrimal()).thenReturn(true);
            Registry.register(registry, primalKey, primal);
        }
        Registry.register(registry, addonKey, addon);
        RegistryAccess registryAccess = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        ServerLevel level = mock(ServerLevel.class);
        when(level.registryAccess()).thenReturn(registryAccess);
        BlockEntityNode node = node();
        node.setLevel(level);
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(3)).thenReturn(1, 0, 0, 0, 0, 0);
        when(random.nextInt(5)).thenReturn(1);

        node.applyPrimordialPearl(random, false);

        assertEquals(1, node.aspectsBase.amountOf(registry.getHolderOrThrow(TTAspects.AER)));
        assertEquals(0, node.aspectsBase.amountOf(registry.getHolderOrThrow(addonKey)));
        verify(random, times(TTAspects.PRIMALS.size())).nextInt(3);
    }

    @Test
    void lateDonorWithInvalidRefillCacheGetsARecomputedCooldownWithoutShrinking() throws Exception {
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.5F, 0.0F, 0.0F);
        when(random.nextBoolean()).thenReturn(true);
        stubEastDischargeTarget(random, true);
        ServerLevel level = generatedLevel(random);
        BlockEntityNode receiver = node();
        BlockEntityNode donor = node();
        Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));
        Holder<IAspect> reserve = Holder.direct(mock(IAspect.class));
        receiver.aspectsBase =
                AspectList.ofEntries(List.of(new AspectInstance(aspect, 1), new AspectInstance(reserve, 10)));
        receiver.aspects = receiver.aspectsBase;
        donor.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(aspect, 2)));
        donor.aspects = donor.aspectsBase;
        setInt(donor, "regeneration", -1);
        when(level.getBlockEntity(new BlockPos(1, 0, 0))).thenReturn(donor);
        Effects.ArcBolt bolt = mock(Effects.ArcBolt.class);
        when(bolt.to(any(Vec3.class))).thenReturn(bolt);
        when(bolt.width(anyFloat())).thenReturn(bolt);
        try (MockedStatic<Effects> effects = mockStatic(Effects.class)) {
            effects.when(() -> Effects.arcBolt(level, Vec3.atCenterOf(new BlockPos(1, 0, 0))))
                    .thenReturn(bolt);
            invokeDischarge(receiver, level);
        }

        assertEquals(1, donor.aspects.amountOf(aspect));
        assertEquals(300, getInt(donor, "wait"));
        assertEquals(600, getInt(donor, "regeneration"));
        assertEquals(2, donor.aspectsBase.amountOf(aspect));
    }

    @Test
    void emptyDischargeDonorCollapsesInsteadOfRemainingAsAnEmptyNode() throws Exception {
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.5F, 0.0F, 0.0F);
        when(random.nextBoolean()).thenReturn(true);
        stubEastDischargeTarget(random, false);
        ServerLevel level = generatedLevel(random);
        BlockPos donorPos = new BlockPos(1, 0, 0);
        BlockEntityNode receiver = node();
        BlockEntityNode donor = node();
        Holder<IAspect> donorAspect = Holder.direct(mock(IAspect.class));
        Holder<IAspect> receiverAspect = Holder.direct(mock(IAspect.class));
        donor.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(donorAspect, 1)));
        donor.aspects = donor.aspectsBase;
        receiver.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(receiverAspect, 10)));
        receiver.aspects = receiver.aspectsBase;
        when(level.getBlockEntity(donorPos)).thenReturn(donor);
        Effects.ArcBolt bolt = mock(Effects.ArcBolt.class);
        when(bolt.to(any(Vec3.class))).thenReturn(bolt);
        when(bolt.width(anyFloat())).thenReturn(bolt);
        try (MockedStatic<Effects> effects = mockStatic(Effects.class)) {
            effects.when(() -> Effects.arcBolt(level, Vec3.atCenterOf(donorPos)))
                    .thenReturn(bolt);

            invokeDischarge(receiver, level);
        }

        assertTrue(donor.aspects.isEmpty());
        verify(level).destroyBlock(donorPos, true);
    }

    @Test
    void collapsingNodeHostLeavesItsDepletedHostBlockInPlace() throws Exception {
        BlockPos pos = BlockPos.ZERO;
        BlockState hostState = TTBlocks.SILVERWOOD_NODE_LOG.get().defaultBlockState();
        NodeHostBlock host = (NodeHostBlock) hostState.getBlock();
        ServerLevel level = mock(ServerLevel.class);
        when(level.getBlockState(pos)).thenReturn(hostState);

        invokeCollapse(new BlockEntityNode(pos, hostState), level, pos);

        verify(level).setBlock(pos, host.depletedState(hostState), Block.UPDATE_ALL);
        verify(level, never()).destroyBlock(any(BlockPos.class), anyBoolean());
    }

    @Test
    void finalAspectDecayReturnsBeforeLaterBehaviorAndSkipsTwentyRoll() throws Exception {
        RandomSource random = mock(RandomSource.class);
        TrackingNode node = new TrackingNode();
        Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));
        node.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(aspect, 1)));
        node.aspects = AspectList.EMPTY;
        setInt(node, "count", 1199);
        ServerLevel level = generatedLevel(random);
        node.setLevel(level);
        try (MockedStatic<com.leclowndu93150.thaumaturge.api.aura.AuraHelper> aura =
                mockStatic(com.leclowndu93150.thaumaturge.api.aura.AuraHelper.class)) {
            aura.when(() -> com.leclowndu93150.thaumaturge.api.aura.AuraHelper.getAuraBase(level, BlockPos.ZERO))
                    .thenReturn(0);
            node.serverTick(level, BlockPos.ZERO);
        }

        assertTrue(node.aspectsBase.isEmpty());
        assertEquals(0, node.typeBehaviorCalls);
        verify(random, never()).nextInt(20);
    }

    private static final class TrackingNode extends BlockEntityNode {
        private int typeBehaviorCalls;

        private TrackingNode() {
            super(BlockPos.ZERO, TTBlocks.NODE.get().defaultBlockState());
        }

        @Override
        protected boolean allowTypeBehavior() {
            typeBehaviorCalls++;
            return true;
        }
    }
}
