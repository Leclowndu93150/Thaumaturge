package com.leclowndu93150.thaumaturge.content.essentia.crystalizer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrystalizerCompatibilityTest {
    private ServerLevel level;
    private RegistryAccess registries;
    private Holder<IAspect> fire;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        IAspect value = mock(IAspect.class);
        when(value.color()).thenReturn(0xFF0000);
        Registry.register(registry, TTAspects.IGNIS, value);
        fire = registry.getHolderOrThrow(TTAspects.IGNIS);
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        level = mock(ServerLevel.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.getRandom()).thenReturn(mock(RandomSource.class));
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
    }

    private BlockEntityEssentiaCrystalizer machine() {
        var state = TTBlocks.ESSENTIA_CRYSTALIZER
                .get()
                .defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.EAST);
        var machine = new BlockEntityEssentiaCrystalizer(BlockPos.ZERO, state);
        machine.setLevel(level);
        return machine;
    }

    private void tick(BlockEntityEssentiaCrystalizer machine) {
        BlockEntityEssentiaCrystalizer.serverTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
    }

    @Test
    void capacityAndSuctionRemainSidedAndSinglePoint() {
        var machine = machine();
        assertEquals(128, machine.pull(Direction.EAST).strength());
        assertEquals(0, machine.pull(Direction.WEST).strength());
        assertEquals(0, machine.insert(fire, 1, Direction.WEST, false));
        assertEquals(0, machine.insert(fire, 0, Direction.EAST, false));
        assertEquals(0, machine.insert(Holder.direct(fire.value()), 1, Direction.EAST, false));
        assertEquals(1, machine.insert(fire, 8, Direction.EAST, false));
        assertEquals(1, machine.heldAmount(null));
        assertEquals(64, machine.pull(Direction.EAST).strength());
        assertEquals(0, machine.insert(fire, 1, Direction.EAST, false));
        assertEquals(0, machine.extract(fire, 1, Direction.EAST, false));
        assertEquals(0, machine.roomFor(fire, Direction.EAST));
        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
        assertEquals(0, machine.pull(Direction.EAST).strength());
    }

    @Test
    void insertionSimulationDoesNotPublishOrStartProcessing() {
        var machine = machine();
        clearInvocations(level);
        assertEquals(1, machine.roomFor(fire, Direction.EAST));
        assertEquals(0, machine.roomFor(fire, Direction.WEST));
        assertEquals(1, machine.insert(fire, 7, Direction.EAST, true));
        assertNull(machine.aspectKey());
        assertEquals(0, machine.progress());
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
        assertEquals(1, machine.insert(fire, 7, Direction.EAST, false));
        assertEquals(TTAspects.IGNIS, machine.aspectKey());
        assertEquals(0, machine.insert(fire, 1, Direction.EAST, true));
        assertEquals(0, machine.extract(fire, 1, Direction.EAST, true));
        verify(level).sendBlockUpdated(eq(BlockPos.ZERO), any(), any(), eq(2));
    }

    @Test
    void nativeAspectFlagAndProgressRoundTripAndClamp() {
        var machine = machine();
        assertEquals(Set.of("HasAspect"), machine.getUpdateTag(registries).getAllKeys());
        machine.insert(fire, 1, Direction.EAST, false);
        CompoundTag tag = machine.getUpdateTag(registries);
        assertEquals(Set.of("HasAspect", "Aspect", "Progress"), tag.getAllKeys());
        tag.putInt("Progress", 500);
        var loaded = machine();
        loaded.loadAdditional(tag, registries);
        assertEquals(TTAspects.IGNIS, loaded.aspectKey());
        assertEquals(200, loaded.progress());
        tag.putBoolean("HasAspect", false);
        loaded.loadAdditional(tag, registries);
        assertNull(loaded.aspectKey());
        assertEquals(0, loaded.progress());
        tag.remove("HasAspect");
        tag.putInt("Progress", -3);
        loaded.loadAdditional(tag, registries);
        assertEquals(TTAspects.IGNIS, loaded.aspectKey());
        assertEquals(0, loaded.progress());
    }

    @Test
    void drawUsesWorldTimeAndDoesNotQueryAnUnloadedNeighbor() {
        var machine = machine();
        var source = mock(IEssentiaConduit.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.east(), Direction.WEST))
                .thenReturn(source);
        when(source.releasesOn(Direction.WEST)).thenReturn(true);
        when(source.pull(Direction.WEST)).thenReturn(EssentiaPull.NONE);
        when(source.heldAspect(Direction.WEST)).thenReturn(fire);
        when(source.heldAmount(Direction.WEST)).thenReturn(2);
        when(source.extract(fire, 1, Direction.WEST, false)).thenReturn(1);
        when(level.getGameTime()).thenReturn(4L);
        tick(machine);
        verifyNoInteractions(source);
        when(level.getGameTime()).thenReturn(5L);
        when(level.hasChunk(0, 0)).thenReturn(false);
        tick(machine);
        verifyNoInteractions(source);
        when(level.hasChunk(0, 0)).thenReturn(true);
        when(source.pullThreshold()).thenReturn(129);
        tick(machine);
        verify(source, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(source.pullThreshold()).thenReturn(0);
        tick(machine);
        assertEquals(TTAspects.IGNIS, machine.aspectKey());
        verify(source).extract(fire, 1, Direction.WEST, false);
    }

    @Test
    void processingKeepsCentivisUnitsAndPausesWithRedstone() {
        var machine = machine();
        machine.insert(fire, 1, Direction.EAST, false);
        try (var aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 0.2F, false))
                    .thenReturn(0.2F);
            when(level.getGameTime()).thenReturn(4L);
            tick(machine);
            aura.verifyNoInteractions();
            when(level.getGameTime()).thenReturn(5L);
            tick(machine);
            assertEquals(41, machine.progress());
            when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
            tick(machine);
            assertEquals(41, machine.progress());
            when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(false);
            aura.when(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 0.2F, false))
                    .thenReturn(0.0F);
            tick(machine);
            assertEquals(42, machine.progress());
        }
    }

    @Test
    void processingClampsReportedVisToItsRequestAndNeverAddsNegativeProgress() {
        var machine = machine();
        machine.insert(fire, 1, Direction.EAST, false);
        try (var aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 0.2F, false))
                    .thenReturn(99.0F);
            tick(machine);
            assertEquals(41, machine.progress());
            aura.when(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 0.2F, false))
                    .thenReturn(-5.0F);
            tick(machine);
            assertEquals(42, machine.progress());
        }
    }

    @Test
    void completionInsertsAConfiguredCrystalThenVentsOnlyIfEjected() {
        var machine = machine();
        CompoundTag tag = new CompoundTag();
        TTNbt.store(tag, "Aspect", LegacyIds.ASPECT_KEY_CODEC, registries, TTAspects.IGNIS);
        tag.putInt("Progress", 199);
        machine.loadAdditional(tag, registries);
        ItemStack crystal = new ItemStack(Items.AMETHYST_SHARD);
        try (var aura = mockStatic(AuraHelper.class);
                var crystals = mockStatic(EssentiaCrystalAccess.class);
                var inventory = mockStatic(InvHelper.class);
                var effects = mockStatic(Effects.class);
                var drops = mockConstruction(ItemEntity.class, (drop, context) -> {
                    assertSame(level, context.arguments().get(0));
                    assertEquals(-0.2, (double) context.arguments().get(1), 0.0001);
                    assertEquals(0.5, (double) context.arguments().get(2), 0.0001);
                    assertEquals(0.5, (double) context.arguments().get(3), 0.0001);
                    assertSame(crystal, context.arguments().get(4));
                })) {
            crystals.when(() -> EssentiaCrystalAccess.create(fire, 1)).thenReturn(crystal);
            inventory
                    .when(() -> InvHelper.insertAt(level, BlockPos.ZERO.west(), Direction.EAST, crystal, false))
                    .thenReturn(crystal);
            tick(machine);
            assertNull(machine.aspectKey());
            assertEquals(0, machine.progress());
            assertEquals(1, drops.constructed().size());
            verify(level).playSound(null, BlockPos.ZERO, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.2F, 1.75F);
            var drop = drops.constructed().getFirst();
            verify(level).addFreshEntity(drop);
            verify(drop).setDeltaMovement(-0.04, 0.0, 0.0);
            Effects.Vent puff = mock(Effects.Vent.class, RETURNS_SELF);
            effects.when(() -> Effects.vent(eq(level), any())).thenReturn(puff);
            when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
            for (int i = 0; i < 8; i++) tick(machine);
            verify(puff, times(6)).send();
            verify(level, never()).blockEvent(any(), any(), anyInt(), anyInt());
            when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(false);
            inventory
                    .when(() -> InvHelper.insertAt(level, BlockPos.ZERO.west(), Direction.EAST, crystal, false))
                    .thenReturn(ItemStack.EMPTY);
            machine.loadAdditional(tag, registries);
            tick(machine);
            assertEquals(1, drops.constructed().size());
            assertNull(machine.aspectKey());
            clearInvocations(puff);
            when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
            for (int i = 0; i < 8; i++) tick(machine);
            verify(puff, never()).send();
        }
    }

    @Test
    void unresolvedFinishedAspectIsClearedWithoutProducingAnItem() {
        var machine = machine();
        CompoundTag tag = new CompoundTag();
        TTNbt.store(tag, "Aspect", LegacyIds.ASPECT_KEY_CODEC, registries, TTAspects.ORDO);
        tag.putInt("Progress", 199);
        machine.loadAdditional(tag, registries);
        try (var aura = mockStatic(AuraHelper.class);
                var crystals = mockStatic(EssentiaCrystalAccess.class)) {
            tick(machine);
            assertNull(machine.aspectKey());
            crystals.verifyNoInteractions();
            verify(level, never()).addFreshEntity(any());
        }
    }

    @Test
    void clientAnimationRetainsTintAndSpinWithoutRunningServerProcessing() {
        var machine = machine();
        machine.insert(fire, 1, Direction.EAST, false);
        BlockEntityEssentiaCrystalizer.clientTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
        assertEquals(1.0F, machine.crystalRed());
        assertEquals(0.95F, machine.crystalGreen(), 0.0001F);
        assertEquals(0.95F, machine.crystalBlue(), 0.0001F);
        assertEquals(0.1F, machine.rotationSpeed(), 0.0001F);
        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
        BlockEntityEssentiaCrystalizer.clientTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
        assertEquals(0.1F, machine.rotation(), 0.0001F);
        assertEquals(0.0F, machine.rotationSpeed());
        Level client = mock(Level.class);
        try (var aura = mockStatic(AuraHelper.class)) {
            BlockEntityEssentiaCrystalizer.serverTick(client, BlockPos.ZERO, machine.getBlockState(), machine);
            aura.verifyNoInteractions();
        }
    }

    @Test
    void clientTintKeepsTheCutoffBrightnessFloor() {
        var machine = machine();
        machine.insert(fire, 1, Direction.EAST, false);
        for (int i = 0; i < 30; i++) {
            BlockEntityEssentiaCrystalizer.clientTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
        }
        assertEquals(1.0F, machine.crystalRed());
        assertEquals(0.25F, machine.crystalGreen(), 0.0001F);
        assertEquals(0.25F, machine.crystalBlue(), 0.0001F);
        assertEquals(46.5F, machine.rotation(), 0.0001F);
    }
}
