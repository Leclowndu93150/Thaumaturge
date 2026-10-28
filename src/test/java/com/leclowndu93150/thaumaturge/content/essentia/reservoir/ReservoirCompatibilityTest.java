package com.leclowndu93150.thaumaturge.content.essentia.reservoir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTCapabilities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservoirCompatibilityTest {
    private ServerLevel level;
    private RegistryAccess registries;
    private Holder<IAspect> order;
    private Holder<IAspect> fire;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        IAspect orderValue = mock(IAspect.class);
        IAspect fireValue = mock(IAspect.class);
        when(orderValue.color()).thenReturn(0xFF0000);
        when(fireValue.color()).thenReturn(0x0000FF);
        Registry.register(registry, TTAspects.ORDO, orderValue);
        Registry.register(registry, TTAspects.IGNIS, fireValue);
        order = registry.getHolderOrThrow(TTAspects.ORDO);
        fire = registry.getHolderOrThrow(TTAspects.IGNIS);
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        level = mock(ServerLevel.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.hasChunkAt(any())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getRandom()).thenReturn(mock(RandomSource.class));
    }

    private BlockEntityEssentiaReservoir reservoir() {
        var state = TTBlocks.ESSENTIA_RESERVOIR
                .get()
                .defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.EAST);
        BlockEntityEssentiaReservoir reservoir = new BlockEntityEssentiaReservoir(BlockPos.ZERO, state);
        reservoir.setLevel(level);
        return reservoir;
    }

    @Test
    void transferUsesTheConfiguredFaceAndClipsToCapacity() {
        var reservoir = reservoir();
        assertEquals(0, reservoir.insert(order, 1, Direction.WEST, false));
        assertEquals(0, reservoir.insert(order, -1, Direction.EAST, false));
        assertEquals(250, reservoir.insert(order, 250, Direction.EAST, false));
        assertEquals(6, reservoir.insert(fire, 10, Direction.EAST, false));
        assertEquals(256, reservoir.getStoredAmount());
        assertEquals(0, reservoir.pull(Direction.EAST).strength());
        assertEquals(0, reservoir.extract(order, 1, Direction.WEST, false));
        assertEquals(1, reservoir.extract(order, 1, Direction.EAST, false));
        assertEquals(24, reservoir.pull(Direction.EAST).strength());
    }

    @Test
    void outputReportsTheLeadingCompartmentRatherThanMixedTotal() {
        var reservoir = reservoir();
        reservoir.insert(order, 2, Direction.EAST, false);
        reservoir.insert(fire, 3, Direction.EAST, false);
        assertEquals(order, reservoir.heldAspect(Direction.EAST));
        assertEquals(2, reservoir.heldAmount(Direction.EAST));
        assertEquals(5, reservoir.getStoredAmount());
        assertEquals(0, reservoir.heldAmount(Direction.WEST));
        assertNull(reservoir.heldAspect(null));
    }

    @Test
    void nbtRetainsMixedContentsAndClipsOversizedLoadedStock() {
        var original = reservoir();
        original.insert(order, 7, Direction.EAST, false);
        original.insert(fire, 9, Direction.EAST, false);
        CompoundTag tag = original.getUpdateTag(registries);
        assertEquals(Set.of("Essentia"), tag.getAllKeys());
        var loaded = reservoir();
        loaded.loadAdditional(tag, registries);
        assertEquals(7, loaded.contents().amountOf(order));
        assertEquals(9, loaded.contents().amountOf(fire));
        assertEquals(tag, loaded.getUpdateTag(registries));
        TTNbt.store(tag, "Essentia", AspectList.CODEC, registries, AspectList.EMPTY.add(order, 257));
        loaded.loadAdditional(tag, registries);
        assertEquals(256, loaded.contents().amountOf(order));
    }

    @Test
    void pullUsesItsOwnCadenceAndReadsOnlyLoadedConnectedChunks() {
        var reservoir = reservoir();
        IEssentiaConduit source = mock(IEssentiaConduit.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.east(), Direction.WEST))
                .thenReturn(source);
        when(source.connectsOn(Direction.WEST)).thenReturn(true);
        when(source.releasesOn(Direction.WEST)).thenReturn(true);
        when(source.heldAmount(Direction.WEST)).thenReturn(2);
        when(source.heldAspect(Direction.WEST)).thenReturn(order);
        when(source.extract(order, 1, Direction.WEST, false)).thenReturn(1);
        when(source.pull(Direction.WEST)).thenReturn(EssentiaPull.NONE);
        when(level.getGameTime()).thenReturn(73L);
        tick(reservoir, 4);
        verifyNoInteractions(source);
        tick(reservoir, 1);
        assertEquals(1, reservoir.getStoredAmount());
        when(level.hasChunkAt(BlockPos.ZERO.east())).thenReturn(false);
        clearInvocations(source);
        tick(reservoir, 5);
        verifyNoInteractions(source);
        when(level.hasChunkAt(BlockPos.ZERO.east())).thenReturn(true);
        when(source.connectsOn(Direction.WEST)).thenReturn(false);
        tick(reservoir, 5);
        verify(source, never()).extract(order, 1, Direction.WEST, false);
    }

    private void tick(BlockEntityEssentiaReservoir reservoir, int count) {
        for (int i = 0; i < count; i++) {
            BlockEntityEssentiaReservoir.serverTick(level, BlockPos.ZERO, reservoir.getBlockState(), reservoir);
        }
    }

    @Test
    void unloadingAndFacingChangesDoNotRuptureTheReservoir() {
        var reservoir = spy(reservoir());
        reservoir.insert(order, 32, Direction.EAST, false);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(reservoir);
        try (var aura = mockStatic(AuraHelper.class)) {
            reservoir.setRemoved();
            var block = (BlockEssentiaReservoir) TTBlocks.ESSENTIA_RESERVOIR.get();
            var state = reservoir.getBlockState();
            block.onRemove(
                    state, level, BlockPos.ZERO, state.setValue(BlockStateProperties.FACING, Direction.WEST), false);
            aura.verifyNoInteractions();
            verify(reservoir, never()).releaseContents(any());
        }
        assertEquals(32, reservoir.getStoredAmount());
    }

    @Test
    void actualBlockRemovalCallsThePortedRuptureHook() {
        var reservoir = mock(BlockEntityEssentiaReservoir.class);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(reservoir);
        var block = (BlockEssentiaReservoir) TTBlocks.ESSENTIA_RESERVOIR.get();
        block.onRemove(block.defaultBlockState(), level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
        verify(reservoir).releaseContents(BlockPos.ZERO);
    }

    @Test
    void fullRuptureKeepsMainsFluxAndPocketBudget() {
        var reservoir = reservoir();
        reservoir.insert(order, 256, Direction.EAST, false);
        try (var aura = mockStatic(AuraHelper.class);
                var flux = mockStatic(PhysicalFlux.class)) {
            flux.when(() -> PhysicalFlux.placeGas(any(), any(), anyInt())).thenReturn(true);
            reservoir.releaseContents(BlockPos.ZERO);
            aura.verify(() -> AuraHelper.polluteAura(level, BlockPos.ZERO, 64.0F, true));
            flux.verify(() -> PhysicalFlux.placeGas(level, BlockPos.ZERO, PhysicalFlux.MAX_QUANTA), times(17));
            verify(level).explode(null, 0.5, 0.5, 0.5, 1.0F, Level.ExplosionInteraction.NONE);
        }
    }

    @Test
    void pocketSearchNeverWritesToUnloadedChunks() {
        when(level.hasChunkAt(any())).thenReturn(false);
        try (var flux = mockStatic(PhysicalFlux.class)) {
            FluxPocketScatter.scatter(level, BlockPos.ZERO, 3);
            flux.verifyNoInteractions();
        }
        verify(level, times(50)).hasChunkAt(any());
        verify(level, never()).getBlockState(any());
    }

    @Test
    void reservoirIsNotExposedAsAnAspectContainer() {
        RegisterCapabilitiesEvent event = mock(RegisterCapabilitiesEvent.class);
        TTCapabilities.onRegisterCapabilities(event);
        boolean exposed = mockingDetails(event).getInvocations().stream()
                .filter(call -> call.getMethod().getName().equals("registerBlockEntity"))
                .anyMatch(call -> call.getArgument(0) == AspectCapabilities.CONTAINER
                        && call.getArgument(1) == TTBlockEntities.ESSENTIA_RESERVOIR.get());
        assertFalse(exposed);
    }

    @Test
    void rendererUsesWorldTimeWithoutAClientBlockTicker() {
        Level client = mock(Level.class);
        when(client.isClientSide()).thenReturn(true);
        var block = (BlockEssentiaReservoir) TTBlocks.ESSENTIA_RESERVOIR.get();
        assertNull(block.getTicker(client, block.defaultBlockState(), TTBlockEntities.ESSENTIA_RESERVOIR.get()));
        assertNotNull(block.getTicker(level, block.defaultBlockState(), TTBlockEntities.ESSENTIA_RESERVOIR.get()));
    }

    @Test
    void conduitSimulationNeverMutatesContentsOrPublishesUpdates() {
        var reservoir = reservoir();
        clearInvocations(level);
        assertEquals(256, reservoir.insert(order, 300, Direction.EAST, true));
        assertEquals(0, reservoir.getStoredAmount());
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
        reservoir.insert(order, 7, Direction.EAST, false);
        clearInvocations(level);
        assertEquals(7, reservoir.extract(order, 7, Direction.EAST, true));
        assertEquals(0, reservoir.extract(order, 8, Direction.EAST, true));
        assertEquals(7, reservoir.getStoredAmount());
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
    }

    @Test
    void smallRuptureOnlyPollutesAuraWithoutAnExplosionOrPhysicalFlux() {
        var reservoir = reservoir();
        reservoir.insert(order, 15, Direction.EAST, false);
        try (var aura = mockStatic(AuraHelper.class);
                var flux = mockStatic(PhysicalFlux.class)) {
            reservoir.releaseContents(BlockPos.ZERO);
            aura.verify(() -> AuraHelper.polluteAura(level, BlockPos.ZERO, 3.75F, true));
            flux.verifyNoInteractions();
            verify(level, never())
                    .explode(
                            any(),
                            anyDouble(),
                            anyDouble(),
                            anyDouble(),
                            anyFloat(),
                            any(Level.ExplosionInteraction.class));
        }
    }

    @Test
    void pocketScatterCountsSuccessfulPlacementsAndUsesGooBelowItsOrigin() {
        RandomSource random = level.getRandom();
        when(random.nextInt(5)).thenReturn(0, 0, 1, 0, 0, 0);
        try (var flux = mockStatic(PhysicalFlux.class)) {
            flux.when(() -> PhysicalFlux.placeGoo(level, BlockPos.ZERO.below(), PhysicalFlux.MAX_QUANTA))
                    .thenReturn(true);
            assertEquals(1, FluxPocketScatter.scatter(level, BlockPos.ZERO, 1));
            flux.verify(() -> PhysicalFlux.placeGoo(level, BlockPos.ZERO.below(), PhysicalFlux.MAX_QUANTA));
            flux.verify(() -> PhysicalFlux.placeGas(any(), any(), anyInt()), never());
            verify(random, times(6)).nextInt(5);
        }
    }

    @Test
    void emptyPocketBudgetDoesNotSearchOrUseRandomness() {
        try (var flux = mockStatic(PhysicalFlux.class)) {
            assertEquals(0, FluxPocketScatter.scatter(level, BlockPos.ZERO, 0));
            flux.verifyNoInteractions();
        }
        verify(level, never()).hasChunkAt(any());
        verifyNoInteractions(level.getRandom());
    }
}
