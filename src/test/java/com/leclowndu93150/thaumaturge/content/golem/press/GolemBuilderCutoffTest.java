package com.leclowndu93150.thaumaturge.content.golem.press;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.api.golems.parts.*;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.registry.*;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GolemBuilderCutoffTest {
    private ServerLevel level;
    private RegistryAccess registries;
    private RandomSource random;
    private Player player;
    private Holder<IAspect> machina;
    private Holder<IAspect> order;
    private final Map<BlockPos, IEssentiaConduit> peers = new HashMap<>();

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(aspects, TTAspects.MACHINA, mock(IAspect.class));
        Registry.register(aspects, TTAspects.ORDO, mock(IAspect.class));
        machina = aspects.getHolderOrThrow(TTAspects.MACHINA);
        order = aspects.getHolderOrThrow(TTAspects.ORDO);
        registries = new RegistryAccess.ImmutableRegistryAccess(Stream.concat(
                                BuiltInRegistries.REGISTRY.stream(),
                                Stream.of(
                                        aspects,
                                        TTGolemParts.materials(),
                                        TTGolemParts.heads(),
                                        TTGolemParts.arms(),
                                        TTGolemParts.legs(),
                                        TTGolemParts.addons(),
                                        TTGolemTraits.registry()))
                        .distinct()
                        .toList())
                .freeze();
        level = mock(ServerLevel.class);
        player = mock(Player.class);
        random = mock(RandomSource.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getRandom()).thenReturn(random);
        when(random.nextFloat()).thenReturn(0.5F);
        when(random.nextDouble()).thenReturn(0.5D);
        when(random.nextInt(anyInt())).thenReturn(1);
        when(player.level()).thenReturn(level);
        when(level.getCapability(eq(EssentiaCapabilities.CONDUIT), any(BlockPos.class), any(Direction.class)))
                .thenAnswer(call -> peers.get(call.getArgument(1)));
    }

    private BlockEntityGolemBuilder builder() {
        BlockEntityGolemBuilder builder = spy(new BlockEntityGolemBuilder(
                BlockPos.ZERO, TTBlocks.GOLEM_BUILDER.get().defaultBlockState()));
        builder.setLevel(level);
        return builder;
    }

    // These mocks isolate the press contract; inventory rollback is a separate shared-helper concern.
    private boolean begin(BlockEntityGolemBuilder builder, GolemProperties design, boolean inputsAvailable) {
        PlayerResearch research = mock(PlayerResearch.class);
        when(research.hasCompleted(any())).thenReturn(true);
        try (var access = mockStatic(PlayerResearchAccess.class);
                var inventory = mockStatic(InvHelper.class)) {
            access.when(() -> PlayerResearchAccess.of(player)).thenReturn(research);
            inventory
                    .when(() -> InvHelper.takeFromNeighboursOrPlayer(
                            eq(level), eq(BlockPos.ZERO), eq(player), eq(true), any(ItemStack[].class)))
                    .thenReturn(inputsAvailable);
            inventory
                    .when(() -> InvHelper.takeFromNeighboursOrPlayer(
                            eq(level), eq(BlockPos.ZERO), eq(player), eq(false), any(ItemStack[].class)))
                    .thenReturn(true);
            boolean started = builder.beginAssembly(design, player);
            inventory.verify(
                    () -> InvHelper.takeFromNeighboursOrPlayer(
                            eq(level), eq(BlockPos.ZERO), eq(player), eq(false), any(ItemStack[].class)),
                    times(started ? 1 : 0));
            return started;
        }
    }

    private BlockEntityGolemBuilder working() {
        BlockEntityGolemBuilder builder = builder();
        assertTrue(begin(builder, GolemProperties.createDefault().withRank(6), true));
        return builder;
    }

    private BlockEntityGolemBuilder lastUnit() {
        BlockEntityGolemBuilder builder = working();
        CompoundTag saved = builder.getUpdateTag(registries);
        saved.putInt("cost", 1);
        builder.loadAdditional(saved, registries);
        return builder;
    }

    private void tick(BlockEntityGolemBuilder builder, int times) {
        for (int i = 0; i < times; i++) {
            BlockEntityGolemBuilder.serverTick(level, BlockPos.ZERO, builder.getBlockState(), builder);
        }
    }

    private ItemStack placer(GolemProperties props, int count) {
        ItemStack stack = new ItemStack(TTItems.GOLEM_PLACER.get(), count);
        stack.set(TTDataComponents.GOLEM_PROPERTIES.get(), props);
        return stack;
    }

    private IEssentiaConduit source(Direction side, boolean releases) {
        Direction face = side.getOpposite();
        IEssentiaConduit source = mock(IEssentiaConduit.class);
        when(source.connectsOn(face)).thenReturn(true);
        when(source.releasesOn(face)).thenReturn(releases);
        when(source.pull(face)).thenReturn(EssentiaPull.NONE);
        when(source.extract(machina, 1, face, false)).thenReturn(1);
        peers.put(BlockPos.ZERO.relative(side), source);
        return source;
    }

    @Test
    void assemblyUsesOriginalBillAndTraitCostButProducesRankZeroDesign() {
        GolemProperties original = GolemProperties.createDefault().withRank(6);
        BlockEntityGolemBuilder builder = builder();
        assertTrue(begin(builder, original, true));
        int expected =
                original.components().stream().mapToInt(ItemStack::getCount).sum()
                        + original.traits().size() * 2;
        assertEquals(expected, builder.cost());
        assertEquals(expected, builder.totalCost());
        assertEquals(original.withRank(0), builder.pendingGolem());
        assertEquals(6, original.rank());
        verify(level).playSound(null, BlockPos.ZERO, TTSounds.WAND.get(), SoundSource.BLOCKS, 0.25F, 1.0F);
    }

    @Test
    void missingComponentsRejectJobWithoutCommittingInputs() {
        BlockEntityGolemBuilder builder = builder();
        assertFalse(begin(builder, GolemProperties.createDefault(), false));
        assertNull(builder.pendingGolem());
        assertEquals(0, builder.cost());
        assertEquals(0, builder.totalCost());
    }

    @Test
    void unknownResearchAndClientOrDetachedBuilderCannotStartAJob() {
        BlockEntityGolemBuilder builder = builder();
        PlayerResearch research = mock(PlayerResearch.class);
        GolemProperties props = mock(GolemProperties.class);
        when(props.isKnownBy(research)).thenReturn(false);
        try (var access = mockStatic(PlayerResearchAccess.class);
                var inventory = mockStatic(InvHelper.class)) {
            access.when(() -> PlayerResearchAccess.of(player)).thenReturn(research);
            assertFalse(builder.beginAssembly(props, player));
            inventory.verifyNoInteractions();
            when(level.isClientSide()).thenReturn(true);
            assertFalse(builder.beginAssembly(props, player));
            BlockEntityGolemBuilder detached = new BlockEntityGolemBuilder(
                    BlockPos.ZERO, TTBlocks.GOLEM_BUILDER.get().defaultBlockState());
            assertFalse(detached.beginAssembly(props, player));
        }
        assertNull(builder.pendingGolem());
    }

    @Test
    void stockedComponentPreviewUsesAdjacentInventoryOnlyAndKeepsBillOrder() {
        BlockEntityGolemBuilder builder = builder();
        GolemProperties props = mock(GolemProperties.class);
        ItemStack dirt = new ItemStack(Items.DIRT, 2);
        ItemStack stone = new ItemStack(Items.STONE, 3);
        when(props.components()).thenReturn(List.of(dirt, stone));
        try (var inventory = mockStatic(InvHelper.class)) {
            inventory
                    .when(() -> InvHelper.neighboursHold(level, BlockPos.ZERO, dirt))
                    .thenReturn(true);
            assertArrayEquals(new boolean[] {true, false}, builder.stockedComponents(props));
            inventory.verify(() -> InvHelper.neighboursHold(level, BlockPos.ZERO, dirt));
            inventory.verify(() -> InvHelper.neighboursHold(level, BlockPos.ZERO, stone));
        }
        BlockEntityGolemBuilder detached = new BlockEntityGolemBuilder(
                BlockPos.ZERO, TTBlocks.GOLEM_BUILDER.get().defaultBlockState());
        assertArrayEquals(new boolean[] {false, false}, detached.stockedComponents(props));
    }

    @Test
    void simulationAcceptsExactlyOneMachinaWithoutMutationAndPreservesMainFaceQuirk() {
        BlockEntityGolemBuilder builder = working();
        clearInvocations(builder, level);
        assertFalse(builder.connectsOn(Direction.UP));
        assertFalse(builder.receivesOn(Direction.UP));
        assertTrue(builder.connectsOn(null));
        assertEquals(1, builder.insert(machina, 50, Direction.UP, true));
        assertEquals(1, builder.insert(machina, 50, null, true));
        assertEquals(1, builder.roomFor(machina, Direction.UP));
        assertEquals(0, builder.insert(order, 1, Direction.DOWN, true));
        assertEquals(0, builder.insert(machina, 0, Direction.DOWN, true));
        assertEquals(0, builder.insert(machina, -1, Direction.DOWN, true));
        assertEquals(0, builder.extract(machina, 50, null, true));
        assertEquals(0, builder.heldAmount(null));
        assertNull(builder.heldAspect(null));
        assertEquals(EssentiaPull.of(machina, 128), builder.pull(null));
        verify(builder, never()).setChanged();
        verify(builder, never()).syncToClient();
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
        assertEquals(1, builder.insert(machina, 50, Direction.UP, false));
        assertEquals(0, builder.roomFor(machina, null));
        assertEquals(0, builder.insert(machina, 1, Direction.DOWN, false));
    }

    @Test
    void firstCycleIsImmediateThenEveryFiveTicksAndBufferPaysOnlyOneUnit() {
        BlockEntityGolemBuilder builder = working();
        int before = builder.cost();
        builder.insert(machina, 1, Direction.DOWN, false);
        tick(builder, 1);
        assertEquals(before - 1, builder.cost());
        builder.insert(machina, 1, Direction.DOWN, false);
        tick(builder, 4);
        assertEquals(before - 1, builder.cost());
        tick(builder, 1);
        assertEquals(before - 2, builder.cost());
        tick(builder, 5);
        assertEquals(before - 2, builder.cost());
    }

    @Test
    void intakeSkipsNonOutputAndUpAndIgnoresSourceThresholdAsMainDoes() {
        BlockEntityGolemBuilder builder = working();
        int before = builder.cost();
        IEssentiaConduit down = source(Direction.DOWN, false);
        IEssentiaConduit up = source(Direction.UP, true);
        IEssentiaConduit north = source(Direction.NORTH, true);
        when(north.pullThreshold()).thenReturn(200);
        tick(builder, 1);
        assertEquals(before - 1, builder.cost());
        verify(down, never()).extract(any(), anyInt(), any(), anyBoolean());
        verifyNoInteractions(up);
        verify(north).extract(machina, 1, Direction.SOUTH, false);
        verify(north, never()).pullThreshold();
    }

    @Test
    void intakeNeverQueriesAnUnloadedNeighborAndRejectsEqualSuction() {
        BlockEntityGolemBuilder builder = working();
        int before = builder.cost();
        IEssentiaConduit down = source(Direction.DOWN, true);
        IEssentiaConduit north = source(Direction.NORTH, true);
        when(level.hasChunk(0, 0)).thenReturn(false);
        when(north.pull(Direction.SOUTH)).thenReturn(EssentiaPull.of(machina, 128));
        tick(builder, 1);
        assertEquals(before, builder.cost());
        verifyNoInteractions(down);
        verify(level, never()).getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.below(), Direction.UP);
        verify(north, never()).extract(any(), anyInt(), any(), anyBoolean());
    }

    @Test
    void blockedCompletedJobRetriesWithoutFurtherEssentiaAndCannotBeOverwritten() {
        BlockEntityGolemBuilder builder = lastUnit();
        builder.outputHandler().setStackInSlot(0, new ItemStack(Items.DIRT));
        builder.insert(machina, 1, Direction.DOWN, false);
        tick(builder, 1);
        assertEquals(0, builder.cost());
        assertNotNull(builder.pendingGolem());
        assertSame(EssentiaPull.NONE, builder.pull(null));
        assertEquals(0, builder.insert(machina, 1, null, false));
        assertFalse(begin(builder, GolemProperties.createDefault(), true));
        builder.outputHandler().setStackInSlot(0, ItemStack.EMPTY);
        tick(builder, 5);
        assertNull(builder.pendingGolem());
        assertEquals(0, builder.totalCost());
        assertEquals(
                GolemProperties.createDefault(),
                builder.outputHandler().getStackInSlot(0).get(TTDataComponents.GOLEM_PROPERTIES.get()));
        verify(level).playSound(null, BlockPos.ZERO, TTSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    @Test
    void compatibleOutputStacksExactlyOneAndFullOrDifferentDesignRejectsStart() {
        BlockEntityGolemBuilder builder = lastUnit();
        builder.outputHandler().setStackInSlot(0, placer(GolemProperties.createDefault(), 2));
        builder.insert(machina, 1, null, false);
        tick(builder, 1);
        assertEquals(3, builder.outputHandler().getStackInSlot(0).getCount());
        ItemStack full = placer(GolemProperties.createDefault(), 1);
        full.setCount(full.getMaxStackSize());
        builder.outputHandler().setStackInSlot(0, full);
        assertFalse(begin(builder, GolemProperties.createDefault(), true));
        builder.outputHandler()
                .setStackInSlot(0, placer(GolemProperties.createDefault().withRank(1), 1));
        assertFalse(begin(builder, GolemProperties.createDefault(), true));
        assertTrue(builder.outputHandler().isItemValid(0, full));
        assertFalse(builder.outputHandler().isItemValid(0, new ItemStack(Items.DIRT)));
    }

    @Test
    void nativeSaveRetainsExistingSchemaAndJobButNotTransientIntakeBuffer() {
        BlockEntityGolemBuilder original = working();
        original.outputHandler().setStackInSlot(0, placer(GolemProperties.createDefault(), 2));
        original.insert(machina, 1, null, false);
        CompoundTag saved = original.getUpdateTag(registries);
        assertEquals(Set.of("inventory", "golem", "cost", "mcost"), saved.getAllKeys());
        BlockEntityGolemBuilder loaded = builder();
        loaded.loadAdditional(saved, registries);
        assertEquals(saved, loaded.getUpdateTag(registries));
        assertEquals(original.pendingGolem(), loaded.pendingGolem());
        assertEquals(original.cost(), loaded.cost());
        assertEquals(1, loaded.roomFor(machina, null));
        tick(loaded, 1);
        assertEquals(original.cost(), loaded.cost());
    }

    @Test
    void descentHissStartsAboveSixtyAndUsesExactNativeSoundAndVentValues() {
        BlockEntityGolemBuilder builder = working();
        builder.press = 54;
        clearInvocations(level);
        BlockEntityGolemBuilder.clientTick(level, BlockPos.ZERO, builder.getBlockState(), builder);
        assertEquals(60, builder.press);
        verify(level, never())
                .addParticle(any(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        BlockEntityGolemBuilder.clientTick(level, BlockPos.ZERO, builder.getBlockState(), builder);
        assertEquals(66, builder.press);
        verify(level)
                .playLocalSound(0.5D, 0.5D, 0.5D, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.35F, 0.9F, false);
        ArgumentCaptor<VentParticleOptions> vents = ArgumentCaptor.forClass(VentParticleOptions.class);
        verify(level, times(5))
                .addParticle(vents.capture(), eq(0.5D), eq(2.0D), eq(0.5D), eq(0.0D), eq(0.0D), eq(0.0D));
        assertTrue(vents.getAllValues().stream()
                .allMatch(vent -> vent.scale() == 1.6F
                        && vent.color() == 0xCFCFD4
                        && vent.vx() == 0.0D
                        && vent.vy() == 1.0D
                        && vent.vz() == 0.0D
                        && !vent.variant()));
    }

    @Test
    void workingBottomPressVentsOnlyOnOneInSevenRoll() {
        BlockEntityGolemBuilder builder = working();
        builder.press = 90;
        clearInvocations(level);
        BlockEntityGolemBuilder.clientTick(level, BlockPos.ZERO, builder.getBlockState(), builder);
        verify(level, never())
                .addParticle(any(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        when(random.nextInt(7)).thenReturn(0);
        BlockEntityGolemBuilder.clientTick(level, BlockPos.ZERO, builder.getBlockState(), builder);
        assertEquals(90, builder.press);
        verify(level)
                .playLocalSound(0.5D, 0.5D, 0.5D, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.04F, 0.9F, false);
        verify(level)
                .addParticle(
                        argThat(particle -> particle instanceof VentParticleOptions vent
                                && vent.scale() == 1.0F
                                && vent.color() == 0xCFCFD4),
                        eq(0.5D),
                        eq(2.0D),
                        eq(0.5D),
                        eq(0.0D),
                        eq(0.0D),
                        eq(0.0D));
    }

    @Test
    void settledPressVentsFourteenOnceAndRetractsThreeWithoutIdleHiss() {
        BlockEntityGolemBuilder builder = builder();
        builder.press = 90;
        clearInvocations(level);
        BlockEntityGolemBuilder.clientTick(level, BlockPos.ZERO, builder.getBlockState(), builder);
        assertEquals(87, builder.press);
        BlockEntityGolemBuilder.clientTick(level, BlockPos.ZERO, builder.getBlockState(), builder);
        assertEquals(84, builder.press);
        ArgumentCaptor<VentParticleOptions> vents = ArgumentCaptor.forClass(VentParticleOptions.class);
        verify(level, times(14))
                .addParticle(vents.capture(), anyDouble(), eq(2.0D), anyDouble(), eq(0.0D), eq(0.0D), eq(0.0D));
        assertTrue(vents.getAllValues().stream().allMatch(vent -> vent.scale() == 2.0F));
        verify(level, never())
                .playLocalSound(
                        anyDouble(), anyDouble(), anyDouble(), any(), any(), anyFloat(), anyFloat(), anyBoolean());
        assertEquals(new AABB(-1, 0, -1, 2, 2, 2), builder.renderBounds());
    }

    @Test
    void menuUsesNativeHandlerAndReplicatesBothCostDataSlots() {
        BlockEntityGolemBuilder builder = working();
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(builder);
        MenuGolemBuilder menu = new MenuGolemBuilder(7, new Inventory(player), builder);
        assertSame(builder, menu.blockEntity());
        assertTrue(menu.getSlot(0).mayPlace(new ItemStack(TTItems.GOLEM_PLACER.get())));
        assertFalse(menu.getSlot(0).mayPlace(new ItemStack(Items.DIRT)));
        menu.broadcastChanges();
        assertEquals(builder.cost(), menu.cost());
        assertEquals(builder.totalCost(), menu.maxCost());
    }

    @Test
    void partResearchUsesCompletedEntriesAndShortCircuitsAtFirstUnknownPart() {
        ResourceLocation first = ResourceLocation.fromNamespaceAndPath("thaumaturge", "first");
        ResourceLocation second = first.withSuffix("_second");
        ResourceLocation later = first.withSuffix("_later");
        GolemMaterial material = mock(GolemMaterial.class);
        GolemHead head = mock(GolemHead.class);
        GolemArm arms = mock(GolemArm.class);
        GolemLeg legs = mock(GolemLeg.class);
        GolemAddon addon = mock(GolemAddon.class);
        when(material.research()).thenReturn(List.of(first));
        when(head.research()).thenReturn(List.of(second));
        when(arms.research()).thenReturn(List.of(later));
        when(legs.research()).thenReturn(List.of());
        when(addon.research()).thenReturn(List.of());
        GolemProperties props = new GolemProperties(material, head, arms, legs, addon, 0);
        PlayerResearch research = mock(PlayerResearch.class);
        when(research.hasCompleted(first)).thenReturn(true);
        assertFalse(props.isKnownBy(research));
        verify(research).hasCompleted(first);
        verify(research).hasCompleted(second);
        verify(research, never()).hasCompleted(later);
        when(research.hasCompleted(second)).thenReturn(true);
        when(research.hasCompleted(later)).thenReturn(true);
        assertTrue(props.isKnownBy(research));
    }
}
