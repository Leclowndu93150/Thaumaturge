package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.essentia.BellowsHelper;
import com.leclowndu93150.thaumaturge.content.essentia.storage.LabelledVesselActions;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.Lifecycle;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmelteryCutoffTest {
    private ServerLevel level;
    private RegistryAccess registries;
    private Holder<IAspect> order;
    private Holder<IAspect> vitium;
    private RandomSource random;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(aspects, TTAspects.ORDO, mock(IAspect.class));
        Registry.register(aspects, TTAspects.VITIUM, mock(IAspect.class));
        order = aspects.getHolderOrThrow(TTAspects.ORDO);
        vitium = aspects.getHolderOrThrow(TTAspects.VITIUM);
        registries = new RegistryAccess.ImmutableRegistryAccess(
                        Stream.concat(BuiltInRegistries.REGISTRY.stream(), Stream.of(aspects))
                                .toList())
                .freeze();
        level = mock(ServerLevel.class);
        random = mock(RandomSource.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.getRandom()).thenReturn(random);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        when(random.nextDouble()).thenReturn(0.5);
    }

    private BlockEntitySmelter smelter() {
        var machine = new BlockEntitySmelter(
                BlockPos.ZERO, TTBlocks.SMELTER_BASIC.get().defaultBlockState());
        machine.setLevel(level);
        return machine;
    }

    private void tick(BlockEntitySmelter machine, int count) {
        for (int i = 0; i < count; i++)
            BlockEntitySmelter.staticTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
    }

    private void input(BlockEntitySmelter machine, int count) {
        machine.itemSlots().setStackInSlot(BlockEntitySmelter.INPUT_SLOT, new ItemStack(Items.DIRT, count));
    }

    @Test
    void cookTargetChangesOnlyDuringFueledCookingAndProgressResetsWhenBlocked() {
        var machine = smelter();
        input(machine, 1);
        machine.cookTarget = 91;
        machine.cookElapsed = 2;
        try (var index = mockStatic(AspectIndexAccess.class);
                var bellows = mockStatic(BellowsHelper.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(order, 3));
            bellows.when(() -> BellowsHelper.countBellows(eq(level), eq(BlockPos.ZERO), any(Direction[].class)))
                    .thenReturn(2);
            tick(machine, 1);
            assertEquals(91, machine.cookTarget);
            assertEquals(0, machine.cookElapsed);
            machine.fuelRemaining = 20;
            tick(machine, 1);
            assertEquals(4, machine.cookTarget);
            assertEquals(1, machine.cookElapsed);
            machine.vis = 256;
            tick(machine, 1);
            assertEquals(4, machine.cookTarget);
            assertEquals(0, machine.cookElapsed);
            assertEquals(18, machine.fuelRemaining);
            assertEquals(1, machine.itemSlots().getStackInSlot(0).getCount());
        }
    }

    @Test
    void fullEfficiencyStillRollsVitiumRetentionAndConsumesExactlyOneInput() {
        var machine = spy(smelter());
        doReturn(1.0F).when(machine).essentiaYield();
        input(machine, 2);
        machine.fuelRemaining = 10;
        machine.cookElapsed = 3;
        when(random.nextFloat()).thenReturn(0.7F);
        try (var index = mockStatic(AspectIndexAccess.class);
                var aura = mockStatic(AuraHelper.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class)))
                    .thenReturn(AspectList.EMPTY.add(order, 1).add(vitium, 1));
            tick(machine, 1);
            assertEquals(1, machine.essentiaStock.amountOf(order));
            assertEquals(0, machine.essentiaStock.amountOf(vitium));
            assertEquals(1, machine.vis);
            assertEquals(1, machine.itemSlots().getStackInSlot(0).getCount());
            aura.verify(() -> AuraHelper.polluteAura(level, BlockPos.ZERO, 1.0F, true));
            verify(random, times(2)).nextFloat();
        }
    }

    @Test
    void survivorTallyAddsToExistingStockAndPollutesForEveryUnventedLoss() {
        var machine = spy(smelter());
        doReturn(0.8F).when(machine).essentiaYield();
        input(machine, 2);
        machine.essentiaStock = AspectList.EMPTY.add(order, 3);
        machine.vis = 3;
        machine.fuelRemaining = 10;
        machine.cookElapsed = 5;
        when(random.nextFloat()).thenReturn(0.1F, 0.6F, 0.9F);
        try (var index = mockStatic(AspectIndexAccess.class);
                var aura = mockStatic(AuraHelper.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class)))
                    .thenReturn(AspectList.EMPTY.add(order, 1).add(vitium, 2));
            tick(machine, 1);
            assertEquals(4, machine.vis);
            assertEquals(4, machine.essentiaStock.amountOf(order));
            assertEquals(0, machine.essentiaStock.amountOf(vitium));
            aura.verify(() -> AuraHelper.polluteAura(level, BlockPos.ZERO, 2.0F, true));
            assertEquals(1, machine.itemSlots().getStackInSlot(0).getCount());
            assertEquals(0, machine.cookElapsed);
        }
    }

    @Test
    void ventUsesCutoffPipeMouthThreePuffsUpwardMotionColorScaleAndSound() {
        var machine = spy(smelter());
        doReturn(0.8F).when(machine).essentiaYield();
        input(machine, 1);
        machine.fuelRemaining = 10;
        machine.cookElapsed = 1;
        when(random.nextFloat()).thenReturn(0.9F);
        when(level.getBlockState(BlockPos.ZERO.east()))
                .thenReturn(TTBlocks.SMELTER_VENT
                        .get()
                        .defaultBlockState()
                        .setValue(BlockSmelterVent.FACING, Direction.WEST));
        var mouth = new Vec3(1.375, 1.0, 0.5);
        try (var index = mockStatic(AspectIndexAccess.class);
                var effects = mockStatic(Effects.class);
                var aura = mockStatic(AuraHelper.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(order, 1));
            var vent = mock(Effects.Vent.class, RETURNS_SELF);
            effects.when(() -> Effects.vent(level, mouth)).thenReturn(vent);
            tick(machine, 1);
            assertEquals(0, machine.vis);
            aura.verifyNoInteractions();
            effects.verify(() -> Effects.vent(level, mouth), times(3));
            verify(vent, times(3)).motion(0.35, 1.0, 0.0);
            verify(vent, times(3)).color(0xC8C8CC);
            verify(vent, times(3)).scale(0.5F);
            verify(vent, times(3)).send();
            verify(level)
                    .playSound(
                            null,
                            mouth.x,
                            mouth.y,
                            mouth.z,
                            SoundEvents.LAVA_EXTINGUISH,
                            SoundSource.BLOCKS,
                            0.15F,
                            1.8F + 0.9F * 0.2F);
        }
    }

    @Test
    void frontVentsAndVentsPointingAwayCannotAbsorbLoss() {
        var machine = spy(smelter());
        doReturn(0.8F).when(machine).essentiaYield();
        input(machine, 1);
        machine.fuelRemaining = 10;
        machine.cookElapsed = 1;
        when(random.nextFloat()).thenReturn(0.99F);
        when(level.getBlockState(BlockPos.ZERO.north()))
                .thenReturn(TTBlocks.SMELTER_VENT
                        .get()
                        .defaultBlockState()
                        .setValue(BlockSmelterVent.FACING, Direction.SOUTH));
        when(level.getBlockState(BlockPos.ZERO.east()))
                .thenReturn(TTBlocks.SMELTER_VENT
                        .get()
                        .defaultBlockState()
                        .setValue(BlockSmelterVent.FACING, Direction.EAST));
        try (var index = mockStatic(AspectIndexAccess.class);
                var effects = mockStatic(Effects.class);
                var aura = mockStatic(AuraHelper.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(order, 1));
            tick(machine, 1);
            effects.verifyNoInteractions();
            aura.verify(() -> AuraHelper.polluteAura(level, BlockPos.ZERO, 1.0F, true));
            assertEquals(0, machine.vis);
        }
    }

    @Test
    void handoverFeedsMainAndEveryCorrectAuxColumnOnceOnFifteenTickCadence() {
        var machine = smelter();
        machine.essentiaStock = AspectList.EMPTY.add(order, 10).add(vitium, 1);
        machine.vis = 11;
        when(level.getBlockState(BlockPos.ZERO.east()))
                .thenReturn(TTBlocks.SMELTER_AUX
                        .get()
                        .defaultBlockState()
                        .setValue(BlockSmelterAux.FACING, Direction.WEST));
        when(level.getBlockState(BlockPos.ZERO.west()))
                .thenReturn(TTBlocks.SMELTER_AUX
                        .get()
                        .defaultBlockState()
                        .setValue(BlockSmelterAux.FACING, Direction.EAST));
        when(level.getBlockState(BlockPos.ZERO.north()))
                .thenReturn(TTBlocks.SMELTER_AUX
                        .get()
                        .defaultBlockState()
                        .setValue(BlockSmelterAux.FACING, Direction.SOUTH));
        try (var columns = mockStatic(BlockEntityAlembic.class)) {
            columns.when(() -> BlockEntityAlembic.feedColumn(eq(level), any(), any()))
                    .thenReturn(true);
            tick(machine, 14);
            columns.verifyNoInteractions();
            tick(machine, 1);
            columns.verify(() -> BlockEntityAlembic.feedColumn(level, BlockPos.ZERO, order));
            columns.verify(() -> BlockEntityAlembic.feedColumn(level, BlockPos.ZERO.east(), order));
            columns.verify(() -> BlockEntityAlembic.feedColumn(level, BlockPos.ZERO.west(), order));
            columns.verify(() -> BlockEntityAlembic.feedColumn(level, BlockPos.ZERO.north(), order), never());
            assertEquals(8, machine.vis);
            assertEquals(7, machine.essentiaStock.amountOf(order));
        }
    }

    @Test
    void alumentumUsesNativeFuelHookAndBoostsHandoverToTwelveTicks() {
        var machine = smelter();
        input(machine, 1);
        machine.essentiaStock = AspectList.EMPTY.add(order, 5);
        machine.vis = 5;
        machine.itemSlots().setStackInSlot(BlockEntitySmelter.FUEL_SLOT, new ItemStack(TTItems.ALUMENTUM.get()));
        try (var index = mockStatic(AspectIndexAccess.class);
                var fuel = mockStatic(EventHooks.class);
                var columns = mockStatic(BlockEntityAlembic.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(order, 100));
            fuel.when(() -> EventHooks.getItemBurnTime(any(ItemStack.class), anyInt(), eq(RecipeType.SMELTING)))
                    .thenReturn(200);
            columns.when(() -> BlockEntityAlembic.feedColumn(level, BlockPos.ZERO, order))
                    .thenReturn(true);
            tick(machine, 11);
            columns.verifyNoInteractions();
            assertEquals(200, machine.fuelCapacity);
            assertTrue(machine.getUpdateTag(registries).getBoolean("SpeedBoost"));
            assertTrue(machine.itemSlots().getStackInSlot(1).isEmpty());
            tick(machine, 1);
            columns.verify(() -> BlockEntityAlembic.feedColumn(level, BlockPos.ZERO, order));
            assertEquals(4, machine.vis);
        }
    }

    @Test
    void nativeInventoryAndAllFurnaceKeysRoundTripAndClientEntryDoesNothing() {
        var machine = smelter();
        input(machine, 3);
        machine.itemSlots().setStackInSlot(1, new ItemStack(Items.COAL, 4));
        machine.fuelCapacity = 20;
        machine.fuelRemaining = 16;
        machine.cookElapsed = 2;
        machine.cookTarget = 8;
        machine.essentiaStock = AspectList.EMPTY.add(order, 11);
        var tag = machine.getUpdateTag(registries);
        assertEquals(
                Set.of(
                        "Aspects",
                        "BurnTime",
                        "CookTime",
                        "SmeltTime",
                        "CurrentItemBurnTime",
                        "SpeedBoost",
                        "inventory"),
                tag.getAllKeys());
        var loaded = smelter();
        loaded.loadAdditional(tag, registries);
        assertEquals(tag, loaded.getUpdateTag(registries));
        assertEquals(11, loaded.vis);
        assertEquals(3, loaded.itemSlots().getStackInSlot(0).getCount());
        var client = mock(Level.class);
        BlockEntitySmelter.staticTick(client, BlockPos.ZERO, machine.getBlockState(), machine);
        verifyNoInteractions(client);
        assertEquals(16, machine.fuelRemaining);
        assertEquals(2, machine.cookElapsed);
        assertEquals(
                RenderShape.MODEL,
                ((BlockSmelter) machine.getBlockState().getBlock()).getRenderShape(machine.getBlockState()));
        var alembic = (BlockAlembic) TTBlocks.ALEMBIC.get();
        assertEquals(RenderShape.MODEL, alembic.getRenderShape(alembic.defaultBlockState()));
    }

    @Test
    void nativeRemovalDropsBothSlotsAndFluxOnlyWhenBlockActuallyChanges() {
        var machine = smelter();
        input(machine, 2);
        machine.itemSlots().setStackInSlot(1, new ItemStack(Items.COAL));
        machine.essentiaStock = AspectList.EMPTY.add(order, 5);
        machine.vis = 5;
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(machine);
        var block = (BlockSmelter) machine.getBlockState().getBlock();
        try (var containers = mockStatic(Containers.class);
                var aura = mockStatic(AuraHelper.class)) {
            machine.setRemoved();
            block.onRemove(
                    machine.getBlockState(),
                    level,
                    BlockPos.ZERO,
                    machine.getBlockState().setValue(BlockSmelter.LIT, true),
                    false);
            containers.verifyNoInteractions();
            aura.verifyNoInteractions();
            block.onRemove(machine.getBlockState(), level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
            containers.verify(() -> Containers.dropItemStack(
                    eq(level),
                    eq(0.0),
                    eq(0.0),
                    eq(0.0),
                    argThat(stack -> stack.is(Items.DIRT) && stack.getCount() == 2)));
            containers.verify(() -> Containers.dropItemStack(
                    eq(level), eq(0.0), eq(0.0), eq(0.0), argThat(stack -> stack.is(Items.COAL))));
            aura.verify(() -> AuraHelper.polluteAura(level, BlockPos.ZERO, 5.0F, true));
            assertTrue(machine.itemSlots().getStackInSlot(0).isEmpty());
            assertTrue(machine.itemSlots().getStackInSlot(1).isEmpty());
            assertEquals(0, machine.vis);
            assertTrue(machine.essentiaStock.isEmpty());
            verify(level, atLeastOnce())
                    .updateNeighbourForOutputSignal(
                            BlockPos.ZERO, machine.getBlockState().getBlock());
        }
    }

    @Test
    void alembicConnectionsUseDirectConduitLookupOnlyInLoadedHorizontalNeighbors() {
        var block = (BlockAlembic) TTBlocks.ALEMBIC.get();
        var context = mock(BlockPlaceContext.class);
        when(context.getLevel()).thenReturn(level);
        when(context.getClickedPos()).thenReturn(BlockPos.ZERO);
        var north = mock(IEssentiaConduit.class);
        var east = mock(IEssentiaConduit.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.north(), Direction.SOUTH))
                .thenReturn(north);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.east(), Direction.WEST))
                .thenReturn(east);
        when(north.connectsOn(Direction.SOUTH)).thenReturn(true);
        when(level.hasChunk(-1, 0)).thenReturn(false);
        var state = block.getStateForPlacement(context);
        assertTrue(state.getValue(BlockAlembic.NORTH));
        assertFalse(state.getValue(BlockAlembic.EAST));
        assertFalse(state.getValue(BlockAlembic.SOUTH));
        assertFalse(state.getValue(BlockAlembic.WEST));
        verify(level).getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.north(), Direction.SOUTH);
        verify(level, never()).getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.west(), Direction.EAST);
        when(north.connectsOn(Direction.SOUTH)).thenReturn(false);
        assertFalse(block.updateShape(
                        state,
                        Direction.NORTH,
                        Blocks.AIR.defaultBlockState(),
                        level,
                        BlockPos.ZERO,
                        BlockPos.ZERO.north())
                .getValue(BlockAlembic.NORTH));
        assertSame(
                state,
                block.updateShape(
                        state,
                        Direction.UP,
                        Blocks.AIR.defaultBlockState(),
                        level,
                        BlockPos.ZERO,
                        BlockPos.ZERO.above()));
    }

    @Test
    void nativeServerSuccessPreservesSwingForDeniedSmelterAndAlembicPouring() {
        var block = (BlockSmelter) TTBlocks.SMELTER_BASIC.get();
        var player = mock(Player.class);
        var hit = new BlockHitResult(Vec3.atCenterOf(BlockPos.ZERO), Direction.UP, BlockPos.ZERO, false);
        try (var gate = mockStatic(DeviceGate.class)) {
            gate.when(() -> DeviceGate.passes(player, TTIds.rl("essentia_smelter")))
                    .thenReturn(false);
            assertEquals(
                    InteractionResult.SUCCESS,
                    block.useWithoutItem(block.defaultBlockState(), level, BlockPos.ZERO, player, hit));
        }
        var alembic =
                new BlockEntityAlembic(BlockPos.ZERO, TTBlocks.ALEMBIC.get().defaultBlockState());
        alembic.setLevel(level);
        alembic.fill(order, 3);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(alembic);
        when(player.isShiftKeyDown()).thenReturn(true);
        var alembicBlock = (BlockAlembic) TTBlocks.ALEMBIC.get();
        try (var labels = mockStatic(LabelledVesselActions.class)) {
            assertEquals(
                    InteractionResult.SUCCESS,
                    alembicBlock.useWithoutItem(alembic.getBlockState(), level, BlockPos.ZERO, player, hit));
            labels.verify(() -> LabelledVesselActions.pourOut(level, BlockPos.ZERO, 3));
            assertEquals(0, alembic.amount());
        }
    }

    @Test
    void nativeAuxAndVentCallbacksPreservePlacementAndAttachmentRules() {
        var aux = TTBlocks.SMELTER_AUX.get();
        var vent = TTBlocks.SMELTER_VENT.get();
        var context = mock(BlockPlaceContext.class);
        when(context.getClickedFace()).thenReturn(Direction.UP);
        assertNull(aux.getStateForPlacement(context));
        when(context.getClickedFace()).thenReturn(Direction.WEST);
        when(context.getHorizontalDirection()).thenReturn(Direction.EAST);
        var auxState = aux.getStateForPlacement(context);
        var ventState = vent.getStateForPlacement(context);
        assertEquals(Direction.EAST, auxState.getValue(BlockSmelterAux.FACING));
        assertEquals(Direction.EAST, ventState.getValue(BlockSmelterVent.FACING));
        when(level.getBlockState(BlockPos.ZERO.east()))
                .thenReturn(TTBlocks.SMELTER_BASIC
                        .get()
                        .defaultBlockState()
                        .setValue(BlockSmelter.FACING, Direction.NORTH));
        assertTrue(aux.canSurvive(auxState, level, BlockPos.ZERO));
        assertTrue(vent.canSurvive(ventState, level, BlockPos.ZERO));
        when(level.getBlockState(BlockPos.ZERO.east()))
                .thenReturn(
                        TTBlocks.SMELTER_BASIC.get().defaultBlockState().setValue(BlockSmelter.FACING, Direction.WEST));
        assertFalse(aux.canSurvive(auxState, level, BlockPos.ZERO));
        assertTrue(aux.updateShape(
                        auxState,
                        Direction.EAST,
                        Blocks.AIR.defaultBlockState(),
                        level,
                        BlockPos.ZERO,
                        BlockPos.ZERO.east())
                .isAir());
        assertTrue(vent.updateShape(
                        ventState,
                        Direction.EAST,
                        Blocks.AIR.defaultBlockState(),
                        level,
                        BlockPos.ZERO,
                        BlockPos.ZERO.east())
                .isAir());
    }

    @Test
    void nativeMenuKeepsTwoSlotsPredicateCoordinatesAndInteractionRange() {
        var machine = smelter();
        var player = mock(Player.class);
        when(player.level()).thenReturn(level);
        when(player.canInteractWithBlock(BlockPos.ZERO, 4.0)).thenReturn(true);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(machine.getBlockState());
        var menu = new MenuSmelter(1, new Inventory(player), machine);
        assertSame(machine.itemSlots(), menu.items());
        assertEquals(80, menu.getSlot(0).x);
        assertEquals(7, menu.getSlot(0).y);
        assertEquals(80, menu.getSlot(1).x);
        assertEquals(47, menu.getSlot(1).y);
        assertTrue(menu.stillValid(player));
        when(player.canInteractWithBlock(BlockPos.ZERO, 4.0)).thenReturn(false);
        assertFalse(menu.stillValid(player));
        try (var index = mockStatic(AspectIndexAccess.class);
                var fuel = mockStatic(EventHooks.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class)))
                    .thenAnswer(call -> ((ItemStack) call.getArgument(0)).is(Items.DIRT)
                            ? AspectList.EMPTY.add(order, 1)
                            : AspectList.EMPTY);
            fuel.when(() -> EventHooks.getItemBurnTime(any(ItemStack.class), anyInt(), eq(RecipeType.SMELTING)))
                    .thenAnswer(call -> ((ItemStack) call.getArgument(0)).is(Items.COAL) ? 200 : 0);
            assertTrue(menu.getSlot(0).mayPlace(new ItemStack(Items.DIRT)));
            assertFalse(menu.getSlot(0).mayPlace(new ItemStack(Items.COAL)));
            assertTrue(menu.getSlot(1).mayPlace(new ItemStack(Items.COAL)));
            assertFalse(menu.getSlot(1).mayPlace(new ItemStack(Items.DIRT)));
        }
    }
}
