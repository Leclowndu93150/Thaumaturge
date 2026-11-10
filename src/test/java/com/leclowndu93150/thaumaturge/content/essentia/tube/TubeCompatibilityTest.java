package com.leclowndu93150.thaumaturge.content.essentia.tube;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

class TubeCompatibilityTest {
    private Level level;
    private RegistryAccess registries;
    private Holder<IAspect> order;
    private Holder<IAspect> fire;
    private final Map<BlockPos, IEssentiaConduit> peers = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.ORDO, mock(IAspect.class));
        Registry.register(registry, TTAspects.IGNIS, mock(IAspect.class));
        order = registry.getHolderOrThrow(TTAspects.ORDO);
        fire = registry.getHolderOrThrow(TTAspects.IGNIS);
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        level = mock(Level.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getCapability(eq(EssentiaCapabilities.CONDUIT), any(BlockPos.class), any(Direction.class)))
                .thenAnswer(call -> peers.get(call.getArgument(1)));
        var random = Level.class.getDeclaredField("random");
        random.setAccessible(true);
        random.set(level, RandomSource.create(0));
        when(level.getRandom()).thenReturn(RandomSource.create(0));
    }

    private BlockEntityTube tube(BlockPos pos) {
        BlockEntityTube tube = new BlockEntityTube(pos, TTBlocks.TUBE.get().defaultBlockState());
        tube.setLevel(level);
        return tube;
    }

    private BlockEntityTubeBuffer buffer() {
        BlockEntityTubeBuffer buffer = new BlockEntityTubeBuffer(
                BlockPos.ZERO, TTBlocks.TUBE_BUFFER.get().defaultBlockState());
        buffer.setLevel(level);
        return buffer;
    }

    private BlockEntityTubeBuffer bufferAt(BlockPos pos) {
        BlockEntityTubeBuffer buffer =
                new BlockEntityTubeBuffer(pos, TTBlocks.TUBE_BUFFER.get().defaultBlockState());
        buffer.setLevel(level);
        return buffer;
    }

    private BlockEntityTubeValve valve(BlockPos pos) {
        BlockEntityTubeValve valve =
                new BlockEntityTubeValve(pos, TTBlocks.TUBE_VALVE.get().defaultBlockState());
        valve.setLevel(level);
        return valve;
    }

    private IEssentiaConduit peer(Direction side, Holder<IAspect> aspect, int strength, int amount) {
        IEssentiaConduit peer = mock(IEssentiaConduit.class);
        Direction back = side.getOpposite();
        when(peer.connectsOn(back)).thenReturn(true);
        when(peer.releasesOn(back)).thenReturn(true);
        when(peer.heldAspect(back)).thenReturn(aspect);
        when(peer.heldAmount(back)).thenReturn(amount);
        when(peer.pull(back)).thenReturn(EssentiaPull.of(aspect, strength));
        peers.put(BlockPos.ZERO.relative(side), peer);
        return peer;
    }

    @Test
    void tubeMovesOneUnitAndKeepsAspectUntilEmpty() {
        BlockEntityTube tube = tube(BlockPos.ZERO);
        assertEquals(1, tube.insert(order, 64, Direction.UP, false));
        assertEquals(0, tube.insert(fire, 1, Direction.UP, false));
        assertEquals(TTAspects.ORDO, tube.essentiaKey());
        assertEquals(0, tube.extract(fire, 1, Direction.DOWN, false));
        assertEquals(1, tube.extract(order, 64, Direction.DOWN, false));
        assertEquals(0, tube.essentiaAmountRaw());
        assertNull(tube.essentiaKey());
    }

    @Test
    void closedSidesAndInvalidAmountsRejectTransfers() {
        BlockEntityTube tube = tube(BlockPos.ZERO);
        tube.setOpenSide(Direction.UP, false);
        assertEquals(0, tube.insert(order, 1, Direction.UP, false));
        assertEquals(0, tube.insert(order, 0, Direction.DOWN, false));
        assertEquals(0, tube.insert(Holder.direct(mock(IAspect.class)), 1, Direction.DOWN, false));
        assertEquals(1, tube.insert(order, 1, Direction.DOWN, false));
        assertEquals(0, tube.extract(Holder.direct(mock(IAspect.class)), 1, Direction.DOWN, false));
        assertEquals(0, tube.extract(order, 0, Direction.DOWN, false));
        assertEquals(1, tube.heldAmount(null));
    }

    @Test
    void valveHeadIsDisconnectedAndClosedValveClearsSuction() {
        BlockEntityTubeValve valve = valve(BlockPos.ZERO);
        assertFalse(valve.connectsOn(Direction.NORTH));
        assertEquals(1, valve.insert(order, 1, Direction.SOUTH, false));
        valve.assignPull(order, 32);
        valve.setAllowFlow(false);
        assertEquals(0, valve.pull(null).strength());
        assertNull(valve.pull(null).aspect());
        assertEquals(0, valve.extract(order, 1, Direction.SOUTH, false));
        valve.setAllowFlow(true);
        assertEquals(1, valve.extract(order, 1, Direction.SOUTH, false));
    }

    @Test
    void oneWayTubeUsesBlockFacingForInputAndOutput() {
        var state = TTBlocks.TUBE_ONEWAY.get().defaultBlockState();
        var tube = new BlockEntityTubeOneway(BlockPos.ZERO, state);
        Direction outlet = tube.flowSide().getOpposite();
        assertFalse(tube.receivesOn(outlet));
        assertTrue(tube.releasesOn(outlet));
        assertTrue(tube.receivesOn(tube.flowSide()));
        assertFalse(tube.releasesOn(tube.flowSide()));
    }

    @Test
    void bufferFillReportsAcceptedAmountAndStaysAtTenUnits() {
        BlockEntityTubeBuffer buffer = buffer();
        assertEquals(0, buffer.fill(TTAspects.ORDO, 2));
        assertEquals(0, buffer.heldTotal());
        assertEquals(1, buffer.fill(TTAspects.ORDO, 1));
        for (int i = 1; i < 10; i++) assertEquals(1, buffer.insert(order, 1, Direction.UP, false));
        assertEquals(10, buffer.heldTotal());
        assertEquals(0, buffer.fill(TTAspects.IGNIS, 1));
        assertEquals(0, buffer.insert(fire, 1, Direction.UP, false));
        assertEquals(3, buffer.extract(order, 3, Direction.DOWN, false));
        assertEquals(7, buffer.heldTotal());
    }

    @Test
    void bufferYieldsToStrongerCompatiblePuller() {
        BlockEntityTubeBuffer buffer = buffer();
        buffer.fill(TTAspects.ORDO, 1);
        peer(Direction.DOWN, order, 8, 0);
        IEssentiaConduit stronger = peer(Direction.UP, order, 16, 0);
        assertEquals(0, buffer.extract(order, 1, Direction.DOWN, false));
        when(stronger.pull(Direction.DOWN)).thenReturn(EssentiaPull.of(fire, 16));
        assertEquals(1, buffer.extract(order, 1, Direction.DOWN, false));
    }

    @Test
    void bufferPullsOnFifthTickAndRetriesWhenFirstPeerRefuses() {
        BlockEntityTubeBuffer buffer = buffer();
        IEssentiaConduit first = peer(Direction.DOWN, order, 0, 1);
        IEssentiaConduit second = peer(Direction.UP, order, 0, 1);
        when(second.extract(order, 1, Direction.DOWN, false)).thenReturn(1);
        for (int i = 0; i < 4; i++) buffer.tickServer(level, BlockPos.ZERO, buffer.getBlockState());
        verify(first, never()).extract(any(), anyInt(), any(), eq(false));
        buffer.tickServer(level, BlockPos.ZERO, buffer.getBlockState());
        verify(first).extract(order, 1, Direction.UP, false);
        verify(second).extract(order, 1, Direction.DOWN, false);
        assertEquals(1, buffer.heldTotal());
    }

    @Test
    void bufferWandToggleKeepsNeighbourSideInSync() {
        BlockEntityTubeBuffer buffer = buffer();
        BlockEntityTube neighbour = tube(BlockPos.ZERO.above());
        when(level.getBlockEntity(BlockPos.ZERO.above())).thenReturn(neighbour);
        assertTrue(buffer.handleCasterClick(Direction.UP.ordinal(), false));
        assertFalse(buffer.isSideOpen(Direction.UP));
        assertFalse(neighbour.isSideOpen(Direction.DOWN));
        assertTrue(buffer.handleCasterClick(Direction.UP.ordinal(), false));
        assertTrue(neighbour.isSideOpen(Direction.DOWN));
    }

    @Test
    void bufferReconnectsClosedCoreFace() {
        BlockEntityTubeBuffer buffer = buffer();
        BlockEntityTubeBuffer neighbour = bufferAt(BlockPos.ZERO.east());
        when(level.getBlockEntity(BlockPos.ZERO.east())).thenReturn(neighbour);
        buffer.setOpenSide(Direction.EAST, false);
        neighbour.setOpenSide(Direction.WEST, false);

        int part = buffer.resolveClickPart(TubeGeometry.CORE_HIT, Direction.EAST);

        assertEquals(Direction.EAST.ordinal(), part);
        assertTrue(buffer.handleCasterClick(part, false));
        assertTrue(buffer.isSideOpen(Direction.EAST));
        assertTrue(neighbour.isSideOpen(Direction.WEST));
    }

    @Test
    void bufferChokeCyclesNormalReducedBlocked() {
        BlockEntityTubeBuffer buffer = buffer();
        for (int expected : new int[] {1, 2, 0}) {
            buffer.cycleChokedSide(Direction.EAST);
            assertEquals(expected, buffer.chokedSide(Direction.EAST));
            assertEquals(expected == 2 ? 0 : 1, buffer.pull(Direction.EAST).strength());
        }
    }

    @Test
    void suctionHonoursHeldAspectAndRestriction() {
        BlockEntityTube tube = tube(BlockPos.ZERO);
        peer(Direction.DOWN, fire, 64, 0);
        peer(Direction.UP, order, 32, 0);
        tube.insert(order, 1, Direction.NORTH, false);
        EssentiaFlowHandler.recalculateSuction(level, BlockPos.ZERO, tube, null, false, false);
        assertEquals(31, tube.pull(null).strength());
        assertEquals(order, tube.pull(null).aspect());
        EssentiaFlowHandler.recalculateSuction(level, BlockPos.ZERO, tube, null, true, false);
        assertEquals(16, tube.pull(null).strength());
    }

    @Test
    void equalizationTransfersOneUnitAndIgnoresUntypedSources() {
        BlockEntityTube tube = tube(BlockPos.ZERO);
        tube.assignPull(null, 32);
        IEssentiaConduit peer = peer(Direction.DOWN, order, 0, 1);
        when(peer.extract(order, 1, Direction.UP, false)).thenReturn(1);
        EssentiaFlowHandler.equalizeWithNeighbours(level, BlockPos.ZERO, tube, false);
        assertEquals(TTAspects.ORDO, tube.essentiaKey());
        assertEquals(1, tube.heldAmount(null));
        verify(peer).extract(order, 1, Direction.UP, false);
    }

    @Test
    void unloadedNeighbourDoesNotQueryCapabilities() {
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);
        assertNull(EssentiaFlowHandler.conduit(level, new BlockPos(32, 10, 32), Direction.UP));
        verify(level, never())
                .getCapability(eq(EssentiaCapabilities.CONDUIT), any(BlockPos.class), any(Direction.class));
    }

    @Test
    void valveSamplesRedstoneOnMainStaggeredPhase() {
        BlockPos pos = new BlockPos(1, 0, 0);
        BlockEntityTubeValve valve = valve(pos);
        when(level.hasNeighborSignal(pos)).thenReturn(true);
        for (int i = 0; i < 3; i++) {
            valve.tickServer(level, pos, valve.getBlockState());
            assertTrue(valve.allowFlow());
        }
        valve.tickServer(level, pos, valve.getBlockState());
        assertFalse(valve.allowFlow());
        valve.setAllowFlow(true);
        for (int i = 0; i < 5; i++) valve.tickServer(level, pos, valve.getBlockState());
        assertTrue(valve.allowFlow());
    }

    @Test
    void valveAnimationKeepsAnglesAndInterpolation() {
        BlockEntityTubeValve valve = valve(BlockPos.ZERO);
        valve.setAllowFlow(false);
        valve.tickClient(level, BlockPos.ZERO, valve.getBlockState());
        assertEquals(20.0F, valve.rotation());
        assertEquals(10.0F, valve.rotation(0.5F));
        for (int i = 0; i < 20; i++) valve.tickClient(level, BlockPos.ZERO, valve.getBlockState());
        assertEquals(360.0F, valve.rotation());
        valve.setAllowFlow(true);
        valve.tickClient(level, BlockPos.ZERO, valve.getBlockState());
        assertEquals(340.0F, valve.rotation());
    }

    @Test
    void tubeUpdateTagPreservesExactKeysAndByteArrayFormat() {
        BlockEntityTube tube = tube(BlockPos.ZERO);
        tube.insert(order, 1, Direction.UP, false);
        tube.assignPull(fire, 23);
        tube.setOpenSide(Direction.EAST, false);
        CompoundTag saved = tube.getUpdateTag(registries);
        assertEquals(
                Set.of("EssentiaType", "EssentiaAmount", "SuctionType", "Suction", "Facing", "OpenSides"),
                saved.getAllKeys());
        assertArrayEquals(new byte[] {1, 1, 1, 1, 1, 0}, saved.getByteArray("OpenSides"));
        BlockEntityTube restored = tube(BlockPos.ZERO);
        restored.loadAdditional(saved, registries);
        assertEquals(saved, restored.getUpdateTag(registries));
    }

    @Test
    void bufferUpdateTagPreservesContentsAndSideLists() {
        BlockEntityTubeBuffer buffer = buffer();
        buffer.fill(TTAspects.ORDO, 1);
        buffer.cycleChokedSide(Direction.NORTH);
        buffer.setOpenSide(Direction.WEST, false);
        CompoundTag saved = buffer.getUpdateTag(registries);
        assertEquals(Set.of("Contents", "Choked", "Open", "Facing"), saved.getAllKeys());
        assertEquals(
                List.of(0, 0, 1, 0, 0, 0),
                TTNbt.read(saved, "Choked", Codec.INT.listOf(), registries).orElseThrow());
        BlockEntityTubeBuffer restored = buffer();
        restored.loadAdditional(saved, registries);
        assertEquals(saved, restored.getUpdateTag(registries));
        assertEquals(1, restored.heldAspects().amountOf(order));
    }

    @Test
    void filterExposesAspectQueryAndNamesAnAspectOnlyWhilePulling() {
        var filter = new BlockEntityTubeFilter(
                BlockPos.ZERO, TTBlocks.TUBE_FILTER.get().defaultBlockState());
        filter.setLevel(level);
        filter.setAspectFilter(TTAspects.ORDO);
        assertSame(EssentiaPull.NONE, filter.pull(null));
        filter.assignPull(null, 20);
        assertEquals(order, filter.pull(null).aspect());
        assertEquals(1, filter.queryAspects().amountOf(order));
        filter.setAspectFilter(null);
        assertEquals(AspectList.EMPTY, filter.queryAspects());
    }
}
