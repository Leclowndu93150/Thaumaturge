package com.leclowndu93150.thaumaturge.content.essentia.jar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JarCompatibilityTest {
    private Level level;
    private RegistryAccess registries;
    private Holder<IAspect> order;
    private Holder<IAspect> fire;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.ORDO, mock(IAspect.class));
        Registry.register(registry, TTAspects.IGNIS, mock(IAspect.class));
        order = registry.getHolderOrThrow(TTAspects.ORDO);
        fire = registry.getHolderOrThrow(TTAspects.IGNIS);
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        level = mock(Level.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
    }

    private BlockEntityJar jar() {
        BlockEntityJar jar =
                new BlockEntityJar(BlockPos.ZERO, TTBlocks.JAR_NORMAL.get().defaultBlockState());
        jar.setLevel(level);
        return jar;
    }

    @Test
    void transportReportsAcceptedAmountAndAspectSourceReportsRemainder() {
        BlockEntityJar jar = jar();
        assertEquals(0, jar.insert(order, 1, Direction.DOWN, false));
        assertEquals(BlockEntityJar.CAPACITY, jar.insert(order, BlockEntityJar.CAPACITY + 3, Direction.UP, false));
        assertEquals(3, jar.fill(order, 3));
        assertEquals(BlockEntityJar.CAPACITY, jar.amount());
        assertEquals(0, jar.extract(fire, 1, Direction.UP, false));
        assertEquals(2, jar.extract(order, 2, Direction.UP, false));
        assertEquals(1, jar.fill(order, 3));
    }

    @Test
    void labelRejectsOtherAspectsAndRemainsAfterDraining() {
        BlockEntityJar jar = jar();
        jar.setAspectFilter(TTAspects.ORDO);
        assertEquals(0, jar.insert(fire, 2, Direction.UP, false));
        assertEquals(64, jar.pull(Direction.UP).strength());
        assertEquals(2, jar.insert(order, 2, Direction.UP, false));
        assertTrue(jar.drain(order, 2));
        assertEquals(TTAspects.ORDO, jar.aspectKey());
        assertEquals(TTAspects.ORDO, jar.aspectFilterKey());
        assertEquals(0, jar.amount());
    }

    @Test
    void storageSimulationAndConfigurationDoNotAdvanceContentRevision() {
        BlockEntityJar jar = jar();
        IEssentiaStorage storage = jar.storage(Direction.UP);
        long start = storage.contentRevision();
        assertEquals(7, storage.insert(order, 7, true));
        assertEquals(start, storage.contentRevision());
        assertEquals(0, jar.amount());
        jar.setBraced(true);
        jar.setFacing(Direction.WEST);
        jar.setAspectFilter(TTAspects.ORDO);
        assertEquals(start, storage.contentRevision());
        assertEquals(7, storage.insert(order, 7, false));
        assertEquals(start + 1, storage.contentRevision());
        assertEquals(7, storage.extract(order, 10, false));
        assertEquals(start + 2, storage.contentRevision());
    }

    @Test
    void voidJarDiscardsOverflowButNotMismatchedAspects() {
        BlockEntityJarVoid jar =
                new BlockEntityJarVoid(BlockPos.ZERO, TTBlocks.JAR_VOID.get().defaultBlockState());
        jar.setLevel(level);
        assertEquals(BlockEntityJar.CAPACITY + 5, jar.insert(order, BlockEntityJar.CAPACITY + 5, Direction.UP, false));
        assertEquals(BlockEntityJar.CAPACITY, jar.amount());
        assertEquals(0, jar.fill(order, 5));
        assertEquals(5, jar.fill(fire, 5));
        assertEquals(BlockEntityJar.CAPACITY, jar.amount());
    }

    @Test
    void jarRetainsExistingNbtKeysAndValues() {
        BlockEntityJar original = jar();
        original.insert(order, 9, Direction.UP, false);
        original.setAspectFilter(TTAspects.ORDO);
        original.setFacing(Direction.WEST);
        original.setBraced(true);
        CompoundTag tag = original.getUpdateTag(registries);
        assertEquals(Set.of("Aspect", "AspectFilter", "Amount", "Facing", "Braced"), tag.getAllKeys());
        BlockEntityJar loaded = jar();
        loaded.loadAdditional(tag, registries);
        assertEquals(TTAspects.ORDO, loaded.aspectKey());
        assertEquals(TTAspects.ORDO, loaded.aspectFilterKey());
        assertEquals(9, loaded.amount());
        assertEquals(Direction.WEST, loaded.facing());
        assertTrue(loaded.isBlocked());
        assertEquals(tag, loaded.getUpdateTag(registries));
        assertEquals(0, loaded.storage(Direction.UP).contentRevision());
    }

    @Test
    void pullWaitsFiveTicksAndTransfersOneTypedUnit() {
        BlockEntityJar jar = jar();
        IEssentiaConduit source = mock(IEssentiaConduit.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.above(), Direction.DOWN))
                .thenReturn(source);
        when(source.releasesOn(Direction.DOWN)).thenReturn(true);
        when(source.pull(Direction.DOWN)).thenReturn(EssentiaPull.NONE);
        when(source.heldAspect(Direction.DOWN)).thenReturn(order);
        when(source.heldAmount(Direction.DOWN)).thenReturn(3);
        when(source.extract(order, 1, Direction.DOWN, false)).thenReturn(1);
        for (int i = 0; i < 4; i++) BlockEntityJar.serverTick(level, BlockPos.ZERO, jar.getBlockState(), jar);
        verifyNoInteractions(source);
        BlockEntityJar.serverTick(level, BlockPos.ZERO, jar.getBlockState(), jar);
        assertEquals(1, jar.amount());
        verify(source).extract(order, 1, Direction.DOWN, false);
    }

    @Test
    void jarNeverQueriesAnUnloadedSourceChunk() {
        when(level.hasChunk(0, 0)).thenReturn(false);
        BlockEntityJar jar = jar();
        for (int i = 0; i < 5; i++) BlockEntityJar.serverTick(level, BlockPos.ZERO, jar.getBlockState(), jar);
        verify(level, never())
                .getCapability(eq(EssentiaCapabilities.CONDUIT), any(BlockPos.class), any(Direction.class));
        assertEquals(0, jar.amount());
    }

    @Test
    void brainXpIsClampedAndRoundTripsWithItsExistingKey() {
        BlockEntityJarBrain brain =
                new BlockEntityJarBrain(BlockPos.ZERO, TTBlocks.JAR_BRAIN.get().defaultBlockState());
        brain.setXp(-1);
        assertEquals(0, brain.xp());
        brain.setXp(BlockEntityJarBrain.XP_MAX + 1);
        assertEquals(BlockEntityJarBrain.XP_MAX, brain.xp());
        CompoundTag tag = brain.getUpdateTag(registries);
        assertEquals(Set.of("XP"), tag.getAllKeys());
        BlockEntityJarBrain loaded = new BlockEntityJarBrain(BlockPos.ZERO, brain.getBlockState());
        loaded.loadAdditional(tag, registries);
        assertEquals(brain.xp(), loaded.xp());
    }

    @Test
    void brainJarAccountsForMergedOrbsAndNeverComputesNegativeExperience() {
        assertEquals(21, BlockEntityJarBrain.orbXp(7, 3));
        assertEquals(7, BlockEntityJarBrain.orbXp(7, 0));
        assertEquals(0, BlockEntityJarBrain.orbXp(-7, 3));
        assertEquals(Integer.MAX_VALUE, BlockEntityJarBrain.orbXp(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void jarBlocksStillUseTheirModelsOnOneTwentyOne() {
        BlockJar jar = (BlockJar) TTBlocks.JAR_NORMAL.get();
        BlockJarBrain brain = (BlockJarBrain) TTBlocks.JAR_BRAIN.get();
        assertEquals(RenderShape.MODEL, jar.getRenderShape(jar.defaultBlockState()));
        assertEquals(RenderShape.MODEL, brain.getRenderShape(brain.defaultBlockState()));
    }

    @Test
    void scoopingAStackedJarDropsOnlyTheFilledJarWhenInventoryIsFull() {
        ServerLevel server = mock(ServerLevel.class);
        when(server.registryAccess()).thenReturn(registries);
        BlockEntityAlembic alembic = mock(BlockEntityAlembic.class);
        when(server.getBlockEntity(BlockPos.ZERO)).thenReturn(alembic);
        when(alembic.aspectKey()).thenReturn(TTAspects.ORDO);
        when(alembic.amount()).thenReturn(5);
        when(alembic.extract(order, 5, Direction.UP, false)).thenReturn(5);
        Player player = mock(Player.class);
        Inventory inventory = mock(Inventory.class);
        when(player.getInventory()).thenReturn(inventory);
        when(player.level()).thenReturn(server);
        ItemStack held = new ItemStack(TTItems.JAR_NORMAL.get(), 2);
        UseOnContext context = mock(UseOnContext.class);
        when(context.getLevel()).thenReturn(server);
        when(context.getClickedPos()).thenReturn(BlockPos.ZERO);
        when(context.getPlayer()).thenReturn(player);
        when(context.getHand()).thenReturn(InteractionHand.MAIN_HAND);
        when(context.getItemInHand()).thenReturn(held);

        assertEquals(InteractionResult.CONSUME, ((JarItem) held.getItem()).useOn(context));
        assertEquals(1, held.getCount());
        ArgumentCaptor<ItemStack> dropped = ArgumentCaptor.forClass(ItemStack.class);
        verify(player).spawnAtLocation(dropped.capture());
        assertEquals(1, dropped.getValue().getCount());
        assertEquals(
                5,
                dropped.getValue().get(TTDataComponents.ESSENTIA_CONTENTS.get()).totalAmount());
        verify(player, never()).setItemInHand(any(), any());
        verify(player, never()).drop(any(ItemStack.class), anyBoolean());
    }
}
