package com.leclowndu93150.thaumaturge.content.device;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

class LatticeAnchorCutoffTest {
    private ServerLevel level;
    private RandomSource random;
    private BlockCondenserLattice clean;
    private BlockCondenserLattice dirty;
    private final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private Predicate<ChunkPos> loaded = pos -> true;

    @BeforeEach
    void setUp() {
        level = mock(ServerLevel.class);
        random = mock(RandomSource.class);
        clean = (BlockCondenserLattice) TTBlocks.CONDENSER_LATTICE.get();
        dirty = (BlockCondenserLattice) TTBlocks.CONDENSER_LATTICE_DIRTY.get();
        when(level.getRandom()).thenReturn(random);
        when(level.hasChunk(anyInt(), anyInt()))
                .thenAnswer(call -> loaded.test(new ChunkPos(call.getArgument(0), call.getArgument(1))));
        when(level.getBlockState(any(BlockPos.class))).thenAnswer(call -> {
            BlockPos pos = call.getArgument(0);
            assertTrue(loaded.test(new ChunkPos(pos)), "Read from an unknown chunk at " + pos);
            return blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        });
    }

    private BlockEntityCondenser anchor(BlockPos pos) {
        blocks.put(pos, TTBlocks.CONDENSER.get().defaultBlockState());
        BlockEntityCondenser entity = mock(BlockEntityCondenser.class);
        when(level.getBlockEntity(pos)).thenReturn(entity);
        return entity;
    }

    @ParameterizedTest
    @CsvSource({"0,0,0,false", "0,-1,0,false", "0,1,0,true", "8,1,3,true", "-8,1,-3,true", "8,2,3,false", "9,1,0,false"
    })
    void reachRequiresStrictlyGreaterHeightAndIncludesExactlySeventyFourSquared(int x, int y, int z, boolean expected) {
        BlockPos condenser = new BlockPos(-20, 50, 17);
        assertEquals(expected, LatticeAnchor.withinReach(condenser, condenser.offset(x, y, z)));
    }

    @Test
    void placementLinksLatticeNeighborsAndOnlyTheCondenserDirectlyBelow() {
        BlockPos pos = new BlockPos(5, 5, 5);
        blocks.put(pos.below(), TTBlocks.CONDENSER.get().defaultBlockState());
        blocks.put(pos.above(), TTBlocks.CONDENSER.get().defaultBlockState());
        blocks.put(pos.east(), dirty.defaultBlockState());
        blocks.put(pos.north(), clean.defaultBlockState());
        BlockPlaceContext context = mock(BlockPlaceContext.class);
        when(context.getClickedPos()).thenReturn(pos);
        when(context.getLevel()).thenReturn(level);
        BlockState placed = clean.getStateForPlacement(context);
        assertNotNull(placed);
        assertTrue(placed.getValue(PipeBlock.DOWN));
        assertTrue(placed.getValue(PipeBlock.EAST));
        assertTrue(placed.getValue(PipeBlock.NORTH));
        assertFalse(placed.getValue(PipeBlock.UP));
        assertFalse(placed.getValue(PipeBlock.SOUTH));
        assertFalse(placed.getValue(PipeBlock.WEST));
        verify(level, never()).scheduleTick(any(BlockPos.class), any(Block.class), anyInt());
    }

    @Test
    void nativeNeighborUpdatesRecomputeOneArmAndScheduleATwoTickSettle() {
        BlockPos pos = BlockPos.ZERO;
        BlockState original = clean.defaultBlockState().setValue(PipeBlock.UP, true);
        BlockState east =
                clean.updateShape(original, Direction.EAST, dirty.defaultBlockState(), level, pos, pos.east());
        assertTrue(east.getValue(PipeBlock.EAST));
        assertTrue(east.getValue(PipeBlock.UP));
        BlockState disconnected =
                clean.updateShape(east, Direction.EAST, Blocks.AIR.defaultBlockState(), level, pos, pos.east());
        assertFalse(disconnected.getValue(PipeBlock.EAST));
        assertTrue(disconnected.getValue(PipeBlock.UP));
        BlockState supported = clean.updateShape(
                original, Direction.DOWN, TTBlocks.CONDENSER.get().defaultBlockState(), level, pos, pos.below());
        assertTrue(supported.getValue(PipeBlock.DOWN));
        BlockState aboveCondenser = clean.updateShape(
                original, Direction.UP, TTBlocks.CONDENSER.get().defaultBlockState(), level, pos, pos.above());
        assertFalse(aboveCondenser.getValue(PipeBlock.UP));
        verify(level, times(4)).scheduleTick(pos, clean, 2);
        verify(level, never()).destroyBlock(any(), anyBoolean());
    }

    @Test
    void newServerPlacementSchedulesButClientAndSameBlockChangesDoNot() {
        BlockState state = clean.defaultBlockState();
        clean.onPlace(state, level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
        clean.onPlace(state.setValue(PipeBlock.UP, true), level, BlockPos.ZERO, state, false);
        verify(level, times(1)).scheduleTick(BlockPos.ZERO, clean, 2);
        Level client = mock(Level.class);
        when(client.isClientSide()).thenReturn(true);
        clean.onPlace(state, client, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
        verify(client, never()).scheduleTick(any(BlockPos.class), any(Block.class), anyInt());
    }

    @Test
    void connectedCleanAndDirtyCellsRetainTheirAnchorAndRequestExactlyOneRecheck() {
        BlockPos condenser = BlockPos.ZERO;
        BlockEntityCondenser entity = anchor(condenser);
        blocks.put(condenser.above(), clean.defaultBlockState());
        blocks.put(condenser.above().east(), dirty.defaultBlockState());
        BlockPos pos = condenser.above().east(2);
        blocks.put(pos, clean.defaultBlockState());
        clean.tick(clean.defaultBlockState(), level, pos, random);
        verify(entity, times(1)).triggerCheck();
        verify(level, never()).destroyBlock(any(), anyBoolean());
    }

    @Test
    void fullyKnownDetachedCellIsDestroyedWithDrops() {
        BlockPos pos = new BlockPos(0, 1, 0);
        blocks.put(pos, dirty.defaultBlockState());
        LatticeAnchor.settle(level, pos);
        verify(level, times(1)).destroyBlock(pos, true);
        verify(level, never()).getBlockEntity(any());
    }

    @Test
    void connectedButOutOfReachCellCannotUseTheDistantCondenser() {
        BlockEntityCondenser entity = anchor(BlockPos.ZERO);
        for (int x = 0; x <= 9; x++) {
            blocks.put(new BlockPos(x, 1, 0), clean.defaultBlockState());
        }
        BlockPos pos = new BlockPos(9, 1, 0);
        LatticeAnchor.settle(level, pos);
        verify(level).destroyBlock(pos, true);
        verify(entity, never()).triggerCheck();
    }

    @Test
    void aCellBelowItsReachableCondenserIsStillDetached() {
        BlockPos condenser = new BlockPos(0, 2, 0);
        BlockEntityCondenser entity = anchor(condenser);
        BlockPos pos = new BlockPos(0, 1, 0);
        blocks.put(pos, clean.defaultBlockState());
        blocks.put(pos.east(), clean.defaultBlockState());
        blocks.put(pos.east().above(), dirty.defaultBlockState());
        blocks.put(pos.east().above(2), clean.defaultBlockState());
        blocks.put(condenser.above(), clean.defaultBlockState());
        LatticeAnchor.settle(level, pos);
        verify(level).destroyBlock(pos, true);
        verify(entity, never()).triggerCheck();
    }

    @Test
    void unknownChunkFrontierStopsDestructionAndIsNeverRead() {
        BlockPos pos = new BlockPos(15, 1, 0);
        blocks.put(pos, clean.defaultBlockState());
        loaded = chunk -> chunk.x < 1;
        LatticeAnchor.settle(level, pos);
        verify(level, never()).getBlockState(argThat(candidate -> candidate.getX() >= 16));
        verify(level, never()).destroyBlock(any(), anyBoolean());
        verify(level, never()).getBlockEntity(any());
    }

    @Test
    void aKnownAnchorRemainsUsableEvenWhenTheSurveyHasAnUnknownFrontier() {
        BlockPos condenser = new BlockPos(15, 0, 0);
        BlockEntityCondenser entity = anchor(condenser);
        BlockPos pos = condenser.above();
        blocks.put(pos, clean.defaultBlockState());
        loaded = chunk -> chunk.x < 1;
        LatticeAnchor.settle(level, pos);
        verify(entity).triggerCheck();
        verify(level, never()).destroyBlock(any(), anyBoolean());
        verify(level, never()).getBlockState(argThat(candidate -> candidate.getX() >= 16));
    }

    @Test
    void aScheduledTickForAnAlreadyReplacedCellDoesNothing() {
        LatticeAnchor.settle(level, BlockPos.ZERO);
        verify(level, never()).hasChunk(anyInt(), anyInt());
        verify(level, never()).destroyBlock(any(), anyBoolean());
        verify(level, never()).getBlockEntity(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void releasedSupportOnlyRechecksALoadedCondenser(boolean known) {
        BlockPos condenser = BlockPos.ZERO;
        BlockEntityCondenser entity = anchor(condenser);
        loaded = candidate -> known;
        LatticeAnchor.released(level, condenser.above());
        verify(entity, times(known ? 1 : 0)).triggerCheck();
        if (!known) {
            verify(level, never()).getBlockState(any());
            verify(level, never()).getBlockEntity(any());
        }
    }

    @Test
    void nativeRemovalDelegatesOnceOnlyForServerBlockIdentityChanges() {
        BlockState state = clean.defaultBlockState();
        Level client = mock(Level.class);
        try (var anchor = mockStatic(LatticeAnchor.class)) {
            clean.onRemove(state, level, BlockPos.ZERO, state.setValue(PipeBlock.UP, true), false);
            clean.onRemove(state, client, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
            anchor.verifyNoInteractions();
            clean.onRemove(state, level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), true);
            anchor.verify(() -> LatticeAnchor.released(level, BlockPos.ZERO), times(1));
            anchor.verifyNoMoreInteractions();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void serverCleaningConsumesOnlySurvivalFiltersAndRestoresNeighborLinks(boolean creative) {
        Player player = mock(Player.class);
        Abilities abilities = new Abilities();
        abilities.instabuild = creative;
        when(player.getAbilities()).thenReturn(abilities);
        ItemStack filter = new ItemStack(TTItems.FILTER.get(), 2);
        blocks.put(BlockPos.ZERO.below(), TTBlocks.CONDENSER.get().defaultBlockState());
        blocks.put(BlockPos.ZERO.east(), dirty.defaultBlockState());
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(BlockPos.ZERO), Direction.EAST, BlockPos.ZERO, false);
        assertEquals(
                ItemInteractionResult.SUCCESS,
                dirty.useItemOn(
                        filter,
                        dirty.defaultBlockState(),
                        level,
                        BlockPos.ZERO,
                        player,
                        InteractionHand.MAIN_HAND,
                        hit));
        assertEquals(creative ? 2 : 1, filter.getCount());
        ArgumentCaptor<BlockState> cleaned = ArgumentCaptor.forClass(BlockState.class);
        verify(level).setBlock(eq(BlockPos.ZERO), cleaned.capture(), eq(3));
        assertTrue(cleaned.getValue().is(clean));
        assertTrue(cleaned.getValue().getValue(PipeBlock.DOWN));
        assertTrue(cleaned.getValue().getValue(PipeBlock.EAST));
        verify(random, times(1)).nextBoolean();
        verify(level, never()).addFreshEntity(any());
        verify(level).playSound(null, BlockPos.ZERO, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
    }

    @Test
    void aSuccessfulCleaningRollDropsOneVitiumCrystalAtTheCutoffFaceOffset() {
        when(random.nextBoolean()).thenReturn(true);
        Holder<IAspect> vitium = Holder.direct(mock(IAspect.class));
        Player player = mock(Player.class);
        when(player.getAbilities()).thenReturn(new Abilities());
        ItemStack filter = new ItemStack(TTItems.FILTER.get(), 2);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(BlockPos.ZERO), Direction.EAST, BlockPos.ZERO, false);
        try (var aspects = mockStatic(Aspects.class);
                var drops = mockConstruction(ItemEntity.class, (drop, context) -> {
                    assertSame(level, context.arguments().get(0));
                    assertEquals(1.125, (double) context.arguments().get(1));
                    assertEquals(0.5, (double) context.arguments().get(2));
                    assertEquals(0.5, (double) context.arguments().get(3));
                    ItemStack crystal = (ItemStack) context.arguments().get(4);
                    assertTrue(crystal.is(TTItems.ESSENTIA_CRYSTAL.get()));
                    assertEquals(1, crystal.getCount());
                    AspectInstance content = crystal.get(TTDataComponents.CRYSTAL_ASPECT.get());
                    assertNotNull(content);
                    assertSame(vitium, content.aspect());
                    assertEquals(1, content.amount());
                })) {
            aspects.when(() -> Aspects.resolve(level, TTAspects.VITIUM)).thenReturn(vitium);
            dirty.useItemOn(
                    filter, dirty.defaultBlockState(), level, BlockPos.ZERO, player, InteractionHand.MAIN_HAND, hit);
            assertEquals(1, drops.constructed().size());
            verify(level).addFreshEntity(drops.constructed().getFirst());
            assertEquals(1, filter.getCount());
        }
    }

    @Test
    void missingVitiumStillCleansButProducesNoCrystal() {
        when(random.nextBoolean()).thenReturn(true);
        Player player = mock(Player.class);
        when(player.getAbilities()).thenReturn(new Abilities());
        ItemStack filter = new ItemStack(TTItems.FILTER.get());
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(BlockPos.ZERO), Direction.UP, BlockPos.ZERO, false);
        try (var aspects = mockStatic(Aspects.class)) {
            dirty.useItemOn(
                    filter, dirty.defaultBlockState(), level, BlockPos.ZERO, player, InteractionHand.MAIN_HAND, hit);
            assertTrue(filter.isEmpty());
            verify(level).setBlock(eq(BlockPos.ZERO), any(), eq(3));
            verify(level, never()).addFreshEntity(any());
        }
    }

    @Test
    void clientCleaningPredictsSuccessWithoutConsumingOrPublishingEffects() {
        Level client = mock(Level.class);
        Player player = mock(Player.class);
        ItemStack filter = new ItemStack(TTItems.FILTER.get(), 2);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(BlockPos.ZERO), Direction.EAST, BlockPos.ZERO, false);
        assertEquals(
                ItemInteractionResult.SUCCESS,
                dirty.useItemOn(
                        filter,
                        dirty.defaultBlockState(),
                        client,
                        BlockPos.ZERO,
                        player,
                        InteractionHand.MAIN_HAND,
                        hit));
        assertEquals(2, filter.getCount());
        verifyNoInteractions(client, player);
    }

    @Test
    void cleanLatticeOrWrongHeldItemPassesThroughWithoutCleaning() {
        Player player = mock(Player.class);
        ItemStack filter = new ItemStack(TTItems.FILTER.get(), 2);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(BlockPos.ZERO), Direction.EAST, BlockPos.ZERO, false);
        assertEquals(
                ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION,
                clean.useItemOn(
                        filter,
                        clean.defaultBlockState(),
                        level,
                        BlockPos.ZERO,
                        player,
                        InteractionHand.MAIN_HAND,
                        hit));
        assertEquals(
                ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION,
                dirty.useItemOn(
                        new ItemStack(Items.STICK),
                        dirty.defaultBlockState(),
                        level,
                        BlockPos.ZERO,
                        player,
                        InteractionHand.MAIN_HAND,
                        hit));
        assertEquals(2, filter.getCount());
        verify(level, never()).setBlock(any(), any(), anyInt());
        verifyNoInteractions(random, player);
    }
}
