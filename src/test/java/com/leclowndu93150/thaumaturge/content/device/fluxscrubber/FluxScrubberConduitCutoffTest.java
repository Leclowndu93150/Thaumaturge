package com.leclowndu93150.thaumaturge.content.device.fluxscrubber;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayNetwork;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Lifecycle;
import java.util.HashSet;
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
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FluxScrubberConduitCutoffTest {
    private static final int CELLS = 33 * 33 * 33;
    private static final int CENTER = 16 * 33 * 33 + 16 * 33 + 16;
    private ServerLevel level;
    private RandomSource random;
    private RegistryAccess registries;
    private Holder<IAspect> magic;
    private Holder<IAspect> other;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(aspects, TTAspects.PRAECANTATIO, mock(IAspect.class));
        Registry.register(aspects, TTAspects.ORDO, mock(IAspect.class));
        magic = aspects.getHolderOrThrow(TTAspects.PRAECANTATIO);
        other = aspects.getHolderOrThrow(TTAspects.ORDO);
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(aspects)).freeze();
        level = mock(ServerLevel.class);
        random = mock(RandomSource.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.getRandom()).thenReturn(random);
        when(level.hasChunkAt(any())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        when(random.nextInt(CELLS)).thenReturn(CENTER);
    }

    private BlockEntityFluxScrubber machine(int essentia, int charges, int power) {
        var state =
                TTBlocks.FLUX_SCRUBBER.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST);
        var machine = new BlockEntityFluxScrubber(BlockPos.ZERO, state);
        machine.setLevel(level);
        CompoundTag input = new CompoundTag();
        input.putInt("Essentia", essentia);
        input.putInt("Charges", charges);
        input.putInt("Power", power);
        machine.loadAdditional(input, registries);
        return machine;
    }

    private void tick(BlockEntityFluxScrubber machine, int count) {
        for (int i = 0; i < count; i++)
            BlockEntityFluxScrubber.serverTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
    }

    @Test
    void conduitExposesOnlyStockOnOutputAndSimulationDoesNotPublish() {
        var machine = machine(3, 0, 0);
        assertEquals(magic, machine.heldAspect(Direction.EAST));
        assertEquals(3, machine.heldAmount(Direction.EAST));
        assertNull(machine.heldAspect(null));
        assertEquals(0, machine.heldAmount(null));
        assertEquals(EssentiaPull.NONE, machine.pull(null));
        assertEquals(0, machine.pullThreshold());
        for (Direction face : Direction.values()) {
            assertEquals(face == Direction.EAST, machine.connectsOn(face));
            assertEquals(face == Direction.EAST, machine.releasesOn(face));
            assertFalse(machine.receivesOn(face));
            assertEquals(0, machine.roomFor(magic, face));
            assertEquals(0, machine.insert(magic, 1, face, false));
        }
        assertEquals(0, machine.extract(other, 1, Direction.EAST, false));
        assertEquals(0, machine.extract(Holder.direct(magic.value()), 1, Direction.EAST, false));
        assertEquals(0, machine.extract(magic, -1, Direction.EAST, true));
        assertEquals(0, machine.extract(magic, 1, Direction.WEST, true));
        clearInvocations(level);
        assertEquals(3, machine.extract(magic, 9, Direction.EAST, true));
        assertEquals(3, machine.storedEssentia());
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
        assertEquals(3, machine.extract(magic, 9, Direction.EAST, false));
        assertEquals(0, machine.storedEssentia());
        assertNull(machine.heldAspect(Direction.EAST));
        verify(level).sendBlockUpdated(BlockPos.ZERO, machine.getBlockState(), machine.getBlockState(), 2);
    }

    @Test
    void nativeNbtIncludesAllCountersInClientUpdatesAndClampsStoredValues() {
        var machine = machine(Integer.MAX_VALUE, -2, -3);
        assertEquals(BlockEntityFluxScrubber.essentiaCapacity(), machine.storedEssentia());
        assertEquals(0, machine.charges());
        assertEquals(0, machine.power());
        CompoundTag saved = machine.getUpdateTag(registries);
        assertEquals(Set.of("Essentia", "Charges", "Power"), saved.getAllKeys());
        var loaded = machine(0, 0, 0);
        loaded.loadAdditional(saved, registries);
        assertEquals(saved, loaded.getUpdateTag(registries));
        assertNotNull(loaded.getUpdatePacket());
        loaded.loadAdditional(new CompoundTag(), registries);
        assertEquals(0, loaded.storedEssentia());
        assertEquals(0, loaded.charges());
        assertEquals(0, loaded.power());
    }

    @Test
    void partialRelayPowerRequestsOnlyNodeRemainderAndFailedDrawWaitsTwentyTicks() {
        var machine = machine(0, 0, 0);
        try (var network = mockStatic(VisRelayNetwork.class);
                var flux = mockStatic(PhysicalFlux.class)) {
            network.when(() -> VisRelayNetwork.drainEverySourceNear(level, BlockPos.ZERO, TTAspects.AER, 10))
                    .thenReturn(3);
            network.when(() -> VisRelayNetwork.drainNodesNear(level, BlockPos.ZERO, TTAspects.AER, 7))
                    .thenReturn(2);
            tick(machine, 1);
            assertEquals(5, machine.power());
            network.verify(() -> VisRelayNetwork.drainNodesNear(level, BlockPos.ZERO, TTAspects.AER, 7));
            tick(machine, 1);
            network.verify(
                    () -> VisRelayNetwork.drainEverySourceNear(level, BlockPos.ZERO, TTAspects.AER, 10), times(1));
        }
        var empty = machine(0, 0, 0);
        try (var network = mockStatic(VisRelayNetwork.class)) {
            tick(empty, 21);
            network.verify(
                    () -> VisRelayNetwork.drainEverySourceNear(level, BlockPos.ZERO, TTAspects.AER, 10), times(1));
            tick(empty, 1);
            network.verify(
                    () -> VisRelayNetwork.drainEverySourceNear(level, BlockPos.ZERO, TTAspects.AER, 10), times(2));
            assertEquals(0, empty.power());
        }
    }

    @Test
    void completeRelayDrawSkipsNodesAndSubWorkPowerNeverScans() {
        var machine = machine(0, 0, 0);
        try (var network = mockStatic(VisRelayNetwork.class);
                var flux = mockStatic(PhysicalFlux.class)) {
            network.when(() -> VisRelayNetwork.drainEverySourceNear(level, BlockPos.ZERO, TTAspects.AER, 10))
                    .thenReturn(10);
            tick(machine, 1);
            assertEquals(10, machine.power());
            network.verify(() -> VisRelayNetwork.drainNodesNear(any(), any(), any(), anyInt()), never());
        }
        var insufficient = machine(0, 0, 4);
        clearInvocations(level);
        try (var network = mockStatic(VisRelayNetwork.class);
                var flux = mockStatic(PhysicalFlux.class)) {
            tick(insufficient, 1);
            flux.verifyNoInteractions();
            verify(level, never()).hasChunkAt(any());
        }
    }

    @Test
    void scanChecksSixteenInteriorCellsWithoutLoadingChunksAndRejectsNonCoprimeStride() {
        var machine = machine(0, 0, 5);
        when(random.nextInt(CELLS - 1)).thenReturn(2, 0);
        Set<BlockPos> visited = new HashSet<>();
        when(level.hasChunkAt(any())).thenAnswer(call -> {
            BlockPos pos = call.getArgument(0);
            assertTrue(visited.add(pos.immutable()));
            assertTrue(pos.distSqr(BlockPos.ZERO) < 16 * 16);
            return false;
        });
        try (var flux = mockStatic(PhysicalFlux.class)) {
            tick(machine, 1);
            flux.verifyNoInteractions();
        }
        assertEquals(16, visited.size());
        verify(random, times(2)).nextInt(CELLS - 1);
        verify(level, never()).getBlockState(any());
        assertEquals(5, machine.power());
        assertEquals(0, machine.charges());
    }

    @Test
    void successfulCleanupConsumesFivePowerStopsScanSendsSparkleAndRollsImmediately() {
        int perRoll = ThaumaturgeCommonConfig.FLUX_SCRUBBER_CHARGES_PER_ROLL.get();
        int yield = ThaumaturgeCommonConfig.FLUX_SCRUBBER_ESSENTIA_PER_ROLL.get();
        int capacity = BlockEntityFluxScrubber.essentiaCapacity();
        var machine = machine(0, perRoll - 1, 5);
        try (var flux = mockStatic(PhysicalFlux.class);
                var effects = mockStatic(Effects.class)) {
            flux.when(() -> PhysicalFlux.isScrubbable(any())).thenReturn(true);
            flux.when(() -> PhysicalFlux.reduce(level, BlockPos.ZERO, 1)).thenReturn(1);
            var sparkle = mock(Effects.SimpleSparkle.class, RETURNS_SELF);
            effects.when(() -> Effects.simpleSparkle(level, Vec3.atCenterOf(BlockPos.ZERO)))
                    .thenReturn(sparkle);
            tick(machine, 1);
            assertEquals(0, machine.power());
            assertEquals(0, machine.charges());
            assertEquals(Math.min(capacity, yield), machine.storedEssentia());
            verify(level, times(1)).hasChunkAt(any());
            verify(sparkle).color(0xDD / 255.0F, 0.0F, 1.0F);
            verify(sparkle).scale(0.8F);
            verify(sparkle).send();
            verify(level).sendBlockUpdated(BlockPos.ZERO, machine.getBlockState(), machine.getBlockState(), 2);
        }
    }

    @Test
    void pendingChargesRollOnlyAfterCleanupConsumeAllRollsAndClipYield() {
        int perRoll = ThaumaturgeCommonConfig.FLUX_SCRUBBER_CHARGES_PER_ROLL.get();
        int capacity = BlockEntityFluxScrubber.essentiaCapacity();
        int pending = perRoll * 3;
        var machine = machine(capacity - 1, pending, 5);
        try (var flux = mockStatic(PhysicalFlux.class);
                var effects = mockStatic(Effects.class)) {
            tick(machine, 1);
            assertEquals(pending, machine.charges());
            assertEquals(capacity - 1, machine.storedEssentia());
            flux.when(() -> PhysicalFlux.isScrubbable(any())).thenReturn(true);
            flux.when(() -> PhysicalFlux.reduce(eq(level), any(), eq(1))).thenReturn(1);
            effects.when(() -> Effects.simpleSparkle(eq(level), any()))
                    .thenReturn(mock(Effects.SimpleSparkle.class, RETURNS_SELF));
            tick(machine, 1);
            assertEquals((pending + 1) % perRoll, machine.charges());
            assertEquals(capacity, machine.storedEssentia());
            verify(random, times((pending + 1) / perRoll)).nextDouble();
        }
    }

    @Test
    void failedRemovalDoesNotConsumePowerAndFailedRollStillConsumesCharge() {
        int perRoll = ThaumaturgeCommonConfig.FLUX_SCRUBBER_CHARGES_PER_ROLL.get();
        var machine = machine(0, perRoll - 1, 5);
        try (var flux = mockStatic(PhysicalFlux.class);
                var effects = mockStatic(Effects.class)) {
            flux.when(() -> PhysicalFlux.isScrubbable(any())).thenReturn(true);
            tick(machine, 1);
            assertEquals(5, machine.power());
            assertEquals(perRoll - 1, machine.charges());
            effects.verifyNoInteractions();
            flux.when(() -> PhysicalFlux.reduce(eq(level), any(), eq(1))).thenReturn(1);
            effects.when(() -> Effects.simpleSparkle(eq(level), any()))
                    .thenReturn(mock(Effects.SimpleSparkle.class, RETURNS_SELF));
            when(random.nextDouble()).thenReturn(1.0);
            tick(machine, 1);
            assertEquals(0, machine.charges());
            assertEquals(0, machine.storedEssentia());
        }
    }

    @Test
    void scanReseedsAtCycleEndInsideTheSameTickAndStaysWithinStrictSphere() {
        var machine = machine(0, 0, 5);
        when(random.nextInt(CELLS)).thenReturn(CELLS - 1, CENTER);
        try (var flux = mockStatic(PhysicalFlux.class)) {
            tick(machine, 2246);
            verify(random, times(1)).nextInt(CELLS);
            tick(machine, 1);
            verify(random, times(2)).nextInt(CELLS);
            assertEquals(5, machine.power());
        }
    }

    @Test
    void nativeBlockHasOnlyServerTickerAndServerEntryPointIgnoresClientLevel() {
        var machine = machine(0, 0, 5);
        Level client = mock(Level.class);
        when(client.isClientSide()).thenReturn(true);
        var block = (BlockFluxScrubber) TTBlocks.FLUX_SCRUBBER.get();
        assertNull(block.getTicker(client, machine.getBlockState(), TTBlockEntities.FLUX_SCRUBBER.get()));
        assertNotNull(block.getTicker(level, machine.getBlockState(), TTBlockEntities.FLUX_SCRUBBER.get()));
        clearInvocations(client);
        BlockEntityFluxScrubber.serverTick(client, BlockPos.ZERO, machine.getBlockState(), machine);
        verifyNoInteractions(client);
        assertEquals(5, machine.power());
    }
}
