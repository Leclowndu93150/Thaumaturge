package com.leclowndu93150.thaumaturge.content.device;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaTransfer;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CondenserConduitCutoffTest {
    private ServerLevel level;
    private RandomSource random;
    private RegistryAccess registries;
    private Holder<IAspect> cleanAspect;
    private Holder<IAspect> vitium;
    private final Map<BlockPos, BlockState> blocks = new HashMap<>();

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.ORDO, mock(IAspect.class));
        Registry.register(registry, TTAspects.VITIUM, mock(IAspect.class));
        cleanAspect = registry.getHolderOrThrow(TTAspects.ORDO);
        vitium = registry.getHolderOrThrow(TTAspects.VITIUM);
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        level = mock(ServerLevel.class);
        random = mock(RandomSource.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.getRandom()).thenReturn(random);
        when(random.nextInt(anyInt())).thenReturn(1);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getBlockState(any()))
                .thenAnswer(call -> blocks.getOrDefault(call.getArgument(0), Blocks.AIR.defaultBlockState()));
        when(level.setBlock(any(), any(), anyInt())).thenAnswer(call -> {
            blocks.put(call.getArgument(0), call.getArgument(1));
            return true;
        });
    }

    private BlockEntityCondenser machine() {
        var machine =
                new BlockEntityCondenser(BlockPos.ZERO, TTBlocks.CONDENSER.get().defaultBlockState());
        machine.setLevel(level);
        blocks.put(BlockPos.ZERO, machine.getBlockState());
        return machine;
    }

    private void lattice(BlockPos... cells) {
        for (BlockPos cell : cells)
            blocks.put(cell, TTBlocks.CONDENSER_LATTICE.get().defaultBlockState());
    }

    private void tick(BlockEntityCondenser machine, int count, boolean enabled) {
        var state = machine.getBlockState().setValue(BlockStateProperties.ENABLED, enabled);
        try (var effects = mockStatic(Effects.class)) {
            var spark = mock(Effects.Spark.class, RETURNS_SELF);
            effects.when(() -> Effects.spark(eq(level), any())).thenReturn(spark);
            for (int i = 0; i < count; i++) BlockEntityCondenser.serverTick(level, BlockPos.ZERO, state, machine);
        }
    }

    private CompoundTag save(BlockEntityCondenser machine) {
        CompoundTag tag = new CompoundTag();
        machine.saveAdditional(tag, registries);
        return tag;
    }

    @Test
    void simulationCountsCapacityAndEveryFacePreserveFinalContracts() {
        var machine = machine();
        assertEquals(100, machine.roomFor(cleanAspect, Direction.NORTH));
        assertEquals(100, machine.insert(cleanAspect, 200, Direction.NORTH, true));
        assertEquals(0, save(machine).getInt("essentia"));
        assertEquals(0, machine.insert(cleanAspect, -1, Direction.NORTH, false));
        assertEquals(0, machine.insert(cleanAspect, 1, Direction.DOWN, false));
        assertEquals(97, machine.insert(cleanAspect, 97, Direction.NORTH, false));
        assertEquals(3, machine.insert(cleanAspect, 20, Direction.SOUTH, false));
        assertEquals(0, machine.roomFor(cleanAspect, Direction.NORTH));
        assertEquals(EssentiaPull.NONE, machine.pull(null));
        assertEquals(0, machine.insert(cleanAspect, 1, Direction.NORTH, false));
        for (Direction face : Direction.values()) {
            assertEquals(face != Direction.UP, machine.connectsOn(face));
            assertEquals(face.getAxis() != Direction.Axis.Y, machine.receivesOn(face));
            assertEquals(face == Direction.DOWN, machine.releasesOn(face));
        }
        CompoundTag stored = new CompoundTag();
        stored.putInt("essentia", 12);
        stored.putInt("flux", 6);
        machine.loadAdditional(stored, registries);
        assertEquals(stored, save(machine));
        assertEquals(vitium, machine.heldAspect(null));
        assertEquals(6, machine.heldAmount(null));
        assertEquals(0, machine.heldAmount(Direction.NORTH));
        assertNull(machine.heldAspect(Direction.NORTH));
        assertEquals(0, machine.extract(cleanAspect, 1, Direction.DOWN, false));
        assertEquals(0, machine.extract(vitium, -1, Direction.DOWN, true));
        assertEquals(0, machine.extract(vitium, 1, Direction.UP, false));
        assertEquals(6, machine.extract(vitium, 20, Direction.DOWN, true));
        assertEquals(6, machine.heldAmount(Direction.DOWN));
        assertEquals(6, machine.extract(vitium, 20, Direction.DOWN, false));
        assertEquals(0, machine.heldAmount(null));
        assertEquals(128, machine.pull(null).strength());
        assertEquals(EssentiaPull.NONE, machine.pull(Direction.DOWN));
    }

    @Test
    void floodScoresTerminalCellsCleanRoutesAndForeignCondensers() {
        var machine = machine();
        machine.triggerCheck();
        assertEquals(0, machine.interval());
        lattice(BlockPos.ZERO.above());
        machine.triggerCheck();
        assertEquals(587, machine.interval());
        assertEquals(5, machine.cost());
        lattice(BlockPos.ZERO.above(2), BlockPos.ZERO.above(3));
        machine.triggerCheck();
        assertEquals(564, machine.interval());
        assertEquals(5, machine.cost());
        blocks.put(
                BlockPos.ZERO.above(2), TTBlocks.CONDENSER_LATTICE_DIRTY.get().defaultBlockState());
        machine.triggerCheck();
        assertEquals(575, machine.interval());
        blocks.put(BlockPos.ZERO.above(2), TTBlocks.CONDENSER_LATTICE.get().defaultBlockState());
        lattice(BlockPos.ZERO.above().east());
        blocks.put(BlockPos.ZERO.east(), TTBlocks.CONDENSER.get().defaultBlockState());
        machine.triggerCheck();
        assertEquals(0, machine.interval());
    }

    @Test
    void vitiumTransferSimulationAcceptsWithoutClogAndCommitAcceptsAndClogs() {
        var machine = machine();
        lattice(BlockPos.ZERO.above(), BlockPos.ZERO.above(2), BlockPos.ZERO.above(3));
        var connected = blocks.get(BlockPos.ZERO.above(2)).setValue(PipeBlock.UP, true);
        blocks.put(BlockPos.ZERO.above(2), connected);
        machine.triggerCheck();

        var source = mock(IEssentiaConduit.class);
        when(source.releasesOn(Direction.SOUTH)).thenReturn(true);
        when(source.extract(vitium, 7, Direction.SOUTH, true)).thenReturn(7);
        when(source.extract(vitium, 7, Direction.SOUTH, false)).thenReturn(7);
        assertEquals(7, EssentiaTransfer.transfer(source, Direction.SOUTH, machine, Direction.NORTH, vitium, 7, true));
        verify(level, never()).setBlock(any(), any(), anyInt());
        verify(random, never()).nextInt(17);

        when(random.nextInt(17)).thenReturn(1);
        assertEquals(7, EssentiaTransfer.transfer(source, Direction.SOUTH, machine, Direction.NORTH, vitium, 7, false));
        assertTrue(blocks.get(BlockPos.ZERO.above(2)).is(TTBlocks.CONDENSER_LATTICE_DIRTY.get()));
        assertTrue(blocks.get(BlockPos.ZERO.above(2)).getValue(PipeBlock.UP));
        assertEquals(0, save(machine).getInt("essentia"));
        assertEquals(575, machine.interval());
        verify(random).nextInt(17);
        verify(source, times(2)).extract(vitium, 7, Direction.SOUTH, true);
        verify(source).extract(vitium, 7, Direction.SOUTH, false);
    }

    @Test
    void scansLoadedCellsOnlyAndVitiumOfferClogsWithoutExtraction() {
        var machine = machine();
        BlockPos unloaded = BlockPos.ZERO.above().west();
        lattice(BlockPos.ZERO.above(), unloaded);
        when(level.hasChunk(-1, 0)).thenReturn(false);
        machine.triggerCheck();
        verify(level, never()).getBlockState(unloaded);
        assertEquals(587, machine.interval());
        var source = mock(IEssentiaConduit.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.north(), Direction.SOUTH))
                .thenReturn(source);
        when(source.releasesOn(Direction.SOUTH)).thenReturn(true);
        when(source.heldAmount(Direction.SOUTH)).thenReturn(1);
        when(source.heldAspect(Direction.SOUTH)).thenReturn(vitium);
        when(source.pull(Direction.SOUTH)).thenReturn(EssentiaPull.NONE);
        when(random.nextInt(1)).thenReturn(0);
        tick(machine, 5, true);
        verify(source, never()).extract(any(), anyInt(), any(), anyBoolean());
        assertTrue(blocks.get(BlockPos.ZERO.above()).is(TTBlocks.CONDENSER_LATTICE_DIRTY.get()));
    }

    @Test
    void preflightBlockingNeighborStopsAllDrawingAndRedstoneStopsWork() {
        var machine = machine();
        lattice(BlockPos.ZERO.above());
        var willing = mock(IEssentiaConduit.class);
        var blocked = mock(IEssentiaConduit.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.north(), Direction.SOUTH))
                .thenReturn(willing);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.south(), Direction.NORTH))
                .thenReturn(blocked);
        when(willing.releasesOn(Direction.SOUTH)).thenReturn(true);
        when(willing.heldAmount(Direction.SOUTH)).thenReturn(1);
        when(willing.heldAspect(Direction.SOUTH)).thenReturn(cleanAspect);
        when(willing.pull(Direction.SOUTH)).thenReturn(EssentiaPull.NONE);
        when(willing.extract(cleanAspect, 1, Direction.SOUTH, false)).thenReturn(1);
        tick(machine, 5, true);
        verify(willing, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(blocked.releasesOn(Direction.NORTH)).thenReturn(true);
        tick(machine, 5, false);
        verify(willing, never()).extract(any(), anyInt(), any(), anyBoolean());
        tick(machine, 5, true);
        assertEquals(1, save(machine).getInt("essentia"));
        verify(willing).extract(cleanAspect, 1, Direction.SOUTH, false);
    }

    @Test
    void conversionUsesCostAuraAndServerSparkleAndStopsAtFluxCapacity() {
        var machine = machine();
        lattice(BlockPos.ZERO.above());
        machine.triggerCheck();
        machine.insert(cleanAspect, 10, Direction.NORTH, false);
        try (var aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.getFlux(level, BlockPos.ZERO)).thenReturn(1.0F);
            tick(machine, 587, true);
            assertEquals(5, save(machine).getInt("essentia"));
            assertEquals(1, machine.heldAmount(null));
            aura.verify(() -> AuraHelper.drainFlux(level, BlockPos.ZERO, 1.0F, false));
        }
        when(random.nextInt(anyInt())).thenReturn(0);
        try (var effects = mockStatic(Effects.class)) {
            var spark = mock(Effects.Spark.class, RETURNS_SELF);
            effects.when(() -> Effects.spark(level, Vec3.atCenterOf(BlockPos.ZERO.above())))
                    .thenReturn(spark);
            BlockEntityCondenser.serverTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
            effects.verify(() -> Effects.spark(level, Vec3.atCenterOf(BlockPos.ZERO.above())));
            verify(spark).color(0.55F, 0.55F, 0.55F);
            verify(spark).size(0.6F);
            verify(spark).send();
        }
        CompoundTag full = new CompoundTag();
        full.putInt("essentia", 10);
        full.putInt("flux", 100);
        machine.loadAdditional(full, registries);
        try (var aura = mockStatic(AuraHelper.class)) {
            tick(machine, 587, true);
            aura.verifyNoInteractions();
            assertEquals(100, machine.heldAmount(null));
        }
    }

    @Test
    void onlyActualRemovalReturnsStoredFluxToAura() {
        var machine = machine();
        CompoundTag tag = new CompoundTag();
        tag.putInt("flux", 4);
        machine.loadAdditional(tag, registries);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(machine);
        var block = (BlockCondenser) machine.getBlockState().getBlock();
        try (var aura = mockStatic(AuraHelper.class)) {
            machine.setRemoved();
            aura.verifyNoInteractions();
            block.onRemove(machine.getBlockState(), level, BlockPos.ZERO, machine.getBlockState(), false);
            aura.verifyNoInteractions();
            block.onRemove(machine.getBlockState(), level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
            aura.verify(() -> AuraHelper.polluteAura(level, BlockPos.ZERO, 4.0F, true));
        }
    }
}
