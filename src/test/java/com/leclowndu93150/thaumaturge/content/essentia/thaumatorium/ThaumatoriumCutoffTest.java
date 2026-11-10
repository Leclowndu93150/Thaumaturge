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
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThaumatoriumCutoffTest {
    private static final ResourceLocation RECIPE = ResourceLocation.fromNamespaceAndPath("thaumaturge", "cutoff_craft");
    private ServerLevel level;
    private RegistryAccess registries;
    private RecipeManager recipes;
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
        registries = new RegistryAccess.ImmutableRegistryAccess(
                        Stream.concat(BuiltInRegistries.REGISTRY.stream(), Stream.of(registry))
                                .toList())
                .freeze();
        level = mock(ServerLevel.class);
        recipes = mock(RecipeManager.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.getRecipeManager()).thenReturn(recipes);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        BlockState heat = mock(BlockState.class);
        when(heat.is(TTBlockTags.CRUCIBLE_HEAT_SOURCES)).thenReturn(true);
        when(level.getBlockState(BlockPos.ZERO.below(2))).thenReturn(heat);
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.5F);
        when(level.getRandom()).thenReturn(random);
        when(level.getCapability(eq(EssentiaCapabilities.CONDUIT), any(BlockPos.class), any(Direction.class)))
                .thenAnswer(call -> peers.get(call.getArgument(1)));
    }

    private CrucibleRecipe recipe(Ingredient catalyst, AspectList cost) {
        return new CrucibleRecipe(catalyst, cost, new ItemStack(Items.GLOWSTONE, 2), Optional.empty());
    }

    private void offer(ResourceLocation id, CrucibleRecipe recipe) {
        when(recipes.byKey(id)).thenReturn(Optional.of(new RecipeHolder<>(id, recipe)));
    }

    private BlockEntityThaumatorium prepared() {
        BlockEntityThaumatorium machine = spy(new BlockEntityThaumatorium(
                BlockPos.ZERO, TTBlocks.THAUMATORIUM.get().defaultBlockState()));
        machine.setLevel(level);
        offer(RECIPE, recipe(Ingredient.of(Items.DIRT), AspectList.EMPTY.add(order, 2)));
        machine.queue().add(RECIPE);
        machine.catalyst().setStackInSlot(0, new ItemStack(Items.DIRT, 3));
        tick(machine, 1);
        return machine;
    }

    private void tick(BlockEntityThaumatorium machine, int times) {
        for (int tick = 0; tick < times; tick++) {
            BlockEntityThaumatorium.serverTick(level, BlockPos.ZERO, machine.getBlockState(), machine);
        }
    }

    private IEssentiaConduit peer(BlockPos pos, Direction face) {
        IEssentiaConduit source = mock(IEssentiaConduit.class);
        when(source.releasesOn(face)).thenReturn(true);
        when(source.heldAmount(face)).thenReturn(1);
        when(source.pull(face)).thenReturn(EssentiaPull.NONE);
        when(source.extract(order, 1, face, false)).thenReturn(1);
        peers.put(pos, source);
        return source;
    }

    @Test
    void plannedShortfallSimulationDoesNotMutateEssentiaOrDirtyState() {
        BlockEntityThaumatorium machine = prepared();
        clearInvocations(machine, level);
        assertEquals(2, machine.roomFor(order, Direction.EAST));
        assertEquals(2, machine.insert(order, 50, Direction.EAST, true));
        assertEquals(0, machine.insert(fire, 1, Direction.EAST, true));
        assertEquals(0, machine.insert(order, 1, Direction.NORTH, true));
        assertEquals(0, machine.insert(order, 1, null, true));
        assertEquals(0, machine.insert(order, -1, Direction.EAST, true));
        assertTrue(machine.essentia().isEmpty());
        verify(machine, never()).setChanged();
        verify(machine, never()).syncToClient();
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
    }

    @Test
    void stoppedWorkClearsPullOnItsNextScheduledWorkTick() {
        BlockEntityThaumatorium machine = prepared();
        assertEquals(EssentiaPull.of(order, 128), machine.pull(null));
        when(level.hasNeighborSignal(BlockPos.ZERO.above())).thenReturn(true);
        tick(machine, 4);
        assertEquals(EssentiaPull.of(order, 128), machine.pull(null));
        tick(machine, 1);
        assertSame(EssentiaPull.NONE, machine.pull(null));
        assertEquals(2, machine.roomFor(order, Direction.EAST));
    }

    @Test
    void noMatchingCatalystResetsPlanAndPullButRetainsPreviouslyReceivedStock() {
        BlockEntityThaumatorium machine = prepared();
        machine.insert(order, 1, Direction.EAST, false);
        machine.catalyst().setStackInSlot(0, new ItemStack(Items.COAL, 3));
        tick(machine, 5);
        assertSame(EssentiaPull.NONE, machine.pull(null));
        assertEquals(0, machine.roomFor(order, Direction.EAST));
        assertEquals(1, machine.essentia().amountOf(order));
        assertEquals(3, machine.catalystStack().getCount());
    }

    @Test
    void selectingAnotherRecipeKeepsReceivedEssentiaAndPlansOnlyItsRequirements() {
        BlockEntityThaumatorium machine = prepared();
        machine.insert(order, 1, Direction.EAST, false);
        ResourceLocation other = RECIPE.withSuffix("_coal");
        offer(other, recipe(Ingredient.of(Items.COAL), AspectList.EMPTY.add(fire, 3)));
        machine.queue().add(other);
        machine.catalyst().setStackInSlot(0, new ItemStack(Items.COAL, 3));
        tick(machine, 5);
        assertEquals(EssentiaPull.of(fire, 128), machine.pull(null));
        assertEquals(3, machine.roomFor(fire, Direction.EAST));
        assertEquals(0, machine.roomFor(order, Direction.EAST));
        assertEquals(1, machine.essentia().amountOf(order));
    }

    @Test
    void completedCraftKeepsPlanForNextCatalystAndUsesExactHissNumbers() {
        BlockEntityThaumatorium machine = prepared();
        machine.insert(order, 2, Direction.EAST, false);
        try (var output = mockStatic(InvHelper.class)) {
            tick(machine, 5);
            output.verify(() -> InvHelper.eject(
                    eq(level),
                    eq(BlockPos.ZERO),
                    eq(Direction.NORTH),
                    argThat(stack -> stack.is(Items.GLOWSTONE) && stack.getCount() == 2)));
        }
        assertEquals(2, machine.catalystStack().getCount());
        assertTrue(machine.essentia().isEmpty());
        assertSame(EssentiaPull.NONE, machine.pull(null));
        assertEquals(2, machine.roomFor(order, Direction.EAST));
        assertEquals(2, machine.insert(order, 2, Direction.EAST, true));
        verify(level)
                .playSound(
                        null,
                        BlockPos.ZERO,
                        SoundEvents.LAVA_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.3F,
                        1.7F + 0.5F * 0.25F);
        verify(level.getRandom(), times(1)).nextFloat();
    }

    @Test
    void drawingSkipsLowerUpFaceButUsesUpperUpFace() {
        IEssentiaConduit lowerUp = peer(BlockPos.ZERO.above(), Direction.DOWN);
        IEssentiaConduit upperUp = peer(BlockPos.ZERO.above(2), Direction.DOWN);
        assertEquals(1, new AdjacentEssentiaDraw().drawOne(level, BlockPos.ZERO, Direction.NORTH, order));
        verifyNoInteractions(lowerUp);
        verify(upperUp).extract(order, 1, Direction.DOWN, false);
    }

    @Test
    void upperFaceSimulationDelegatesOnlyAllowedSidesAndRemainsSinkOnly() {
        BlockEntityThaumatorium machine = prepared();
        BlockEntityThaumatoriumTop top = new BlockEntityThaumatoriumTop(
                BlockPos.ZERO.above(), TTBlocks.THAUMATORIUM_TOP.get().defaultBlockState());
        top.setLevel(level);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(machine);
        clearInvocations(machine, level);
        assertEquals(2, top.roomFor(order, Direction.EAST));
        assertEquals(2, top.insert(order, 50, Direction.EAST, true));
        assertEquals(0, top.insert(order, 1, Direction.DOWN, true));
        assertEquals(0, top.roomFor(order, Direction.NORTH));
        assertEquals(0, top.extract(order, 1, Direction.EAST, false));
        assertNull(top.heldAspect(null));
        assertEquals(0, top.heldAmount(null));
        assertFalse(top.releasesOn(Direction.EAST));
        assertEquals(EssentiaPull.of(order, 128), top.pull(null));
        assertTrue(machine.essentia().isEmpty());
        verify(machine, never()).setChanged();
    }

    @Test
    void completedCraftCreditsOnlineRecipeOwnerAndResearch() {
        BlockEntityThaumatorium machine = prepared();
        machine.queue().clear();
        Player owner = mock(Player.class);
        UUID id = new UUID(4L, 8L);
        when(owner.getUUID()).thenReturn(id);
        machine.toggleRecipe(level, owner, RECIPE);
        tick(machine, 5);
        machine.insert(order, 2, Direction.EAST, false);
        MinecraftServer server = mock(MinecraftServer.class);
        PlayerList players = mock(PlayerList.class);
        ServerPlayer crafter = mock(ServerPlayer.class);
        when(level.getServer()).thenReturn(server);
        when(server.getPlayerList()).thenReturn(players);
        when(players.getPlayer(id)).thenReturn(crafter);
        try (var output = mockStatic(InvHelper.class);
                var research = mockStatic(ResearchProgressionEvents.class)) {
            tick(machine, 5);
            research.verify(() -> ResearchProgressionEvents.recordCrafted(
                    eq(crafter), argThat(stack -> stack.is(Items.GLOWSTONE) && stack.getCount() == 2)));
        }
        verify(crafter).awardStat(Stats.ITEM_CRAFTED.get(Items.GLOWSTONE), 2);
        assertEquals(2, machine.catalystStack().getCount());
    }

    @Test
    void nativeSaveRetainsContentsButDoesNotPersistEphemeralCrafterMap() {
        BlockEntityThaumatorium original = prepared();
        original.queue().clear();
        Player owner = mock(Player.class);
        when(owner.getUUID()).thenReturn(new UUID(3L, 7L));
        original.toggleRecipe(level, owner, RECIPE);
        tick(original, 5);
        original.insert(order, 2, Direction.EAST, false);
        CompoundTag saved = original.getUpdateTag(registries);
        assertEquals(java.util.Set.of("Catalyst", "Queue", "MaxRecipes", "Essentia"), saved.getAllKeys());
        BlockEntityThaumatorium loaded = new BlockEntityThaumatorium(
                BlockPos.ZERO, TTBlocks.THAUMATORIUM.get().defaultBlockState());
        loaded.setLevel(level);
        loaded.loadAdditional(saved, registries);
        assertEquals(saved, loaded.getUpdateTag(registries));
        clearInvocations(level);
        try (var output = mockStatic(InvHelper.class);
                var research = mockStatic(ResearchProgressionEvents.class)) {
            tick(loaded, 1);
            research.verifyNoInteractions();
        }
        verify(level, never()).getServer();
        assertEquals(2, loaded.catalystStack().getCount());
        assertTrue(loaded.essentia().isEmpty());
    }

    @Test
    void nativeRemovalCleansUpOtherHalfOnlyOnServerAndActualReplacement() {
        BlockThaumatorium lower = (BlockThaumatorium) TTBlocks.THAUMATORIUM.get();
        BlockThaumatoriumTop upper = (BlockThaumatoriumTop) TTBlocks.THAUMATORIUM_TOP.get();
        BlockState lowerState = lower.defaultBlockState();
        BlockState upperState = upper.defaultBlockState();
        when(level.getBlockState(BlockPos.ZERO.above())).thenReturn(upperState);
        lower.onRemove(
                lowerState,
                level,
                BlockPos.ZERO,
                lowerState.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                false);
        verify(level, never()).destroyBlock(any(), anyBoolean());
        lower.onRemove(lowerState, level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
        verify(level).destroyBlock(BlockPos.ZERO.above(), true);
        clearInvocations(level);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(lowerState);
        upper.onRemove(upperState, level, BlockPos.ZERO.above(), Blocks.AIR.defaultBlockState(), false);
        verify(level).destroyBlock(BlockPos.ZERO, true);
        Level client = mock(Level.class);
        when(client.isClientSide()).thenReturn(true);
        lower.onRemove(lowerState, client, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
        upper.onRemove(upperState, client, BlockPos.ZERO.above(), Blocks.AIR.defaultBlockState(), false);
        verify(client, never()).destroyBlock(any(), anyBoolean());
    }
}
