package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.wands.WandChargingEvents;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Lifecycle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class NodeHungerPortTest {
    private int originalEatInterval;

    @BeforeEach
    void setUp() {
        ModConfigSpec.IntValue interval = ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_EAT_INTERVAL;
        originalEatInterval = interval.get();
        interval.set(17);
    }

    @AfterEach
    void tearDown() {
        ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_EAT_INTERVAL.set(originalEatInterval);
    }

    private static BlockEntityNode node() {
        return new BlockEntityNode(BlockPos.ZERO, TTBlocks.NODE.get().defaultBlockState());
    }

    private static ServerLevel level(RandomSource random) {
        ServerLevel level = mock(ServerLevel.class);
        ServerChunkCache chunks = mock(ServerChunkCache.class);
        when(level.getChunkSource()).thenReturn(chunks);
        when(chunks.getChunkNow(anyInt(), anyInt())).thenReturn(mock(LevelChunk.class));
        when(level.getRandom()).thenReturn(random);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.isPositionEntityTicking(any())).thenReturn(true);
        return level;
    }

    private static void setInt(BlockEntityNode node, String name, int value) throws Exception {
        Field field = BlockEntityNode.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setInt(node, value);
    }

    private static void setCenter(Entity entity, Vec3 center) {
        when(entity.position()).thenReturn(center);
        when(entity.blockPosition()).thenReturn(BlockPos.containing(center));
        when(entity.getBoundingBox())
                .thenReturn(new AABB(
                        center.x - 0.25,
                        center.y - 0.25,
                        center.z - 0.25,
                        center.x + 0.25,
                        center.y + 0.25,
                        center.z + 0.25));
    }

    private static void invokeFeed(NodeHunger hunger, ServerLevel level, AspectList source, RandomSource random)
            throws Exception {
        Method method =
                NodeHunger.class.getDeclaredMethod("feed", ServerLevel.class, AspectList.class, RandomSource.class);
        method.setAccessible(true);
        method.invoke(hunger, level, source, random);
    }

    @Test
    void entitiesBetweenSourceAndCurrentMawThresholdArePulledAndDamaged() {
        RandomSource random = mock(RandomSource.class);
        ServerLevel level = level(random);
        DamageSources damageSources = mock(DamageSources.class);
        DamageSource damageSource = mock(DamageSource.class);
        when(level.damageSources()).thenReturn(damageSources);
        when(damageSources.fellOutOfWorld()).thenReturn(damageSource);
        BlockEntityNode node = node();
        NodeHunger hunger = new NodeHunger(node);
        Entity entity = mock(Entity.class);
        when(entity.isAlive()).thenReturn(true);
        when(entity.level()).thenReturn(level);
        setCenter(entity, new Vec3(1.85, 0.5, 0.5));
        when(level.getEntitiesOfClass(eq(Entity.class), any(AABB.class), any())).thenReturn(List.of(entity));

        hunger.tick(level, BlockPos.ZERO, 10);

        verify(entity).hurt(damageSource, 1.0F);
        verify(entity).push(anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void itemEssenceIsReadFromOneCopiedItemEvenWhenTheEntityStackIsLarger() throws Exception {
        NodeHunger hunger = new NodeHunger(node());
        ItemEntity item = mock(ItemEntity.class);
        ItemStack stack = new ItemStack(Items.STONE, 64);
        when(item.getItem()).thenReturn(stack);
        Method method = NodeHunger.class.getDeclaredMethod("essenceOf", Entity.class);
        method.setAccessible(true);
        try (MockedStatic<AspectIndexAccess> index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY);

            assertEquals(AspectList.EMPTY, method.invoke(hunger, item));

            index.verify(() -> AspectIndexAccess.of(argThat((ItemStack candidate) -> candidate.getCount() == 1)));
        }
    }

    @Test
    void fullCapacityGrowthUsesFloatChanceAndAddsOnlyBaseCapacity() throws Exception {
        ResourceKeyHolder holder = registeredIgnis();
        ServerLevel level = mock(ServerLevel.class);
        when(level.registryAccess()).thenReturn(holder.registryAccess());
        BlockEntityNode node = node();
        node.setLevel(level);
        node.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(holder.aspect(), 1)));
        node.aspects = node.aspectsBase;
        NodeHunger hunger = new NodeHunger(node);
        AspectList essence = AspectList.EMPTY.add(holder.aspect(), 1);
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(1)).thenReturn(0);
        when(random.nextFloat()).thenReturn(0.0F);
        try (MockedStatic<WandChargingEvents> wand = mockStatic(WandChargingEvents.class)) {
            wand.when(() -> WandChargingEvents.reduceToPrimals(essence)).thenReturn(Map.of(TTAspects.IGNIS, 1));

            invokeFeed(hunger, level, essence, random);

            assertEquals(2, node.aspectsBase.amountOf(holder.aspect()));
            assertEquals(1, node.aspects.amountOf(holder.aspect()));
            verify(random).nextFloat();
            verify(random, never()).nextInt(3);
        }
    }

    @Test
    void pullCacheIsClearedWhenItsOwningLevelChanges() {
        RandomSource random = mock(RandomSource.class);
        ServerLevel firstLevel = level(random);
        ServerLevel secondLevel = level(random);
        BlockEntityNode node = node();
        NodeHunger hunger = new NodeHunger(node);
        Entity entity = mock(Entity.class);
        when(entity.isAlive()).thenReturn(true);
        when(entity.level()).thenReturn(firstLevel);
        setCenter(entity, new Vec3(5.0, 0.5, 0.5));
        when(firstLevel.getEntitiesOfClass(eq(Entity.class), any(AABB.class), any()))
                .thenReturn(List.of(entity));

        hunger.tick(firstLevel, BlockPos.ZERO, 10);
        hunger.tick(secondLevel, BlockPos.ZERO, 11);

        verify(entity, times(1)).push(anyDouble(), anyDouble(), anyDouble());
        verify(secondLevel, never()).getEntitiesOfClass(eq(Entity.class), any(AABB.class), any());
    }

    @Test
    void serverCycleRefillsBeforeHungryEntityDamage() throws Exception {
        RandomSource random = mock(RandomSource.class);
        ServerLevel level = level(random);
        DamageSources damageSources = mock(DamageSources.class);
        when(level.damageSources()).thenReturn(damageSources);
        when(damageSources.fellOutOfWorld()).thenReturn(mock(DamageSource.class));
        BlockEntityNode node = node();
        node.setNodeType(com.leclowndu93150.thaumaturge.api.nodes.NodeType.HUNGRY);
        node.setLevel(level);
        setInt(node, "count", 599);
        ResourceKeyHolder holder = registeredIgnis();
        node.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(holder.aspect(), 2)));
        node.aspects = AspectList.ofEntries(List.of(new AspectInstance(holder.aspect(), 1)));
        when(level.registryAccess()).thenReturn(holder.registryAccess());
        Entity entity = mock(Entity.class);
        when(entity.isAlive()).thenReturn(true, true, false, false);
        when(entity.level()).thenReturn(level);
        setCenter(entity, new Vec3(1.0, 0.5, 0.5));
        when(level.getEntitiesOfClass(eq(Entity.class), any(AABB.class), any())).thenReturn(List.of(entity));
        AtomicInteger eventOrder = new AtomicInteger();
        AtomicInteger refillOrder = new AtomicInteger();
        AtomicInteger damageOrder = new AtomicInteger();
        when(entity.hurt(any(DamageSource.class), anyFloat())).thenAnswer(invocation -> {
            damageOrder.set(eventOrder.incrementAndGet());
            return true;
        });
        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class);
                MockedStatic<EntityAspects> entityAspects = mockStatic(EntityAspects.class)) {
            aura.when(() -> AuraHelper.getAuraBase(level, BlockPos.ZERO)).thenReturn(0);
            aura.when(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 3.0F, false))
                    .thenAnswer(invocation -> {
                        refillOrder.set(eventOrder.incrementAndGet());
                        return 3.0F;
                    });
            entityAspects.when(() -> EntityAspects.of(entity)).thenReturn(AspectList.EMPTY);

            node.serverTick(level, BlockPos.ZERO);

            assertEquals(2, node.aspects.amountOf(holder.aspect()));
            assertTrue(refillOrder.get() > 0);
            assertTrue(damageOrder.get() > refillOrder.get());
        }
    }

    @Test
    void hungryPullCacheRefreshRunsAtTenTickBoundaryWithoutFiftyTickGate() throws Exception {
        RandomSource random = mock(RandomSource.class);
        ServerLevel level = level(random);
        BlockEntityNode node = node();
        node.setNodeType(com.leclowndu93150.thaumaturge.api.nodes.NodeType.HUNGRY);
        Entity entity = mock(Entity.class);
        when(entity.isAlive()).thenReturn(true);
        when(entity.level()).thenReturn(level);
        setCenter(entity, new Vec3(5.0, 0.5, 0.5));
        when(level.getEntitiesOfClass(eq(Entity.class), any(AABB.class), any())).thenReturn(List.of(entity));
        Method behavior =
                BlockEntityNode.class.getDeclaredMethod("handleTypeBehavior", ServerLevel.class, BlockPos.class);
        behavior.setAccessible(true);

        setInt(node, "count", 9);
        behavior.invoke(node, level, BlockPos.ZERO);
        verify(level, never()).getEntitiesOfClass(eq(Entity.class), any(AABB.class), any());

        setInt(node, "count", 10);
        behavior.invoke(node, level, BlockPos.ZERO);

        verify(level).getEntitiesOfClass(eq(Entity.class), any(AABB.class), any());
    }

    @Test
    void entitiesOutsideTheSphericalPullRangeAreNotDragged() {
        RandomSource random = mock(RandomSource.class);
        ServerLevel level = level(random);
        NodeHunger hunger = new NodeHunger(node());
        Entity entity = mock(Entity.class);
        when(entity.isAlive()).thenReturn(true);
        when(entity.level()).thenReturn(level);
        setCenter(entity, new Vec3(14.0, 14.0, 0.5));
        when(level.getEntitiesOfClass(eq(Entity.class), any(AABB.class), any())).thenReturn(List.of(entity));

        hunger.tick(level, BlockPos.ZERO, 10);

        verify(entity, never()).push(anyDouble(), anyDouble(), anyDouble());
        verify(entity, never()).hurt(any(DamageSource.class), anyFloat());
    }

    @Test
    void playerCanBePulledButDoesNotFeedAspectEssenceToTheNode() throws Exception {
        NodeHunger hunger = new NodeHunger(node());
        Player player = mock(Player.class);
        Method method = NodeHunger.class.getDeclaredMethod("essenceOf", Entity.class);
        method.setAccessible(true);

        assertEquals(AspectList.EMPTY, method.invoke(hunger, player));
    }

    @Test
    void blockTargetSamplingDoesNotInspectOrLoadAnUnloadedChunk() {
        Level level = mock(Level.class);
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(anyInt())).thenReturn(1, 0, 0, 0, 0, 0);
        when(level.hasChunkAt(any(BlockPos.class))).thenReturn(false);

        assertNull(NodeHunger.findTarget(level, BlockPos.ZERO, null, random));

        verify(level).hasChunkAt(any(BlockPos.class));
        verify(level, never()).getHeight(any(), anyInt(), anyInt());
        verify(level, never()).clip(any());
    }

    private static ResourceKeyHolder registeredIgnis() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        IAspect ignis = mock(IAspect.class);
        when(ignis.isPrimal()).thenReturn(true);
        Registry.register(registry, TTAspects.IGNIS, ignis);
        RegistryAccess access = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        return new ResourceKeyHolder(registry.getHolderOrThrow(TTAspects.IGNIS), access);
    }

    private record ResourceKeyHolder(Holder<IAspect> aspect, RegistryAccess registryAccess) {}
}
