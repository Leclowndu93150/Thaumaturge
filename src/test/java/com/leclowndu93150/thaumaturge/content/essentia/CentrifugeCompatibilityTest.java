package com.leclowndu93150.thaumaturge.content.essentia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CentrifugeCompatibilityTest {
    private ServerLevel level;
    private RegistryAccess registries;
    private Holder<IAspect> primal;
    private Holder<IAspect> compound;
    private IAspect compoundValue;
    private RandomSource random;
    private long gameTime;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        IAspect primalValue = mock(IAspect.class);
        when(primalValue.isPrimal()).thenReturn(true);
        Registry.register(registry, TTAspects.ORDO, primalValue);
        primal = registry.getHolderOrThrow(TTAspects.ORDO);
        compoundValue = mock(IAspect.class);
        when(compoundValue.components()).thenReturn(List.of(primal, primal));
        Registry.register(registry, TTAspects.PRAECANTATIO, compoundValue);
        compound = registry.getHolderOrThrow(TTAspects.PRAECANTATIO);
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        level = mock(ServerLevel.class);
        when(level.registryAccess()).thenReturn(registries);
        random = mock(RandomSource.class);
        when(level.getRandom()).thenReturn(random);
        when(level.getGameTime()).thenAnswer(invocation -> gameTime);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
    }

    private BlockEntityCentrifuge machine() {
        var machine = new BlockEntityCentrifuge(
                BlockPos.ZERO, TTBlocks.CENTRIFUGE.get().defaultBlockState());
        machine.setLevel(level);
        return machine;
    }

    private void tick(BlockEntityCentrifuge machine, int count) {
        for (int i = 0; i < count; i++) {
            gameTime++;
            BlockEntityCentrifuge.serverTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
        }
    }

    @Test
    void processingPausesWithRedstoneAndProducesOneComponentAfterCountdown() {
        var machine = machine();
        assertEquals(0, machine.insert(primal, 1, Direction.DOWN, false));
        assertEquals(0, machine.insert(Holder.direct(compound.value()), 1, Direction.DOWN, false));
        assertEquals(1, machine.insert(compound, 3, Direction.DOWN, false));
        assertEquals(0, machine.insert(compound, 1, Direction.DOWN, false));
        assertEquals(64, machine.pull(Direction.DOWN).strength());
        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
        tick(machine, 100);
        assertEquals(0, machine.heldAmount(Direction.UP));
        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(false);
        tick(machine, 39);
        assertEquals(0, machine.heldAmount(Direction.UP));
        tick(machine, 1);
        assertEquals(primal, machine.heldAspect(Direction.UP));
        assertEquals(1, machine.heldAmount(Direction.UP));
        assertEquals(1, machine.extract(primal, 2, Direction.UP, true));
        assertEquals(0, machine.extract(primal, 1, Direction.DOWN, false));
        assertEquals(0, machine.extract(compound, 1, Direction.UP, false));
        assertEquals(1, machine.extract(primal, 1, Direction.UP, false));
        assertEquals(0, machine.heldAmount(Direction.UP));
    }

    @Test
    void nativeInputAndOutputKeysRoundTrip() {
        var machine = machine();
        CompoundTag tag = new CompoundTag();
        TTNbt.store(tag, "AspectIn", LegacyIds.ASPECT_KEY_CODEC, registries, TTAspects.PRAECANTATIO);
        TTNbt.store(tag, "AspectOut", LegacyIds.ASPECT_KEY_CODEC, registries, TTAspects.ORDO);
        machine.loadAdditional(tag, registries);
        assertTrue(machine.isSpinning());
        assertEquals(primal, machine.heldAspect(Direction.UP));
        assertEquals(
                Set.of("AspectIn", "AspectOut"),
                machine.getUpdateTag(registries).getAllKeys());
        assertEquals(tag, machine.getUpdateTag(registries));
        tick(machine, 50);
        assertTrue(machine.isSpinning());
        assertEquals(1, machine.heldAmount(Direction.UP));
    }

    @Test
    void pullsOnlyCompoundStockFromLoadedLowerNeighborsWithWeakerSuction() {
        var machine = machine();
        var source = mock(IEssentiaConduit.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.below(), Direction.UP))
                .thenReturn(source);
        when(source.connectsOn(Direction.UP)).thenReturn(true);
        when(source.releasesOn(Direction.UP)).thenReturn(true);
        when(source.pull(Direction.UP)).thenReturn(EssentiaPull.NONE);
        when(source.heldAspect(Direction.UP)).thenReturn(compound);
        when(source.heldAmount(Direction.UP)).thenReturn(1);
        when(source.extract(compound, 1, Direction.UP, false)).thenReturn(1);
        tick(machine, 4);
        verifyNoInteractions(source);
        when(level.hasChunk(0, 0)).thenReturn(false);
        tick(machine, 1);
        verifyNoInteractions(source);
        when(level.hasChunk(0, 0)).thenReturn(true);
        when(source.pull(Direction.UP)).thenReturn(EssentiaPull.any(128));
        tick(machine, 5);
        verify(source, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(source.pull(Direction.UP)).thenReturn(EssentiaPull.NONE);
        tick(machine, 5);
        assertTrue(machine.isSpinning());
        verify(source).extract(compound, 1, Direction.UP, false);
    }

    @Test
    void removingStoredOutputPollutesButChunkUnloadDoesNot() {
        var machine = machine();
        machine.insert(compound, 1, Direction.DOWN, false);
        tick(machine, 40);
        var state = machine.getBlockState();
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(machine);
        try (var aura = mockStatic(AuraHelper.class)) {
            machine.setRemoved();
            aura.verifyNoInteractions();
            ((BlockCentrifuge) state.getBlock()).onRemove(state, level, BlockPos.ZERO, state, false);
            aura.verifyNoInteractions();
            ((BlockCentrifuge) state.getBlock())
                    .onRemove(state, level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
            aura.verify(() -> AuraHelper.polluteAura(level, BlockPos.ZERO, 1.0F, true));
            assertEquals(0, machine.heldAmount(Direction.UP));
            machine.releaseStoredProductOnBreak(BlockPos.ZERO);
            aura.verifyNoMoreInteractions();
        }
    }

    @Test
    void missingLoadedAspectCannotCrashComponentSelection() {
        var machine = machine();
        CompoundTag tag = new CompoundTag();
        TTNbt.store(tag, "AspectIn", LegacyIds.ASPECT_KEY_CODEC, registries, TTAspects.IGNIS);
        machine.loadAdditional(tag, registries);
        tick(machine, 40);
        assertFalse(machine.isSpinning());
        assertEquals(0, machine.heldAmount(Direction.UP));
    }

    @Test
    void simulationCapacityAndFaceContractsLeaveStateUnchanged() {
        var machine = machine();
        assertEquals(0, machine.insert(compound, 0, Direction.DOWN, true));
        assertEquals(0, machine.insert(compound, 1, Direction.UP, true));
        assertEquals(0, machine.roomFor(primal, Direction.DOWN));
        assertEquals(0, machine.roomFor(compound, Direction.NORTH));
        assertEquals(1, machine.roomFor(compound, Direction.DOWN));
        assertEquals(1, machine.insert(compound, 3, Direction.DOWN, true));
        assertFalse(machine.isSpinning());
        assertEquals(0, machine.heldAmount(Direction.DOWN));
        assertEquals(128, machine.pull(null).strength());
        assertEquals(EssentiaPull.NONE, machine.pull(Direction.UP));
        assertEquals(0, machine.pullThreshold());
        for (Direction side : Direction.values()) {
            assertEquals(side == Direction.DOWN || side == Direction.UP, machine.connectsOn(side));
            assertEquals(side == Direction.DOWN, machine.receivesOn(side));
            assertEquals(side == Direction.UP, machine.releasesOn(side));
        }
        machine.insert(compound, 1, Direction.DOWN, false);
        assertEquals(compound, machine.heldAspect(Direction.DOWN));
        assertEquals(1, machine.heldAmount(Direction.DOWN));
        assertEquals(0, machine.roomFor(compound, Direction.DOWN));
        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
        assertFalse(machine.isSpinning());
        assertEquals(EssentiaPull.NONE, machine.pull(null));
    }

    @Test
    void loadedInputRestartsFullCountdownAndOccupiedOutputAllowsPendingInput() {
        var machine = machine();
        CompoundTag tag = new CompoundTag();
        TTNbt.store(tag, "AspectIn", LegacyIds.ASPECT_KEY_CODEC, registries, TTAspects.PRAECANTATIO);
        machine.loadAdditional(tag, registries);
        tick(machine, 39);
        assertEquals(0, machine.heldAmount(Direction.UP));
        tick(machine, 1);
        assertEquals(1, machine.heldAmount(Direction.UP));
        assertEquals(64, machine.pull(Direction.DOWN).strength());
        assertEquals(1, machine.roomFor(compound, Direction.DOWN));
        assertEquals(1, machine.insert(compound, 1, Direction.DOWN, false));
        tick(machine, 50);
        assertEquals(1, machine.heldAmount(Direction.DOWN));
        assertEquals(1, machine.heldAmount(Direction.UP));
        assertEquals(0, machine.extract(primal, 0, Direction.UP, true));
        machine.extract(primal, 1, Direction.UP, false);
        tick(machine, 1);
        assertEquals(0, machine.heldAmount(Direction.DOWN));
        assertEquals(1, machine.heldAmount(Direction.UP));
    }

    @Test
    void selectionUsesAllComponentsIncludingSingleComponentAndClearsEmptyInput() {
        var machine = machine();
        when(compoundValue.components()).thenReturn(List.of(primal, primal, compound));
        when(random.nextInt(3)).thenReturn(2);
        machine.insert(compound, 1, Direction.DOWN, false);
        tick(machine, 40);
        assertEquals(compound, machine.heldAspect(Direction.UP));
        machine.extract(compound, 1, Direction.UP, false);
        when(compoundValue.components()).thenReturn(List.of(primal));
        machine.insert(compound, 1, Direction.DOWN, false);
        tick(machine, 40);
        assertEquals(primal, machine.heldAspect(Direction.UP));
        machine.extract(primal, 1, Direction.UP, false);
        when(compoundValue.components()).thenReturn(List.of());
        machine.insert(compound, 1, Direction.DOWN, false);
        tick(machine, 40);
        assertFalse(machine.isSpinning());
        assertEquals(0, machine.heldAmount(Direction.UP));
    }

    @Test
    void sourceConnectionRegistrationAndPullThresholdPreventExtraction() {
        var machine = machine();
        var source = mock(IEssentiaConduit.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.below(), Direction.UP))
                .thenReturn(source);
        when(source.releasesOn(Direction.UP)).thenReturn(true);
        when(source.heldAspect(Direction.UP)).thenReturn(compound);
        when(source.heldAmount(Direction.UP)).thenReturn(1);
        when(source.pull(Direction.UP)).thenReturn(EssentiaPull.NONE);
        tick(machine, 5);
        verify(source, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(source.connectsOn(Direction.UP)).thenReturn(true);
        when(source.heldAspect(Direction.UP)).thenReturn(Holder.direct(compound.value()));
        tick(machine, 5);
        verify(source, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(source.heldAspect(Direction.UP)).thenReturn(compound);
        when(source.pullThreshold()).thenReturn(129);
        tick(machine, 5);
        verify(source, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(source.pullThreshold()).thenReturn(128);
        when(source.extract(compound, 1, Direction.UP, false)).thenReturn(1);
        tick(machine, 5);
        assertTrue(machine.isSpinning());
    }

    @Test
    void rotorWrapsAndServerPlaysPumpWhileClientOnlyAnimates() {
        var machine = machine();
        machine.insert(compound, 1, Direction.DOWN, false);
        machine.rotation = 170.0F;
        machine.rotationSpeed = 24.0F;
        when(random.nextFloat()).thenReturn(0.5F);
        tick(machine, 1);
        assertEquals(194.0F, machine.rotation);
        verify(level).playSound(null, BlockPos.ZERO, TTSounds.PUMP.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
        clearInvocations(level);
        machine.rotation = 350.0F;
        BlockEntityCentrifuge.clientTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
        assertEquals(14.0F, machine.rotation);
        verify(level, never()).playSound(null, BlockPos.ZERO, TTSounds.PUMP.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
        BlockEntityCentrifuge.clientTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
        assertEquals(23.4F, machine.rotationSpeed, 0.0001F);
        var client = mock(Level.class);
        machine.setLevel(client);
        BlockEntityCentrifuge.serverTick(client, BlockPos.ZERO, machine.getBlockState(), machine);
        assertEquals(1, machine.heldAmount(Direction.DOWN));
    }
}
