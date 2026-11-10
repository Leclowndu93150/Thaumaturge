package com.leclowndu93150.thaumaturge.content.device.sprayer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PotionSprayerConduitCutoffTest {
    private ServerLevel level;
    private RegistryAccess registries;
    private Holder<IAspect> aspect;
    private Holder<IAspect> other;
    private RandomSource random;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(aspects, TTAspects.ORDO, mock(IAspect.class));
        Registry.register(aspects, TTAspects.ALKIMIA, mock(IAspect.class));
        aspect = aspects.getHolderOrThrow(TTAspects.ORDO);
        other = aspects.getHolderOrThrow(TTAspects.ALKIMIA);
        List<Registry<?>> all = new ArrayList<>();
        BuiltInRegistries.REGISTRY.forEach(registry -> {
            if (!registry.key().equals(IAspect.REGISTRY_KEY)) all.add(registry);
        });
        all.add(aspects);
        registries = new RegistryAccess.ImmutableRegistryAccess(all);
        level = mock(ServerLevel.class);
        random = mock(RandomSource.class);
        when(level.getRandom()).thenReturn(random);
        when(level.registryAccess()).thenReturn(registries);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
    }

    private BlockEntityPotionSprayer machine(Direction front) {
        var state = TTBlocks.POTION_SPRAYER.get().defaultBlockState().setValue(BlockStateProperties.FACING, front);
        var machine = new BlockEntityPotionSprayer(BlockPos.ZERO, state);
        machine.setLevel(level);
        return machine;
    }

    private void configure(BlockEntityPotionSprayer machine, ItemStack potion, AspectList recipe) {
        try (var aspects = mockStatic(PotionAspects.class)) {
            aspects.when(() -> PotionAspects.of(level, potion)).thenReturn(recipe);
            machine.setPotion(potion);
        }
    }

    private void tick(BlockEntityPotionSprayer machine, int count, boolean enabled) {
        BlockState state = machine.getBlockState().setValue(BlockStateProperties.ENABLED, enabled);
        for (int i = 0; i < count; i++) machine.tick(level, BlockPos.ZERO, state);
    }

    private void oneShot(BlockEntityPotionSprayer machine) {
        assertEquals(1, machine.insert(aspect, 1, Direction.DOWN, false));
        tick(machine, 5, true);
        assertEquals(1, machine.sprayShots());
    }

    @Test
    void exactPotionKindsSimulationCountsRoomAndFacesMatchFinal() {
        var machine = machine(Direction.EAST);
        assertTrue(BlockEntityPotionSprayer.holdsPotionItem(new ItemStack(Items.POTION)));
        assertTrue(BlockEntityPotionSprayer.holdsPotionItem(new ItemStack(Items.SPLASH_POTION)));
        assertTrue(BlockEntityPotionSprayer.holdsPotionItem(new ItemStack(Items.LINGERING_POTION)));
        assertFalse(BlockEntityPotionSprayer.holdsPotionItem(ItemStack.EMPTY));
        assertFalse(BlockEntityPotionSprayer.holdsPotionItem(new ItemStack(Items.GLASS_BOTTLE)));
        assertEquals(0, machine.insert(aspect, 1, Direction.DOWN, false));
        configure(machine, new ItemStack(Items.POTION), AspectList.EMPTY.add(aspect, 3));
        assertEquals(3, machine.roomFor(aspect, Direction.DOWN));
        assertEquals(0, machine.roomFor(other, Direction.DOWN));
        assertEquals(0, machine.roomFor(aspect, Direction.EAST));
        assertEquals(0, machine.insert(aspect, 0, Direction.DOWN, true));
        assertEquals(0, machine.insert(aspect, -1, Direction.DOWN, false));
        assertEquals(0, machine.insert(aspect, 1, Direction.EAST, true));
        assertEquals(3, machine.insert(aspect, 20, Direction.DOWN, true));
        assertTrue(machine.essentiaStored().isEmpty());
        assertEquals(3, machine.insert(aspect, 20, Direction.DOWN, false));
        assertEquals(3, machine.essentiaStored().amountOf(aspect));
        assertEquals(0, machine.roomFor(aspect, Direction.DOWN));
        assertNull(machine.heldAspect(null));
        assertEquals(0, machine.heldAmount(null));
        assertEquals(0, machine.extract(aspect, 1, Direction.DOWN, false));
        assertEquals(0, machine.pullThreshold());
        for (Direction face : Direction.values()) {
            assertEquals(face != Direction.EAST, machine.connectsOn(face));
            assertEquals(face != Direction.EAST, machine.receivesOn(face));
            assertFalse(machine.releasesOn(face));
        }
    }

    @Test
    void fiveTickChargingStopsAtEightAndSameStackPreservesWork() {
        var machine = machine(Direction.NORTH);
        var potion = new ItemStack(Items.POTION);
        configure(machine, potion, AspectList.EMPTY.add(aspect, 1));
        machine.insert(aspect, 1, Direction.DOWN, false);
        machine.setPotion(potion);
        assertEquals(1, machine.essentiaStored().amountOf(aspect));
        tick(machine, 4, true);
        assertEquals(0, machine.sprayShots());
        tick(machine, 1, true);
        assertEquals(1, machine.sprayShots());
        assertTrue(machine.essentiaStored().isEmpty());
        assertEquals(EssentiaPull.NONE, machine.pull(null));
        for (int i = 1; i < 8; i++) {
            machine.insert(aspect, 1, Direction.DOWN, false);
            tick(machine, 5, true);
        }
        assertEquals(8, machine.sprayShots());
        assertEquals(0, machine.roomFor(aspect, Direction.DOWN));
        assertEquals(0, machine.insert(aspect, 1, Direction.DOWN, false));
        machine.setPotion(ItemStack.EMPTY);
        assertEquals(0, machine.sprayShots());
        assertTrue(machine.essentiaStored().isEmpty());
        assertTrue(machine.essentiaNeeded().isEmpty());
        assertEquals(0x3A3A3A, machine.color());
    }

    @Test
    void aspectPullChecksLoadedMatchingReleaseStrengthThresholdAndUpperFrontSource() {
        var machine = machine(Direction.EAST);
        configure(machine, new ItemStack(Items.POTION), AspectList.EMPTY.add(aspect, 2));
        BlockPos sourcePos = BlockPos.ZERO.above().east();
        var source = mock(IEssentiaConduit.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, sourcePos, Direction.WEST))
                .thenReturn(source);
        when(source.releasesOn(Direction.WEST)).thenReturn(true);
        when(source.heldAmount(Direction.WEST)).thenReturn(1);
        when(source.heldAspect(Direction.WEST)).thenReturn(other);
        when(source.pull(Direction.WEST)).thenReturn(EssentiaPull.NONE);
        when(source.extract(aspect, 1, Direction.WEST, false)).thenReturn(1);
        when(level.hasChunk(0, 0)).thenReturn(false);
        tick(machine, 5, true);
        verifyNoInteractions(source);
        when(level.hasChunk(0, 0)).thenReturn(true);
        tick(machine, 5, true);
        verify(source, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(source.heldAspect(Direction.WEST)).thenReturn(aspect);
        when(source.pull(Direction.WEST)).thenReturn(EssentiaPull.any(128));
        tick(machine, 5, true);
        verify(source, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(source.pull(Direction.WEST)).thenReturn(EssentiaPull.NONE);
        when(source.pullThreshold()).thenReturn(129);
        tick(machine, 5, true);
        verify(source, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(source.pullThreshold()).thenReturn(128);
        tick(machine, 5, true);
        assertEquals(0, machine.essentiaStored().amountOf(aspect));
        assertEquals(EssentiaPull.of(aspect, 128), machine.pull(null));
        assertEquals(EssentiaPull.NONE, machine.pull(Direction.EAST));
        verify(source, never()).extract(any(), anyInt(), any(), anyBoolean());
        verify(level, never()).getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.east(), Direction.WEST);
    }

    @Test
    void risingRedstoneSpraysOnceAndBroadcastsFifteenTicksOfServerMist() {
        var machine = machine(Direction.EAST);
        configure(machine, new ItemStack(Items.POTION), AspectList.EMPTY.add(aspect, 1));
        oneShot(machine);
        when(level.getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any()))
                .thenReturn(List.of());
        try (var effects = mockStatic(Effects.class)) {
            var vent = mock(Effects.Vent.class, RETURNS_SELF);
            effects.when(() -> Effects.vent(eq(level), any())).thenReturn(vent);
            tick(machine, 1, false);
            assertEquals(0, machine.sprayShots());
            verify(level).playSound(null, BlockPos.ZERO, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.25F, 2.6F);
            effects.verify(() -> Effects.vent(level, new Vec3(0.9, 0.4, 0.4)), times(7));
            verify(vent, times(7)).motion(0.25, 0.0, 0.0);
            verify(vent, times(7)).scale(4.0F);
            tick(machine, 15, false);
            verify(vent, times(49)).send();
            clearInvocations(vent);
            tick(machine, 1, false);
            verifyNoInteractions(vent);
            verify(level, times(1))
                    .playSound(null, BlockPos.ZERO, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.25F, 2.6F);
        }
        var area = ArgumentCaptor.forClass(AABB.class);
        verify(level).getEntitiesOfClass(eq(LivingEntity.class), area.capture(), any());
        assertEquals(AABB.ofSize(new Vec3(2.5, 0.5, 0.5), 3.0, 3.0, 3.0), area.getValue());
    }

    @Test
    void firstPoweredTickDoesNotFireAndClientTicksDoNotWork() {
        var machine = machine(Direction.UP);
        configure(machine, new ItemStack(Items.POTION), AspectList.EMPTY.add(aspect, 1));
        var saved = machine.getUpdateTag(registries);
        saved.putInt("Charges", 3);
        machine.loadAdditional(saved, registries);
        tick(machine, 1, false);
        assertEquals(3, machine.sprayShots());
        verify(level, never()).getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any());
        var client = mock(Level.class);
        machine.tick(client, BlockPos.ZERO, machine.getBlockState().setValue(BlockStateProperties.ENABLED, false));
        verifyNoInteractions(client);
        assertEquals(3, machine.sprayShots());
    }

    @Test
    void nativePotionEffectsKeepDurationAmplifierAndVisibility() {
        var machine = machine(Direction.NORTH);
        var potion = new ItemStack(Items.POTION);
        var contents = mock(PotionContents.class);
        potion.set(DataComponents.POTION_CONTENTS, contents);
        configure(machine, potion, AspectList.EMPTY.add(aspect, 1));
        var instant = mock(MobEffect.class);
        when(instant.isInstantenous()).thenReturn(true);
        var lasting = mock(MobEffect.class);
        var instantInstance = new MobEffectInstance(Holder.direct(instant), 1, 2);
        var lastingInstance = new MobEffectInstance(Holder.direct(lasting), 123, 3, true, false, false);
        doAnswer(call -> {
                    Consumer<MobEffectInstance> consumer = call.getArgument(0);
                    consumer.accept(instantInstance);
                    consumer.accept(lastingInstance);
                    return null;
                })
                .when(contents)
                .forEachEffect(any());
        var target = mock(LivingEntity.class);
        when(level.getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any()))
                .thenReturn(List.of(target));
        oneShot(machine);
        try (var effects = mockStatic(Effects.class)) {
            effects.when(() -> Effects.vent(eq(level), any())).thenReturn(mock(Effects.Vent.class, RETURNS_SELF));
            tick(machine, 1, false);
        }
        verify(instant).applyInstantenousEffect(null, null, target, 2, 1.0);
        var effect = ArgumentCaptor.forClass(MobEffectInstance.class);
        verify(target).addEffect(effect.capture());
        assertNotSame(lastingInstance, effect.getValue());
        assertEquals(123, effect.getValue().getDuration());
        assertEquals(3, effect.getValue().getAmplifier());
        assertTrue(effect.getValue().isAmbient());
        assertFalse(effect.getValue().isVisible());
        assertFalse(effect.getValue().showIcon());
    }

    @Test
    void nativeNbtKeysRoundTripAndClampShots() {
        var machine = machine(Direction.UP);
        configure(machine, new ItemStack(Items.POTION), AspectList.EMPTY.add(aspect, 3));
        machine.insert(aspect, 2, Direction.DOWN, false);
        var saved = machine.getUpdateTag(registries);
        assertEquals(java.util.Set.of("Potion", "Recipe", "Progress", "Charges", "Color"), saved.getAllKeys());
        var loaded = machine(Direction.UP);
        loaded.loadAdditional(saved, registries);
        assertEquals(saved, loaded.getUpdateTag(registries));
        saved.putInt("Charges", 99);
        loaded.loadAdditional(saved, registries);
        assertEquals(8, loaded.sprayShots());
        saved.putInt("Charges", -3);
        loaded.loadAdditional(saved, registries);
        assertEquals(0, loaded.sprayShots());
        loaded.loadAdditional(new CompoundTag(), registries);
        assertEquals(0x3A3A3A, loaded.color());
    }

    @Test
    void onlyBreakingDropsPotionAndContainerDirtyNotificationPreservesCharges() {
        var machine = machine(Direction.UP);
        var potion = new ItemStack(Items.POTION);
        configure(machine, potion, AspectList.EMPTY.add(aspect, 1));
        oneShot(machine);
        new PotionSprayerContainer(machine).setChanged();
        assertEquals(1, machine.sprayShots());
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(machine);
        var block = (BlockPotionSprayer) machine.getBlockState().getBlock();
        try (var containers = mockStatic(Containers.class)) {
            machine.setRemoved();
            containers.verifyNoInteractions();
            block.onRemove(machine.getBlockState(), level, BlockPos.ZERO, machine.getBlockState(), false);
            containers.verifyNoInteractions();
            block.onRemove(machine.getBlockState(), level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
            containers.verify(() -> Containers.dropItemStack(level, 0.0, 0.0, 0.0, potion));
            assertTrue(machine.getPotion().isEmpty());
            machine.dropPotionOnBreak(BlockPos.ZERO);
            containers.verifyNoMoreInteractions();
        }
    }
}
