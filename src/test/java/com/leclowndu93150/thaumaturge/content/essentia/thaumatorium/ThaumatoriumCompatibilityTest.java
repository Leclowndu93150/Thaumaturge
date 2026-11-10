package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.AdjacentEssentiaDraw;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.BrainBoxScanner;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.NeighbourRing;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.RecipeQueue;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.RequirementTracker;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.ThaumatoriumScheduler;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThaumatoriumCompatibilityTest {
    private static final ResourceLocation RECIPE = ResourceLocation.fromNamespaceAndPath("thaumaturge", "test_craft");
    private ServerLevel level;
    private RegistryAccess registries;
    private Holder<IAspect> order;
    private Holder<IAspect> fire;
    private RecipeManager recipes;
    private final Map<BlockPos, IEssentiaConduit> peers = new HashMap<>();

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.ORDO, mock(IAspect.class));
        Registry.register(registry, TTAspects.IGNIS, mock(IAspect.class));
        order = registry.getHolderOrThrow(TTAspects.ORDO);
        fire = registry.getHolderOrThrow(TTAspects.IGNIS);
        registries = new RegistryAccess.ImmutableRegistryAccess(
                        Stream.concat(BuiltInRegistries.REGISTRY.stream(), Stream.of(registry))
                                .toList())
                .freeze();
        level = mock(ServerLevel.class);
        recipes = mock(RecipeManager.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.getRecipeManager()).thenReturn(recipes);
        when(level.getRandom()).thenReturn(mock(RandomSource.class));
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        BlockState heat = mock(BlockState.class);
        when(heat.is(TTBlockTags.CRUCIBLE_HEAT_SOURCES)).thenReturn(true);
        when(level.getBlockState(BlockPos.ZERO.below(2))).thenReturn(heat);
        when(level.getCapability(eq(EssentiaCapabilities.CONDUIT), any(BlockPos.class), any(Direction.class)))
                .thenAnswer(call -> peers.get(call.getArgument(1)));
    }

    private BlockEntityThaumatorium machine() {
        BlockEntityThaumatorium machine = new BlockEntityThaumatorium(
                BlockPos.ZERO, TTBlocks.THAUMATORIUM.get().defaultBlockState());
        machine.setLevel(level);
        return machine;
    }

    private CrucibleRecipe recipe(AspectList cost) {
        return new CrucibleRecipe(Ingredient.of(Items.DIRT), cost, new ItemStack(Items.GLOWSTONE, 2), Optional.empty());
    }

    private void offer(ResourceLocation id, CrucibleRecipe recipe) {
        when(recipes.byKey(id)).thenReturn(Optional.of(new RecipeHolder<>(id, recipe)));
    }

    private void tick(BlockEntityThaumatorium machine) {
        BlockEntityThaumatorium.serverTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
    }

    private IEssentiaConduit peer(BlockPos pos, Direction face) {
        IEssentiaConduit peer = mock(IEssentiaConduit.class);
        when(peer.releasesOn(face)).thenReturn(true);
        when(peer.heldAmount(face)).thenReturn(4);
        when(peer.pull(face)).thenReturn(EssentiaPull.NONE);
        peers.put(pos, peer);
        return peer;
    }

    @Test
    void queueResizingKeepsOldestSelectionsAndRestoresTheirOrder() {
        RecipeQueue queue = new RecipeQueue();
        assertEquals(1, queue.capacity());
        assertEquals(5, RecipeQueue.capacityFor(2));
        ResourceLocation other = RECIPE.withSuffix("_other");
        queue.restore(5, List.of(RECIPE, other, RECIPE.withSuffix("_third")));
        assertFalse(queue.resize(3));
        assertTrue(queue.resize(1));
        assertEquals(List.of(RECIPE), queue.ids());
        assertTrue(queue.isFull());
        assertTrue(queue.remove(RECIPE));
        assertTrue(queue.isEmpty());
    }

    @Test
    void requirementOrderAndAcceptedAmountsFollowMainsPlan() {
        RequirementTracker tracker = new RequirementTracker();
        tracker.planFor(recipe(AspectList.EMPTY.add(order, 3).add(fire, 2)));
        assertEquals(fire, tracker.firstUnmet());
        assertEquals(0, tracker.accept(fire, -1));
        assertEquals(2, tracker.accept(fire, 50));
        assertEquals(order, tracker.firstUnmet());
        assertEquals(3, tracker.accept(order, 5));
        assertNull(tracker.firstUnmet());
        assertEquals(0, tracker.accept(order, 1));
        tracker.clearPlan();
        assertEquals(5, tracker.received().totalAmount());
        assertEquals(0, tracker.accept(order, 5));
        tracker.clearReceived();
        assertTrue(tracker.received().isEmpty());
    }

    @Test
    void schedulerFiresImmediatelyThenAtFiveAndFortyTickIntervals() {
        ThaumatoriumScheduler scheduler = new ThaumatoriumScheduler();
        for (int tick = 0; tick <= 80; tick++) {
            scheduler.advance();
            assertEquals(tick % 40 == 0, scheduler.refreshDue(), "refresh tick " + tick);
            assertEquals(tick % 5 == 0, scheduler.workDue(), "work tick " + tick);
        }
    }

    @Test
    void neighbourRingVisitsTwoLayersButNeverTheFrontOrDownwardFace() {
        NeighbourRing ring = new NeighbourRing();
        Set<BlockPos> positions = new HashSet<>();
        for (int slot = 0; slot < ring.slots(); slot++) {
            if (!ring.aim(BlockPos.ZERO, Direction.NORTH, slot)) continue;
            assertNotEquals(Direction.NORTH, ring.side());
            assertNotEquals(Direction.DOWN, ring.side());
            positions.add(ring.pos().immutable());
        }
        assertEquals(8, positions.size());
        assertTrue(positions.contains(BlockPos.ZERO.above()));
        assertTrue(positions.contains(BlockPos.ZERO.above(2)));
        assertTrue(positions.contains(BlockPos.ZERO.above().east()));
    }

    @Test
    void brainBoxesMustBeLoadedAndFaceTheMachine() {
        BlockState brain = TTBlocks.BRAIN_BOX.get().defaultBlockState();
        when(level.getBlockState(BlockPos.ZERO.east()))
                .thenReturn(brain.setValue(BlockStateProperties.FACING, Direction.WEST));
        when(level.getBlockState(BlockPos.ZERO.above().west()))
                .thenReturn(brain.setValue(BlockStateProperties.FACING, Direction.EAST));
        when(level.getBlockState(BlockPos.ZERO.south()))
                .thenReturn(brain.setValue(BlockStateProperties.FACING, Direction.SOUTH));
        BrainBoxScanner scanner = new BrainBoxScanner();
        assertEquals(2, scanner.count(level, BlockPos.ZERO, Direction.NORTH));
        clearInvocations(level);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);
        assertEquals(0, scanner.count(level, BlockPos.ZERO, Direction.NORTH));
        verify(level, never()).getBlockState(any());
    }

    @Test
    void pullingSkipsRefusingPeersAndNeverQueriesUnloadedCapabilities() {
        IEssentiaConduit refusing = peer(BlockPos.ZERO.south(), Direction.NORTH);
        IEssentiaConduit source = peer(BlockPos.ZERO.west(), Direction.EAST);
        when(source.extract(order, 1, Direction.EAST, false)).thenReturn(1);
        AdjacentEssentiaDraw puller = new AdjacentEssentiaDraw();
        assertEquals(1, puller.drawOne(level, BlockPos.ZERO, Direction.NORTH, order));
        verify(refusing).extract(order, 1, Direction.NORTH, false);
        verify(source).extract(order, 1, Direction.EAST, false);
        clearInvocations(level);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(false);
        assertEquals(0, puller.drawOne(level, BlockPos.ZERO, Direction.NORTH, order));
        verify(level, never())
                .getCapability(eq(EssentiaCapabilities.CONDUIT), any(BlockPos.class), any(Direction.class));
    }

    @Test
    void pullerHonorsOutputAndSuctionRestrictions() {
        IEssentiaConduit source = peer(BlockPos.ZERO.south(), Direction.NORTH);
        AdjacentEssentiaDraw puller = new AdjacentEssentiaDraw();
        when(source.pull(Direction.NORTH)).thenReturn(EssentiaPull.any(128));
        assertEquals(0, puller.drawOne(level, BlockPos.ZERO, Direction.NORTH, order));
        when(source.pull(Direction.NORTH)).thenReturn(EssentiaPull.NONE);
        when(source.pullThreshold()).thenReturn(129);
        assertEquals(0, puller.drawOne(level, BlockPos.ZERO, Direction.NORTH, order));
        when(source.pullThreshold()).thenReturn(0);
        when(source.releasesOn(Direction.NORTH)).thenReturn(false);
        assertEquals(0, puller.drawOne(level, BlockPos.ZERO, Direction.NORTH, order));
        verify(source, never()).extract(any(), anyInt(), any(), eq(false));
    }

    @Test
    void nativeNbtPreservesCatalystQueueAndReceivedEssentia() {
        BlockEntityThaumatorium machine = machine();
        offer(RECIPE, recipe(AspectList.EMPTY.add(order, 3)));
        machine.toggleRecipe(level, mock(Player.class), RECIPE);
        machine.catalyst().setStackInSlot(0, new ItemStack(Items.DIRT, 4));
        tick(machine);
        assertEquals(2, machine.insert(order, 2, Direction.EAST, false));
        CompoundTag tag = machine.getUpdateTag(registries);
        assertEquals(Set.of("Catalyst", "Queue", "MaxRecipes", "Essentia"), tag.getAllKeys());
        BlockEntityThaumatorium loaded = machine();
        loaded.loadAdditional(tag, registries);
        assertEquals(tag, loaded.getUpdateTag(registries));
        assertEquals(4, loaded.catalystStack().getCount());
        assertEquals(List.of(RECIPE), loaded.queue());
        assertEquals(2, loaded.essentia().amountOf(order));
        BlockEntityThaumatorium empty = machine();
        empty.loadAdditional(new CompoundTag(), registries);
        assertEquals(1, empty.maxRecipes());
        assertTrue(empty.queue().isEmpty());
    }

    @Test
    void machineOnlyAcceptsPlannedAspectsThroughAllowedFacesAndCapsTheAmount() {
        BlockEntityThaumatorium machine = machine();
        assertEquals(0, machine.insert(order, 1, Direction.EAST, false));
        offer(RECIPE, recipe(AspectList.EMPTY.add(order, 3)));
        machine.queue().add(RECIPE);
        machine.catalyst().setStackInSlot(0, new ItemStack(Items.DIRT));
        tick(machine);
        assertEquals(order, machine.pull(Direction.EAST).aspect());
        assertEquals(128, machine.pull(Direction.EAST).strength());
        assertEquals(0, machine.insert(order, 3, Direction.NORTH, false));
        assertEquals(0, machine.insert(fire, 3, Direction.EAST, false));
        assertEquals(0, machine.insert(order, -1, Direction.EAST, false));
        assertEquals(3, machine.insert(order, 80, Direction.EAST, false));
        assertEquals(0, machine.insert(order, 1, Direction.EAST, false));
        assertEquals(0, machine.extract(order, 1, Direction.EAST, false));
        assertFalse(machine.releasesOn(Direction.EAST));
        assertFalse(machine.receivesOn(null));
        ItemStack copy = machine.catalystStack();
        copy.shrink(1);
        assertEquals(1, machine.catalystStack().getCount());
    }

    @Test
    void completedCraftUsesNativeAssemblyConsumesOneCatalystAndEjectsTheRealResult() {
        BlockEntityThaumatorium machine = machine();
        CrucibleRecipe recipe = spy(recipe(AspectList.EMPTY.add(order, 2)));
        offer(RECIPE, recipe);
        machine.queue().add(RECIPE);
        machine.catalyst().setStackInSlot(0, new ItemStack(Items.DIRT, 3));
        tick(machine);
        assertEquals(2, machine.insert(order, 5, Direction.EAST, false));
        try (var output = mockStatic(InvHelper.class)) {
            for (int tick = 0; tick < 5; tick++) tick(machine);
            output.verify(() -> InvHelper.eject(
                    eq(level),
                    eq(BlockPos.ZERO),
                    eq(Direction.NORTH),
                    argThat(stack -> stack.is(Items.GLOWSTONE) && stack.getCount() == 2)));
        }
        verify(recipe).assemble(any(), eq(registries));
        assertEquals(2, machine.catalystStack().getCount());
        assertTrue(machine.essentia().isEmpty());
        assertNull(machine.pull(Direction.EAST).aspect());
        assertEquals(0, machine.pull(Direction.EAST).strength());
        assertEquals(2, recipe.rawResult().getCount());
    }

    @Test
    void redstonePausesWorkAndTopDelegatesOnlyItsAllowedSides() {
        BlockEntityThaumatorium machine = machine();
        offer(RECIPE, recipe(AspectList.EMPTY.add(order, 3)));
        machine.queue().add(RECIPE);
        machine.catalyst().setStackInSlot(0, new ItemStack(Items.DIRT));
        when(level.hasNeighborSignal(BlockPos.ZERO.above())).thenReturn(true);
        tick(machine);
        assertNull(machine.pull(Direction.EAST).aspect());
        when(level.hasNeighborSignal(BlockPos.ZERO.above())).thenReturn(false);
        for (int tick = 0; tick < 5; tick++) tick(machine);
        BlockEntityThaumatoriumTop top = new BlockEntityThaumatoriumTop(
                BlockPos.ZERO.above(), TTBlocks.THAUMATORIUM_TOP.get().defaultBlockState());
        top.setLevel(level);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(machine);
        assertFalse(top.connectsOn(Direction.DOWN));
        assertFalse(top.receivesOn(Direction.NORTH));
        assertEquals(0, top.insert(order, 1, Direction.DOWN, false));
        assertEquals(2, top.insert(order, 2, Direction.EAST, false));
        assertEquals(order, top.pull(Direction.EAST).aspect());
        assertFalse(top.releasesOn(Direction.EAST));
        assertEquals(0, top.extract(order, 1, Direction.EAST, false));
    }

    @Test
    void candidateRecipesUseNativeManagerAndRespectGateUnlessAlreadyQueued() {
        BlockEntityThaumatorium machine = machine();
        Player player = mock(Player.class);
        CrucibleRecipe allowed = recipe(AspectList.EMPTY.add(order, 2));
        CrucibleRecipe denied = spy(recipe(AspectList.EMPTY.add(fire, 1)));
        doReturn(false).when(denied).doesPassGate(player);
        ResourceLocation lockedId = RECIPE.withSuffix("_locked");
        when(recipes.getAllRecipesFor(TTRecipeTypes.CRUCIBLE.get()))
                .thenReturn(List.of(new RecipeHolder<>(RECIPE, allowed), new RecipeHolder<>(lockedId, denied)));
        machine.catalyst().setStackInSlot(0, new ItemStack(Items.DIRT));
        List<ResourceLocation> ids = new ArrayList<>();
        assertEquals(List.of(allowed), machine.candidateRecipes(level, player, ids));
        assertEquals(List.of(RECIPE), ids);
        machine.queue().add(lockedId);
        machine.catalyst().setStackInSlot(0, ItemStack.EMPTY);
        ids.clear();
        assertEquals(List.of(denied), machine.candidateRecipes(level, player, ids));
        assertEquals(List.of(lockedId), ids);
    }
}
