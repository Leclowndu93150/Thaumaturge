package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.essentia.BellowsHelper;
import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockEntityBellows;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.EventHooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmelteryCompatibilityTest {
    private ServerLevel level;
    private RegistryAccess registries;
    private Holder<IAspect> order;
    private Holder<IAspect> fire;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.ORDO, mock(IAspect.class));
        Registry.register(registry, TTAspects.IGNIS, mock(IAspect.class));
        order = registry.getHolderOrThrow(TTAspects.ORDO);
        fire = registry.getHolderOrThrow(TTAspects.IGNIS);
        registries = new RegistryAccess.ImmutableRegistryAccess(
                        Stream.concat(BuiltInRegistries.REGISTRY.stream(), Stream.of(registry))
                                .toList())
                .freeze();
        level = mock(ServerLevel.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getRandom()).thenReturn(mock(RandomSource.class));
    }

    private BlockEntityAlembic alembic(BlockPos pos) {
        BlockEntityAlembic alembic =
                new BlockEntityAlembic(pos, TTBlocks.ALEMBIC.get().defaultBlockState());
        alembic.setLevel(level);
        return alembic;
    }

    private BlockEntitySmelter smelter() {
        BlockEntitySmelter smelter = new BlockEntitySmelter(
                BlockPos.ZERO, TTBlocks.SMELTER_BASIC.get().defaultBlockState());
        smelter.setLevel(level);
        return smelter;
    }

    @Test
    void facingInvalidatesCapabilitiesOnlyOnAnActualChange() {
        BlockEntityAlembic alembic = alembic(BlockPos.ZERO);
        alembic.aimSpout(Direction.DOWN);
        verify(level, never()).invalidateCapabilities(any(BlockPos.class));
        alembic.fill(order, 7);
        alembic.aimSpout(Direction.NORTH);
        verify(level).invalidateCapabilities(BlockPos.ZERO);
        alembic.aimSpout(Direction.NORTH);
        verify(level, times(1)).invalidateCapabilities(BlockPos.ZERO);
        assertEquals(7, alembic.amount());
        assertEquals(TTAspects.ORDO, alembic.aspectKey());
        assertFalse(alembic.releasesOn(Direction.NORTH));
        assertFalse(alembic.releasesOn(Direction.DOWN));
        assertTrue(alembic.releasesOn(Direction.UP));
    }

    @Test
    void alembicStorageIsExtractionOnlyAndTracksDirectMutations() {
        BlockEntityAlembic alembic = alembic(BlockPos.ZERO);
        var storage = alembic.storage(Direction.UP);
        assertEquals(0, storage.insert(order, 4, false));
        assertEquals(0, alembic.insert(order, 4, Direction.UP, false));
        assertEquals(0, alembic.fill(order, 7));
        assertEquals(1, storage.contentRevision());
        assertEquals(2, storage.extract(order, 2, true));
        assertEquals(1, storage.contentRevision());
        assertEquals(2, storage.extract(order, 2, false));
        assertEquals(2, storage.contentRevision());
        assertEquals(5, alembic.amount());
    }

    @Test
    void columnPrefersMatchingStockOverAnEarlierEmptyAlembic() {
        BlockEntityAlembic empty = alembic(BlockPos.ZERO.above());
        BlockEntityAlembic stocked = alembic(BlockPos.ZERO.above(2));
        stocked.fill(order, 2);
        when(level.getBlockEntity(BlockPos.ZERO.above())).thenReturn(empty);
        when(level.getBlockEntity(BlockPos.ZERO.above(2))).thenReturn(stocked);
        assertTrue(BlockEntityAlembic.feedColumn(level, BlockPos.ZERO, order));
        assertEquals(0, empty.amount());
        assertEquals(3, stocked.amount());
        assertTrue(BlockEntityAlembic.feedColumn(level, BlockPos.ZERO, fire));
        assertEquals(1, empty.amount());
        assertEquals(TTAspects.IGNIS, empty.aspectKey());
    }

    @Test
    void columnDoesNotReadAnUnloadedChunk() {
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);
        assertFalse(BlockEntityAlembic.feedColumn(level, BlockPos.ZERO, order));
        verify(level, never()).getBlockEntity(any());
    }

    @Test
    void alembicNbtKeysAndLabelsRoundTripWithClampedContents() {
        BlockEntityAlembic original = alembic(BlockPos.ZERO);
        original.fill(order, 9);
        original.setAspectFilter(TTAspects.ORDO);
        original.aimSpout(Direction.EAST);
        CompoundTag tag = original.getUpdateTag(registries);
        assertEquals(Set.of("Aspect", "AspectFilter", "Amount", "Facing"), tag.getAllKeys());
        BlockEntityAlembic loaded = alembic(BlockPos.ZERO);
        loaded.loadAdditional(tag, registries);
        assertEquals(tag, loaded.getUpdateTag(registries));
        assertEquals(0, loaded.storage(Direction.UP).contentRevision());
        tag.putInt("Amount", Integer.MAX_VALUE);
        loaded.loadAdditional(tag, registries);
        assertEquals(BlockEntityAlembic.CAPACITY, loaded.amount());
    }

    @Test
    void smelterPreservesNativeInventoryAndFurnaceNbt() {
        BlockEntitySmelter original = smelter();
        original.fuelRemaining = 16;
        original.fuelCapacity = 20;
        original.cookElapsed = 2;
        original.cookTarget = 8;
        original.essentiaStock = AspectList.EMPTY.add(order, 11);
        original.itemSlots().setStackInSlot(0, new ItemStack(Items.DIRT, 3));
        original.itemSlots().setStackInSlot(1, new ItemStack(Items.COAL, 4));
        CompoundTag tag = original.getUpdateTag(registries);
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
        BlockEntitySmelter loaded = smelter();
        loaded.loadAdditional(tag, registries);
        assertEquals(tag, loaded.getUpdateTag(registries));
        assertEquals(11, loaded.vis);
        assertEquals(3, loaded.itemSlots().getStackInSlot(0).getCount());
        assertEquals(Items.COAL, loaded.itemSlots().getStackInSlot(1).getItem());
    }

    @Test
    void smelterCooksOneItemIntoStockUsingMainsCycle() {
        BlockEntitySmelter smelter = smelter();
        smelter.fuelRemaining = 10;
        smelter.itemSlots().setStackInSlot(0, new ItemStack(Items.DIRT, 2));
        try (var index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(order, 2));
            for (int i = 0; i < 4; i++)
                BlockEntitySmelter.staticTick(level, BlockPos.ZERO, smelter.getBlockState(), smelter);
        }
        assertEquals(4, smelter.cookTarget);
        assertEquals(0, smelter.cookElapsed);
        assertEquals(6, smelter.fuelRemaining);
        assertEquals(1, smelter.itemSlots().getStackInSlot(0).getCount());
        assertEquals(2, smelter.essentiaStock.amountOf(order));
        assertEquals(2, smelter.vis);
    }

    @Test
    void consumedFuelReturnsItsNativeContainer() {
        BlockEntitySmelter smelter = smelter();
        smelter.itemSlots().setStackInSlot(0, new ItemStack(Items.DIRT));
        smelter.itemSlots().setStackInSlot(1, new ItemStack(Items.LAVA_BUCKET));
        // unit tests don't load the furnace fuel data map; provide the fuel hook's result here.
        try (var index = mockStatic(AspectIndexAccess.class);
                var fuel = mockStatic(EventHooks.class)) {
            fuel.when(() -> EventHooks.getItemBurnTime(any(ItemStack.class), anyInt(), eq(RecipeType.SMELTING)))
                    .thenReturn(20000);
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(order, 2));
            BlockEntitySmelter.staticTick(level, BlockPos.ZERO, smelter.getBlockState(), smelter);
        }
        assertEquals(Items.BUCKET, smelter.itemSlots().getStackInSlot(1).getItem());
        assertEquals(1, smelter.itemSlots().getStackInSlot(1).getCount());
        assertTrue(smelter.fuelRemaining > 0);
        assertEquals(smelter.fuelCapacity, smelter.fuelRemaining);
    }

    @Test
    void bellowsOnlyCountWhenEnabledAndFacingTheMachine() {
        BlockEntityBellows active = mock(BlockEntityBellows.class);
        when(active.bellowsEnabled()).thenReturn(true);
        when(active.bellowsFacing()).thenReturn(Direction.WEST);
        BlockEntityBellows reversed = mock(BlockEntityBellows.class);
        when(reversed.bellowsEnabled()).thenReturn(true);
        when(reversed.bellowsFacing()).thenReturn(Direction.NORTH);
        BlockEntityBellows disabled = mock(BlockEntityBellows.class);
        when(disabled.bellowsFacing()).thenReturn(Direction.EAST);
        when(level.getBlockEntity(BlockPos.ZERO.east())).thenReturn(active);
        when(level.getBlockEntity(BlockPos.ZERO.north())).thenReturn(reversed);
        when(level.getBlockEntity(BlockPos.ZERO.west())).thenReturn(disabled);
        assertEquals(1, BellowsHelper.countBellows(level, BlockPos.ZERO, Direction.values()));
    }

    @Test
    void bellowsProbeUsesTheRequestedOffsetAndSkipsUnloadedChunks() {
        BlockEntityBellows active = mock(BlockEntityBellows.class);
        when(active.bellowsEnabled()).thenReturn(true);
        when(active.bellowsFacing()).thenReturn(Direction.DOWN);
        when(level.getBlockEntity(BlockPos.ZERO.above(2))).thenReturn(active);
        assertEquals(1, BellowsHelper.countBellows(level, BlockPos.ZERO, new Direction[] {Direction.UP}, 2));
        clearInvocations(level);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);
        assertEquals(0, BellowsHelper.countBellows(level, BlockPos.ZERO, Direction.values()));
        verify(level, never()).getBlockEntity(any());
        assertEquals(0, BellowsHelper.countBellows(null, BlockPos.ZERO, Direction.values()));
    }

    @Test
    void attachedBellowsReduceSmelterCookTimeFromMainsCycle() {
        BlockEntitySmelter smelter = smelter();
        smelter.fuelRemaining = 10;
        smelter.itemSlots().setStackInSlot(0, new ItemStack(Items.DIRT));
        BlockEntityBellows active = mock(BlockEntityBellows.class);
        when(active.bellowsEnabled()).thenReturn(true);
        when(active.bellowsFacing()).thenReturn(Direction.WEST);
        when(level.getBlockEntity(BlockPos.ZERO.east())).thenReturn(active);
        try (var index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(order, 4));
            BlockEntitySmelter.staticTick(level, BlockPos.ZERO, smelter.getBlockState(), smelter);
        }
        assertEquals(7, smelter.cookTarget);
        assertEquals(1, smelter.cookElapsed);
    }

    @Test
    void fullStockPreventsFuelAndInputConsumption() {
        BlockEntitySmelter smelter = smelter();
        smelter.vis = 256;
        smelter.essentiaStock = AspectList.EMPTY.add(order, 256);
        smelter.itemSlots().setStackInSlot(0, new ItemStack(Items.DIRT));
        smelter.itemSlots().setStackInSlot(1, new ItemStack(Items.COAL));
        try (var index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(order, 2));
            BlockEntitySmelter.staticTick(level, BlockPos.ZERO, smelter.getBlockState(), smelter);
        }
        assertEquals(0, smelter.fuelRemaining);
        assertEquals(1, smelter.itemSlots().getStackInSlot(0).getCount());
        assertEquals(1, smelter.itemSlots().getStackInSlot(1).getCount());
    }

    @Test
    void gaugesDoNotMutatePersistedStateAndHandleZeroDivisors() {
        BlockEntitySmelter smelter = smelter();
        smelter.cookElapsed = 5;
        smelter.fuelRemaining = 10;
        smelter.vis = 128;
        assertEquals(500, smelter.scaled(SmelterGauge.COOK, 100));
        assertEquals(5, smelter.scaled(SmelterGauge.FUEL, 100));
        assertEquals(50, smelter.scaled(SmelterGauge.ESSENTIA, 100));
        assertEquals(0, smelter.cookTarget);
        assertEquals(0, smelter.fuelCapacity);
    }

    @Test
    void unloadingDoesNotSpillItemsOrEssentia() {
        BlockEntitySmelter smelter = smelter();
        smelter.itemSlots().setStackInSlot(0, new ItemStack(Items.DIRT));
        smelter.essentiaStock = AspectList.EMPTY.add(order, 4);
        smelter.vis = 4;
        try (var aura = mockStatic(AuraHelper.class)) {
            smelter.setRemoved();
            aura.verifyNoInteractions();
        }
        assertEquals(1, smelter.itemSlots().getStackInSlot(0).getCount());
        assertEquals(4, smelter.vis);
    }

    @Test
    void blockRemovalSpillsStockButLightingChangesDoNot() {
        BlockEntitySmelter smelter = spy(smelter());
        doNothing().when(smelter).dropContents();
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(smelter);
        BlockSmelter block = (BlockSmelter) TTBlocks.SMELTER_BASIC.get();
        var state = block.defaultBlockState();
        block.onRemove(state, level, BlockPos.ZERO, state.setValue(BlockSmelter.LIT, true), false);
        verify(smelter, never()).dropContents();
        block.onRemove(state, level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
        verify(smelter).dropContents();
    }
}
