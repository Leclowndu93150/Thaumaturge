package com.leclowndu93150.thaumaturge.content.device;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaIntake;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LampDeviceCutoffTest {
    private ServerLevel level;
    private RegistryAccess registries;
    private Holder<IAspect> desire;
    private RandomSource random;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(aspects, TTAspects.DESIDERIUM, mock(IAspect.class));
        Registry.register(aspects, TTAspects.HERBA, mock(IAspect.class));
        desire = aspects.getHolderOrThrow(TTAspects.DESIDERIUM);
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(aspects)).freeze();
        level = mock(ServerLevel.class);
        random = mock(RandomSource.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.getRandom()).thenReturn(random);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        when(random.nextInt(169)).thenReturn(84);
    }

    private BlockEntityLampFertility fertility(int charges) {
        var state =
                TTBlocks.LAMP_FERTILITY.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST);
        var lamp = new BlockEntityLampFertility(BlockPos.ZERO, state);
        lamp.setLevel(level);
        CompoundTag stored = new CompoundTag();
        stored.putInt("charges", charges);
        lamp.loadAdditional(stored, registries);
        return lamp;
    }

    private BlockEntityLampGrowth growth(int charges, boolean reserve) {
        var state =
                TTBlocks.LAMP_GROWTH.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST);
        var lamp = new BlockEntityLampGrowth(BlockPos.ZERO, state);
        lamp.setLevel(level);
        CompoundTag stored = new CompoundTag();
        stored.putInt("charges", charges);
        stored.putBoolean("reserve", reserve);
        lamp.loadAdditional(stored, registries);
        return lamp;
    }

    private CompoundTag save(BlockEntityLampFertility lamp) {
        CompoundTag stored = new CompoundTag();
        lamp.saveAdditional(stored, registries);
        return stored;
    }

    private void tick(BlockEntityLampFertility lamp, int count) {
        for (int i = 0; i < count; i++)
            BlockEntityLampFertility.serverTick(level, BlockPos.ZERO, lamp.getBlockState(), lamp);
    }

    private void tick(BlockEntityLampGrowth lamp, int count) {
        for (int i = 0; i < count; i++)
            BlockEntityLampGrowth.serverTick(level, BlockPos.ZERO, lamp.getBlockState(), lamp);
    }

    private Animal adult(EntityType<?> type) {
        var animal = mock(Animal.class);
        doReturn(type).when(animal).getType();
        when(animal.isAlive()).thenReturn(true);
        when(animal.canFallInLove()).thenReturn(true);
        return animal;
    }

    private void nearby(Animal... animals) {
        when(level.getEntitiesOfClass(eq(Animal.class), any(AABB.class))).thenReturn(List.of(animals));
    }

    @Test
    void fertilitySuctionTapersWithStoredChargeAndNativeNbtRetainsTheCounter() {
        var lamp = fertility(0);
        for (int charges = 0; charges <= 10; charges++) {
            CompoundTag stored = new CompoundTag();
            stored.putInt("charges", charges);
            lamp.loadAdditional(stored, registries);
            assertEquals(charges < 10, lamp.wantsEssentia());
            assertEquals(128 - 10 * charges, lamp.intakeSuction(128));
            assertEquals(
                    charges < 10 ? EssentiaPull.of(desire, 128 - 10 * charges) : EssentiaPull.NONE,
                    lamp.intake().pull(Direction.EAST));
            assertEquals(EssentiaPull.NONE, lamp.intake().pull(Direction.WEST));
            assertEquals(stored, save(lamp));
        }
        lamp.loadAdditional(new CompoundTag(), registries);
        assertEquals(0, save(lamp).getInt("charges"));
    }

    @Test
    void arcaneLampSkipsHeightmapLookupForAnUnloadedTargetColumn() {
        var state = TTBlocks.LAMP_ARCANE.get().defaultBlockState();
        var lamp = new BlockEntityLampArcane(BlockPos.ZERO, state);
        when(level.getGameTime()).thenReturn(0L);
        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(false);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);

        BlockEntityLampArcane.serverTick(level, BlockPos.ZERO, state, lamp);

        verify(level).hasChunk(0, 0);
        verify(level, never()).getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0);
        verify(level, never()).getBlockState(any());
    }

    @Test
    void arcaneLampRemovesItsGeneratedLightsBeforeRemoval() {
        var state = TTBlocks.LAMP_ARCANE.get().defaultBlockState();
        var lamp = new BlockEntityLampArcane(BlockPos.ZERO, state);
        BlockPos glimmer = BlockPos.ZERO.offset(1, 2, 3);
        Set<BlockPos> visited = new HashSet<>();
        lamp.setLevel(level);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(lamp);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);
        when(level.hasChunk(0, 0)).thenReturn(true);
        when(level.getBlockState(any())).thenAnswer(call -> {
            BlockPos position = call.getArgument(0);
            visited.add(position.immutable());
            return position.equals(glimmer)
                    ? TTBlocks.EFFECT_GLIMMER.get().defaultBlockState()
                    : Blocks.AIR.defaultBlockState();
        });

        TTBlocks.LAMP_ARCANE.get().onRemove(state, level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);

        assertTrue(visited.contains(glimmer));
        verify(level).setBlock(any(), eq(Blocks.AIR.defaultBlockState()), eq(Block.UPDATE_ALL));
    }

    @Test
    void fertilityAbsorbsAtMostOnePerTickAndStopsAtTenEvenWhilePowered() {
        try (var intakes = mockConstruction(EssentiaIntake.class)) {
            var lamp = fertility(8);
            var intake = intakes.constructed().getFirst();
            when(intake.pullOne()).thenReturn(true);
            when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
            tick(lamp, 3);
            assertEquals(10, save(lamp).getInt("charges"));
            verify(intake, times(2)).pullOne();
            verify(level, never()).getEntitiesOfClass(eq(Animal.class), any(AABB.class));
        }
    }

    @Test
    void fertilityPairsTwoEligibleAnimalsOfTheSameTypeForFiveCharges() {
        try (var intakes = mockConstruction(EssentiaIntake.class)) {
            var lamp = fertility(5);
            var cow = adult(EntityType.COW);
            var pig = adult(EntityType.PIG);
            var partner = adult(EntityType.COW);
            nearby(cow, pig, partner);
            tick(lamp, 1);
            verify(cow).setInLove(null);
            verify(partner).setInLove(null);
            verify(pig, never()).setInLove(any());
            assertEquals(0, save(lamp).getInt("charges"));
            verify(level).getEntitiesOfClass(Animal.class, new AABB(BlockPos.ZERO).inflate(7.0));
        }
    }

    @Test
    void fertilityRejectsDeadYoungCoolingDownAndUnavailableAnimals() {
        try (var intakes = mockConstruction(EssentiaIntake.class)) {
            var lamp = fertility(5);
            var dead = adult(EntityType.COW);
            when(dead.isAlive()).thenReturn(false);
            var young = adult(EntityType.COW);
            when(young.getAge()).thenReturn(-1);
            var coolingDown = adult(EntityType.COW);
            when(coolingDown.getAge()).thenReturn(1);
            var unavailable = adult(EntityType.COW);
            when(unavailable.canFallInLove()).thenReturn(false);
            nearby(dead, young, coolingDown, unavailable);
            tick(lamp, 1);
            assertEquals(5, save(lamp).getInt("charges"));
            for (Animal animal : List.of(dead, young, coolingDown, unavailable))
                verify(animal, never()).setInLove(any());
            var lonelyCow = adult(EntityType.COW);
            var lonelyPig = adult(EntityType.PIG);
            nearby(lonelyCow, lonelyPig);
            tick(lamp, 300);
            verify(lonelyCow, never()).setInLove(any());
            verify(lonelyPig, never()).setInLove(any());
        }
    }

    @Test
    void fertilityCountsEveryAnimalTowardCrowdingButCanPairAnotherSpecies() {
        try (var intakes = mockConstruction(EssentiaIntake.class)) {
            var lamp = fertility(5);
            List<Animal> animals = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                var cow = adult(EntityType.COW);
                if (i > 1) when(cow.isAlive()).thenReturn(false);
                animals.add(cow);
            }
            var pig = adult(EntityType.PIG);
            var partner = adult(EntityType.PIG);
            animals.add(pig);
            animals.add(partner);
            nearby(animals.toArray(Animal[]::new));
            tick(lamp, 1);
            for (int i = 0; i < 10; i++) verify(animals.get(i), never()).setInLove(any());
            verify(pig).setInLove(null);
            verify(partner).setInLove(null);
            assertEquals(0, save(lamp).getInt("charges"));
            animals.remove(9);
            nearby(animals.toArray(Animal[]::new));
            var lessCrowded = fertility(5);
            tick(lessCrowded, 1);
            verify(animals.get(0)).setInLove(null);
            verify(animals.get(1)).setInLove(null);
            assertEquals(0, save(lessCrowded).getInt("charges"));
        }
    }

    @Test
    void fertilityUsesThreeHundredActiveTicksAndPausesItsTimerWhenDisabled() {
        try (var intakes = mockConstruction(EssentiaIntake.class)) {
            var lamp = fertility(10);
            nearby(adult(EntityType.COW), adult(EntityType.COW));
            tick(lamp, 1);
            assertEquals(5, save(lamp).getInt("charges"));
            tick(lamp, 299);
            verify(level, times(1)).getEntitiesOfClass(eq(Animal.class), any(AABB.class));
            tick(lamp, 1);
            assertEquals(0, save(lamp).getInt("charges"));
            verify(level, times(2)).getEntitiesOfClass(eq(Animal.class), any(AABB.class));
            tick(lamp, 1);
            CompoundTag recharged = new CompoundTag();
            recharged.putInt("charges", 10);
            lamp.loadAdditional(recharged, registries);
            when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
            tick(lamp, 100);
            assertEquals(10, save(lamp).getInt("charges"));
            when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(false);
            tick(lamp, 1);
            assertEquals(10, save(lamp).getInt("charges"));
            tick(lamp, 298);
            assertEquals(10, save(lamp).getInt("charges"));
            verify(level, times(2)).getEntitiesOfClass(eq(Animal.class), any(AABB.class));
            tick(lamp, 1);
            assertEquals(5, save(lamp).getInt("charges"));
            verify(level, times(3)).getEntitiesOfClass(eq(Animal.class), any(AABB.class));
            var insufficient = fertility(4);
            tick(insufficient, 301);
            assertEquals(4, save(insufficient).getInt("charges"));
            verify(level, times(3)).getEntitiesOfClass(eq(Animal.class), any(AABB.class));
        }
    }

    @Test
    void growthPullsOnePointPerTickToChargeThenReserveAndStopsUntilNeeded() {
        try (var intakes = mockConstruction(EssentiaIntake.class)) {
            var lamp = growth(-1, false);
            var intake = intakes.constructed().getFirst();
            when(intake.pullOne()).thenReturn(true);
            when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
            tick(lamp, 1);
            assertEquals(20, lamp.getUpdateTag(registries).getInt("charges"));
            assertFalse(lamp.getUpdateTag(registries).getBoolean("reserve"));
            tick(lamp, 1);
            assertTrue(lamp.getUpdateTag(registries).getBoolean("reserve"));
            tick(lamp, 1);
            verify(intake, times(2)).pullOne();
            assertFalse(lamp.wantsEssentia());
            verify(level, never()).getBlockState(any());
        }
    }

    @Test
    void growthTransfersReserveNormalizesIdleAndRoundTripsNativeKeys() {
        try (var intakes = mockConstruction(EssentiaIntake.class)) {
            var lamp = growth(0, true);
            when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
            tick(lamp, 1);
            var stored = lamp.getUpdateTag(registries);
            assertEquals(Set.of("reserve", "charges"), stored.getAllKeys());
            assertEquals(20, stored.getInt("charges"));
            assertFalse(stored.getBoolean("reserve"));
            var loaded = growth(-1, false);
            loaded.loadAdditional(stored, registries);
            assertEquals(stored, loaded.getUpdateTag(registries));
            CompoundTag empty = new CompoundTag();
            loaded.loadAdditional(empty, registries);
            assertEquals(-1, loaded.getUpdateTag(registries).getInt("charges"));
            empty.putInt("charges", 0);
            loaded.loadAdditional(empty, registries);
            tick(loaded, 1);
            assertEquals(-1, loaded.getUpdateTag(registries).getInt("charges"));
        }
    }

    @Test
    void growthUsesSeededColumnsWithoutReplacementAndVisitsOnlyTheStrictSphere() {
        var first = growth(1000, true);
        when(level.getRandom()).thenReturn(RandomSource.create(125L));
        List<BlockPos> columns = new ArrayList<>();
        List<BlockPos> cells = new ArrayList<>();
        when(level.getBlockState(any())).thenAnswer(call -> {
            BlockPos cell = call.getArgument(0);
            cells.add(cell.immutable());
            return Blocks.AIR.defaultBlockState();
        });
        try (var chunks = mockStatic(LoadedChunks.class, CALLS_REAL_METHODS)) {
            chunks.when(() -> LoadedChunks.contains(eq(level), any(BlockPos.class)))
                    .thenAnswer(call -> {
                        BlockPos cell = call.getArgument(1);
                        columns.add(cell.immutable());
                        return call.callRealMethod();
                    });
            tick(first, 169);
            assertEquals(169, columns.size());
            assertEquals(169, new HashSet<>(columns).size());
            assertTrue(cells.stream().allMatch(cell -> cell.distSqr(BlockPos.ZERO) < 36));
            assertEquals(1000, first.getUpdateTag(registries).getInt("charges"));
            List<BlockPos> initialOrder = List.copyOf(columns);
            columns.clear();
            cells.clear();
            var second = growth(1000, true);
            when(level.getRandom()).thenReturn(RandomSource.create(125L));
            tick(second, 169);
            assertEquals(initialOrder, columns);
        }
    }

    @Test
    void growthNeverReadsAnUnloadedColumnOrSpendsItsCharges() {
        var lamp = growth(7, true);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);
        tick(lamp, 1);
        verify(level, never()).getBlockState(any());
        assertEquals(7, lamp.getUpdateTag(registries).getInt("charges"));
    }

    @Test
    void growthSkipsMatureBlacklistedGrassAndInvalidTargetsThenTicksOnePlant() {
        var lamp = growth(7, true);
        Map<BlockPos, BlockState> cells = new HashMap<>();
        var mature = mock(BlockState.class);
        when(mature.getBlock()).thenReturn(Blocks.WHEAT);
        when(mature.getValue(CropBlock.AGE)).thenReturn(7);
        cells.put(BlockPos.ZERO.above(5), mature);
        var blacklisted = mock(BlockState.class);
        when(blacklisted.is(TTBlockTags.LAMP_GROWTH_BLACKLIST)).thenReturn(true);
        cells.put(BlockPos.ZERO.above(4), blacklisted);
        var grass = mock(BlockState.class);
        when(grass.getBlock()).thenReturn(Blocks.GRASS_BLOCK);
        cells.put(BlockPos.ZERO.above(3), grass);
        var stone = mock(BlockState.class);
        when(stone.getBlock()).thenReturn(Blocks.STONE);
        cells.put(BlockPos.ZERO.above(2), stone);
        var wart = mock(BlockState.class);
        when(wart.getBlock()).thenReturn(Blocks.NETHER_WART);
        when(wart.getValue(NetherWartBlock.AGE)).thenReturn(3);
        cells.put(BlockPos.ZERO.above(), wart);
        var invalid = mock(BlockState.class);
        when(invalid.getBlock()).thenReturn(mock(Block.class, withSettings().extraInterfaces(BonemealableBlock.class)));
        cells.put(BlockPos.ZERO, invalid);
        var plant = mock(BlockState.class);
        when(plant.getBlock()).thenReturn(Blocks.WHEAT);
        when(plant.getValue(CropBlock.AGE)).thenReturn(0);
        when(plant.isRandomlyTicking()).thenReturn(true);
        cells.put(BlockPos.ZERO.below(), plant);
        when(level.getBlockState(any()))
                .thenAnswer(call -> cells.getOrDefault(call.getArgument(0), Blocks.AIR.defaultBlockState()));
        tick(lamp, 1);
        assertEquals(6, lamp.getUpdateTag(registries).getInt("charges"));
        verify(plant).randomTick(level, BlockPos.ZERO.below(), random);
        for (BlockState rejected : List.of(mature, blacklisted, grass, stone, wart, invalid))
            verify(rejected, never()).randomTick(any(), any(), any());
    }

    @Test
    void growthAcceptsMatureStemsAndTallPlantsButSpendsChargeWithoutForcingBonemeal() {
        for (Block block : List.of(Blocks.PUMPKIN_STEM, Blocks.CACTUS, Blocks.SUGAR_CANE)) {
            var lamp = growth(2, true);
            var plant = mock(BlockState.class);
            when(plant.getBlock()).thenReturn(block);
            when(plant.getValue(StemBlock.AGE)).thenReturn(7);
            when(level.getBlockState(any())).thenReturn(plant);
            tick(lamp, 1);
            assertEquals(1, lamp.getUpdateTag(registries).getInt("charges"));
            verify(plant, never()).randomTick(any(), any(), any());
        }
    }

    @Test
    void growthFeedbackOccursOnlyWhenTheTrackedStateChangesEvenAfterRedstoneStopsGrowth() {
        var lamp = growth(2, true);
        var plant = mock(BlockState.class);
        when(plant.getBlock()).thenReturn(Blocks.WHEAT);
        when(plant.getValue(CropBlock.AGE)).thenReturn(0);
        when(plant.isRandomlyTicking()).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(plant);
        tick(lamp, 1);
        verify(level, never())
                .sendParticles(
                        eq(ParticleTypes.HAPPY_VILLAGER),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyInt(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble(),
                        anyDouble());
        when(level.hasNeighborSignal(BlockPos.ZERO)).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        tick(lamp, 2);
        verify(level, times(1)).sendParticles(ParticleTypes.HAPPY_VILLAGER, 0.5, 5.5, 0.5, 6, 0.3, 0.3, 0.3, 0.0);
        assertEquals(1, lamp.getUpdateTag(registries).getInt("charges"));
    }
}
