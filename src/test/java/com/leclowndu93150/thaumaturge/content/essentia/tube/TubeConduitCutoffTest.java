package com.leclowndu93150.thaumaturge.content.essentia.tube;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TubeConduitCutoffTest {
    private Level level;
    private RegistryAccess registries;
    private Holder<IAspect> order;
    private Holder<IAspect> fire;
    private final Map<BlockPos, IEssentiaConduit> peers = new HashMap<>();

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.ORDO, mock(IAspect.class));
        Registry.register(registry, TTAspects.IGNIS, mock(IAspect.class));
        order = registry.getHolderOrThrow(TTAspects.ORDO);
        fire = registry.getHolderOrThrow(TTAspects.IGNIS);
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        level = mock(Level.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getRandom()).thenReturn(RandomSource.create(0));
        when(level.getCapability(eq(EssentiaCapabilities.CONDUIT), any(BlockPos.class), any(Direction.class)))
                .thenAnswer(call -> peers.get(call.getArgument(1)));
    }

    private BlockEntityTube tube() {
        BlockEntityTube tube =
                spy(new BlockEntityTube(BlockPos.ZERO, TTBlocks.TUBE.get().defaultBlockState()));
        tube.setLevel(level);
        return tube;
    }

    private BlockEntityTubeBuffer buffer() {
        BlockEntityTubeBuffer buffer = spy(new BlockEntityTubeBuffer(
                BlockPos.ZERO, TTBlocks.TUBE_BUFFER.get().defaultBlockState()));
        buffer.setLevel(level);
        return buffer;
    }

    private IEssentiaConduit peer(Direction side, Holder<IAspect> aspect, int strength, int amount) {
        IEssentiaConduit peer = mock(IEssentiaConduit.class);
        Direction back = side.getOpposite();
        when(peer.connectsOn(back)).thenReturn(true);
        when(peer.releasesOn(back)).thenReturn(true);
        when(peer.pull(back)).thenReturn(EssentiaPull.of(aspect, strength));
        when(peer.heldAspect(back)).thenReturn(aspect);
        when(peer.heldAmount(back)).thenReturn(amount);
        peers.put(BlockPos.ZERO.relative(side), peer);
        return peer;
    }

    @Test
    void restrictorHalvesOfferedPullWithoutSubtractingHopLoss() {
        BlockEntityTube tube = tube();
        peer(Direction.UP, order, 64, 0);
        EssentiaFlowHandler.recalculateSuction(level, BlockPos.ZERO, tube, null, true, false);
        assertEquals(EssentiaPull.of(order, 32), tube.pull(null));
        EssentiaFlowHandler.recalculateSuction(level, BlockPos.ZERO, tube, null, false, false);
        assertEquals(EssentiaPull.of(order, 63), tube.pull(null));
    }

    @Test
    @SuppressWarnings("removal")
    void unloadedLookupNeverQueriesEitherCapability() {
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);
        assertNull(EssentiaFlowHandler.conduit(level, BlockPos.ZERO.above(), Direction.DOWN));
        verify(level, never())
                .getCapability(eq(EssentiaCapabilities.CONDUIT), any(BlockPos.class), any(Direction.class));
        verify(level, never())
                .getCapability(eq(EssentiaCapabilities.TRANSPORT), any(BlockPos.class), any(Direction.class));
    }

    @Test
    void tubeSimulationMovesOneUnitWithoutChangingContentsOrDirtyState() {
        BlockEntityTube tube = tube();
        clearInvocations(tube, level);
        assertEquals(1, tube.insert(order, 10, Direction.UP, true));
        assertEquals(1, tube.roomFor(order, Direction.UP));
        assertEquals(0, tube.heldAmount(null));
        verify(tube, never()).setChanged();
        assertEquals(1, tube.insert(order, 10, Direction.UP, false));
        clearInvocations(tube, level);
        assertEquals(1, tube.extract(order, 10, Direction.DOWN, true));
        assertEquals(0, tube.extract(fire, 1, Direction.DOWN, true));
        assertEquals(1, tube.heldAmount(null));
        verify(tube, never()).setChanged();
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
    }

    @Test
    void oneWaySimulationRejectsTransfersAgainstItsDirection() {
        BlockEntityTubeOneway tube = new BlockEntityTubeOneway(
                BlockPos.ZERO, TTBlocks.TUBE_ONEWAY.get().defaultBlockState());
        tube.setLevel(level);
        Direction input = tube.flowSide();
        Direction output = input.getOpposite();
        assertEquals(0, tube.insert(order, 1, output, true));
        assertEquals(0, tube.roomFor(order, output));
        assertEquals(1, tube.insert(order, 1, input, false));
        assertEquals(0, tube.extract(order, 1, input, true));
        assertEquals(1, tube.extract(order, 1, output, true));
        assertEquals(1, tube.heldAmount(null));
    }

    @Test
    void closedValveRejectsSimulatedTransfersAndHidesPull() {
        BlockEntityTubeValve valve = spy(new BlockEntityTubeValve(
                BlockPos.ZERO, TTBlocks.TUBE_VALVE.get().defaultBlockState()));
        valve.setLevel(level);
        valve.assignPull(order, 32);
        valve.setAllowFlow(false);
        clearInvocations(valve, level);
        assertSame(EssentiaPull.NONE, valve.pull(null));
        assertFalse(valve.receivesOn(Direction.SOUTH));
        assertFalse(valve.releasesOn(Direction.SOUTH));
        assertEquals(0, valve.insert(order, 1, Direction.SOUTH, true));
        assertEquals(0, valve.extract(order, 1, Direction.SOUTH, true));
        assertEquals(0, valve.roomFor(order, Direction.SOUTH));
        verify(valve, never()).setChanged();
    }

    @Test
    void bufferSimulatesExactUnitInsertionAndMixedAspectExtraction() {
        BlockEntityTubeBuffer buffer = buffer();
        assertEquals(1, buffer.insert(order, 1, Direction.UP, true));
        assertEquals(0, buffer.insert(order, 2, Direction.UP, true));
        assertEquals(0, buffer.heldTotal());
        buffer.insert(order, 1, Direction.UP, false);
        buffer.insert(fire, 1, Direction.UP, false);
        clearInvocations(buffer, level);
        assertEquals(1, buffer.extract(order, 10, Direction.DOWN, true));
        assertEquals(1, buffer.heldAspects().amountOf(order));
        assertEquals(1, buffer.heldAspects().amountOf(fire));
        assertEquals(8, buffer.roomFor(order, Direction.UP));
        verify(buffer, never()).setChanged();
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
    }

    @Test
    void bufferIgnoresDisconnectedRivalWhenDecidingExtraction() {
        BlockEntityTubeBuffer buffer = buffer();
        buffer.insert(order, 1, Direction.UP, false);
        peer(Direction.DOWN, order, 8, 0);
        IEssentiaConduit rival = peer(Direction.UP, order, 16, 0);
        assertEquals(0, buffer.extract(order, 1, Direction.DOWN, true));
        when(rival.connectsOn(Direction.DOWN)).thenReturn(false);
        assertEquals(1, buffer.extract(order, 1, Direction.DOWN, true));
        assertEquals(1, buffer.heldTotal());
    }

    @Test
    void exposedPullCacheIsInvalidatedByAssignmentClearAndNativeLoad() {
        BlockEntityTube tube = tube();
        tube.assignPull(order, 32);
        EssentiaPull first = tube.pull(null);
        assertSame(first, tube.pull(Direction.UP));
        tube.assignPull(fire, 16);
        assertEquals(EssentiaPull.of(fire, 16), tube.pull(null));
        assertNotSame(first, tube.pull(null));
        CompoundTag saved = tube.getUpdateTag(registries);
        tube.clearSuction();
        assertSame(EssentiaPull.NONE, tube.pull(null));
        tube.loadAdditional(saved, registries);
        assertEquals(EssentiaPull.of(fire, 16), tube.pull(null));
        assertSame(tube.pull(null), tube.pull(Direction.DOWN));
    }

    @Test
    void filterIntentIsIndependentOfInactivePullAndOverridesActiveAspect() {
        BlockEntityTubeFilter filter = new BlockEntityTubeFilter(
                BlockPos.ZERO, TTBlocks.TUBE_FILTER.get().defaultBlockState());
        filter.setLevel(level);
        filter.setAspectFilter(TTAspects.ORDO);
        assertSame(EssentiaPull.NONE, filter.pull(null));
        assertEquals(1, filter.queryAspects().amountOf(order));
        filter.assignPull(fire, 20);
        assertEquals(EssentiaPull.of(order, 20), filter.pull(null));
        filter.setAspectFilter(null);
        assertEquals(EssentiaPull.of(fire, 20), filter.pull(null));
    }

    @Test
    void serverPressureClashPausesFlowForExactlyFortyTicks() {
        BlockEntityTube tube = tube();
        IEssentiaConduit first = peer(Direction.DOWN, order, 33, 0);
        IEssentiaConduit second = peer(Direction.UP, fire, 32, 0);
        tube.tickServer(level, BlockPos.ZERO, tube.getBlockState());
        tube.tickServer(level, BlockPos.ZERO, tube.getBlockState());
        assertEquals(40, tube.ventingTicks());
        clearInvocations(first, second);
        for (int tick = 0; tick < 40; tick++) {
            tube.tickServer(level, BlockPos.ZERO, tube.getBlockState());
        }
        assertEquals(0, tube.ventingTicks());
        verifyNoInteractions(first, second);
    }

    @Test
    void clientJetLastsFiftyTicksWithStableUnitHeading() {
        BlockEntityTube tube = tube();
        tube.triggerVent(0x123456);
        assertEquals(50, tube.ventingTicks());
        for (int tick = 0; tick < 51; tick++) {
            tube.tickClient(level, BlockPos.ZERO, tube.getBlockState());
        }
        ArgumentCaptor<VentParticleOptions> particles = ArgumentCaptor.forClass(VentParticleOptions.class);
        verify(level, times(50)).addParticle(particles.capture(), eq(0.5), eq(0.5), eq(0.5), eq(0.0), eq(0.0), eq(0.0));
        VentParticleOptions first = particles.getAllValues().getFirst();
        assertEquals(0x123456, first.color());
        assertEquals(1.5F, first.scale());
        assertEquals(1.0, first.vx() * first.vx() + first.vy() * first.vy() + first.vz() * first.vz(), 1.0E-9);
        assertTrue(particles.getAllValues().stream().allMatch(first::equals));
        assertEquals(0, tube.ventingTicks());
    }

    @Test
    void everyOrdinaryTubeSubtypeHasClientTickerForVenting() {
        when(level.isClientSide()).thenReturn(true);
        BlockTube tube = (BlockTube) TTBlocks.TUBE.get();
        BlockTube filter = (BlockTube) TTBlocks.TUBE_FILTER.get();
        BlockTube oneway = (BlockTube) TTBlocks.TUBE_ONEWAY.get();
        BlockTube restrictor = (BlockTube) TTBlocks.TUBE_RESTRICT.get();
        assertNotNull(tube.getTicker(level, tube.defaultBlockState(), TTBlockEntities.TUBE.get()));
        assertNotNull(filter.getTicker(level, filter.defaultBlockState(), TTBlockEntities.TUBE_FILTER.get()));
        assertNotNull(oneway.getTicker(level, oneway.defaultBlockState(), TTBlockEntities.TUBE_ONEWAY.get()));
        assertNotNull(restrictor.getTicker(level, restrictor.defaultBlockState(), TTBlockEntities.TUBE_RESTRICT.get()));
    }
}
