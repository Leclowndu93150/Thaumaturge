package com.leclowndu93150.thaumaturge.content.warp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import com.leclowndu93150.thaumaturge.content.entity.EntityMindSpider;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

class WarpSpawnParityTest {
    @Test
    void deadlyGazeExcludesSpectatorsFromItsTargetQuery() {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        MobEffectInstance gaze = mock(MobEffectInstance.class);
        when(player.level()).thenReturn(level);
        when(player.getEffect(TTMobEffects.DEATH_GAZE)).thenReturn(gaze);
        when(player.getBoundingBox()).thenReturn(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
        when(gaze.getAmplifier()).thenReturn(0);
        when(level.getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), any()))
                .thenReturn(List.of());

        WarpEvents.checkDeathGaze(player);

        verify(level).getEntitiesOfClass(eq(LivingEntity.class), any(AABB.class), same(EntitySelector.NO_SPECTATORS));
    }

    @Test
    void spiderSummonCreatesOnceAcrossPlacementRetries() {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        RandomSource random = mock(RandomSource.class);
        BlockState floor = mock(BlockState.class);
        AABB bounds = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        when(player.level()).thenReturn(level);
        when(player.getRandom()).thenReturn(random);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.VANILLA_SET);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(floor);
        when(floor.isCollisionShapeFullBlock(eq(level), any(BlockPos.class))).thenReturn(true);
        when(level.noCollision(any(Entity.class))).thenReturn(false, true);
        when(level.containsAnyLiquid(bounds)).thenReturn(false);

        try (MockedConstruction<EntityMindSpider> spiders = mockConstruction(
                        EntityMindSpider.class,
                        (spider, context) -> when(spider.getBoundingBox()).thenReturn(bounds));
                MockedStatic<WarpManager> warpManager = mockStatic(WarpManager.class)) {
            WarpEvents.suddenlySpiders(player, 1, true);

            assertEquals(1, spiders.constructed().size());
            verify(spiders.constructed().get(0), times(2)).moveTo(0.0, 0.0, 0.0, 0.0F, 0.0F);
        }
    }

    @Test
    void spiderSummonDrawsYawBeforeRejectingUnsupportedCandidate() {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        RandomSource random = mock(RandomSource.class);
        BlockState floor = mock(BlockState.class);
        AABB bounds = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        when(player.level()).thenReturn(level);
        when(player.getRandom()).thenReturn(random);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.VANILLA_SET);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(floor);
        when(floor.isCollisionShapeFullBlock(eq(level), any(BlockPos.class))).thenReturn(false, true);
        when(level.noCollision(any(Entity.class))).thenReturn(true);
        when(level.containsAnyLiquid(bounds)).thenReturn(false);
        when(random.nextFloat()).thenReturn(0.25F, 0.75F);

        try (MockedConstruction<EntityMindSpider> spiders = mockConstruction(
                        EntityMindSpider.class,
                        (spider, context) -> when(spider.getBoundingBox()).thenReturn(bounds));
                MockedStatic<WarpManager> warpManager = mockStatic(WarpManager.class)) {
            WarpEvents.suddenlySpiders(player, 1, true);

            assertEquals(1, spiders.constructed().size());
            EntityMindSpider spider = spiders.constructed().get(0);
            verify(random, times(2)).nextFloat();
            verify(spider).moveTo(0.0, 0.0, 0.0, 270.0F, 0.0F);
        }
    }

    @Test
    void guardianSummonReusesItsEntityAcrossPlacementRetries() {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        RandomSource random = mock(RandomSource.class);
        BlockState floor = mock(BlockState.class);
        AABB bounds = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        BlockPos origin = new BlockPos(10, 20, 30);
        when(player.level()).thenReturn(level);
        when(player.getRandom()).thenReturn(random);
        when(player.blockPosition()).thenReturn(origin);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.VANILLA_SET);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(floor);
        when(floor.isCollisionShapeFullBlock(eq(level), any(BlockPos.class))).thenReturn(true);
        when(level.noCollision(any(Entity.class))).thenReturn(false, true);
        when(level.containsAnyLiquid(bounds)).thenReturn(false);
        when(level.addFreshEntity(any(Entity.class))).thenReturn(true);
        when(random.nextInt(3)).thenReturn(2);
        when(random.nextInt(18)).thenReturn(0);
        when(random.nextBoolean()).thenReturn(true);
        when(random.nextFloat()).thenReturn(0.25F, 0.75F);

        try (MockedConstruction<EntityEldritchGuardian> guardians = mockConstruction(
                EntityEldritchGuardian.class,
                (guardian, context) -> when(guardian.getBoundingBox()).thenReturn(bounds))) {
            GuardianSpawner.spawn(player, 1);

            assertEquals(1, guardians.constructed().size());
            EntityEldritchGuardian guardian = guardians.constructed().get(0);
            verify(guardian).moveTo(17.0, 27.0, 37.0, 90.0F, 0.0F);
            verify(guardian).moveTo(17.0, 27.0, 37.0, 270.0F, 0.0F);
            verify(level).addFreshEntity(guardian);
            verify(guardian).setTarget(player);
        }
    }

    @Test
    void portalSummonReusesItsEntityAcrossPlacementRetries() {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        RandomSource random = mock(RandomSource.class);
        BlockState floor = mock(BlockState.class);
        AABB bounds = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        BlockPos origin = new BlockPos(10, 20, 30);
        when(player.level()).thenReturn(level);
        when(player.getRandom()).thenReturn(random);
        when(player.blockPosition()).thenReturn(origin);
        when(level.enabledFeatures()).thenReturn(FeatureFlags.VANILLA_SET);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(floor);
        when(floor.isSolidRender(eq(level), any(BlockPos.class))).thenReturn(true);
        when(level.noCollision(any(Entity.class))).thenReturn(false, true);
        when(level.containsAnyLiquid(bounds)).thenReturn(false);
        when(level.addFreshEntity(any(Entity.class))).thenReturn(true);
        when(random.nextInt(3)).thenReturn(2);
        when(random.nextInt(18)).thenReturn(0);
        when(random.nextBoolean()).thenReturn(true);

        try (MockedConstruction<EntityCultistPortalLesser> portals = mockConstruction(
                        EntityCultistPortalLesser.class,
                        (portal, context) -> when(portal.getBoundingBox()).thenReturn(bounds));
                MockedStatic<EventHooks> eventHooks = mockStatic(EventHooks.class);
                MockedStatic<WarpManager> warpManager = mockStatic(WarpManager.class)) {
            GuardianSpawner.spawnPortal(player);

            assertEquals(1, portals.constructed().size());
            EntityCultistPortalLesser portal = portals.constructed().get(0);
            verify(portal, times(2)).moveTo(17.5, 28.0, 37.5, 0.0F, 0.0F);
            verify(level).addFreshEntity(portal);
        }
    }

    @Test
    void disabledEntityFactoriesDoNotReadPlacementOrMutateTheWorld() {
        ServerLevel level = mock(ServerLevel.class);
        ServerPlayer player = mock(ServerPlayer.class);
        RandomSource random = mock(RandomSource.class);
        when(player.level()).thenReturn(level);
        when(player.getRandom()).thenReturn(random);
        when(level.enabledFeatures()).thenReturn(FeatureFlagSet.of());
        assertFalse(TTEntities.ELDRITCH_GUARDIAN.get().isEnabled(FeatureFlagSet.of()));
        assertFalse(TTEntities.CULTIST_PORTAL_LESSER.get().isEnabled(FeatureFlagSet.of()));
        assertNull(TTEntities.ELDRITCH_GUARDIAN.get().create(level));
        assertNull(TTEntities.CULTIST_PORTAL_LESSER.get().create(level));

        GuardianSpawner.spawn(player, 1);
        GuardianSpawner.spawnPortal(player);

        verify(level, never()).getBlockState(any(BlockPos.class));
        verify(level, never()).addFreshEntity(any(Entity.class));
        verifyNoInteractions(random);
    }
}
