package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class FluxRiftPortTest {
    private static EntityFluxRift rift() {
        return new EntityFluxRift(TTEntities.FLUX_RIFT.get(), mock(Level.class));
    }

    @Test
    void collapsePersistenceRetainsOriginalMaximumSizeAfterTheRiftShrinks() {
        EntityFluxRift original = rift();
        original.resize(17);
        original.beginCollapse();
        original.resize(2);

        CompoundTag saved = new CompoundTag();
        original.addAdditionalSaveData(saved);
        assertEquals(17, saved.getInt("collapse_size"));
        assertEquals(2, saved.getInt("rift_size"));

        EntityFluxRift loaded = rift();
        loaded.readAdditionalSaveData(saved);
        assertTrue(loaded.isCollapsing());
        assertEquals(17, loaded.collapseSize());
        assertEquals(2, loaded.currentSize());
    }

    @Test
    void collapseReleasesFluxOrVisInsteadOfPollutingAura() {
        EntityFluxRift rift = spy(rift());
        rift.resize(5);
        rift.beginCollapse();
        RandomSource random = mock(RandomSource.class);
        when(rift.getRandom()).thenReturn(random);
        when(random.nextBoolean()).thenReturn(true);
        when(random.nextInt(10)).thenReturn(1);
        ServerLevel level = mock(ServerLevel.class);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            assertFalse(new RiftCollapse().tick(level, rift));
            aura.verify(() -> AuraHelper.addVis(level, BlockPos.ZERO, 1.0F));
            aura.verify(() -> AuraHelper.addFlux(level, BlockPos.ZERO, 1.0F), never());
            aura.verifyNoMoreInteractions();
        }
    }

    @Test
    void scheduledGrowthRunsBeforeTheSameTickRiftEvent() {
        EntityFluxRift rift = spy(rift());
        rift.setId(600);
        rift.tickCount = 600;
        rift.adjustStability(-94.0F);
        RandomSource random = mock(RandomSource.class);
        when(rift.getRandom()).thenReturn(random);
        when(random.nextInt(1000)).thenReturn(99);
        when(random.nextInt(81)).thenReturn(80);
        ServerLevel level = mock(ServerLevel.class);

        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.getFlux(level, BlockPos.ZERO)).thenReturn(100.0F);
            new RiftTaskSchedule(List.of(new RiftGrowthTask(), new RiftEventTask())).tick(level, rift);
        }

        assertEquals(6, rift.currentSize());
        assertTrue(rift.isCollapsing());
        assertEquals(6, rift.collapseSize());
    }

    @Test
    void sweepDoesNotClipOrQueryEntitiesWhenEitherEndpointChunkIsUnloaded() {
        EntityFluxRift rift = rift();
        rift.resize(6);
        ServerLevel level = mock(ServerLevel.class);
        when(level.hasChunkAt(any(BlockPos.class))).thenReturn(false);

        new RiftSegmentSweep().tick(level, rift);

        verify(level).hasChunkAt(any(BlockPos.class));
        verify(level, never()).clip(any(ClipContext.class));
        verify(level, never()).getEntities(any(EntityFluxRift.class), any(AABB.class));
    }

    @Test
    void sweepUsesCollisionShapeSoBlocksWithoutCollisionAreSkipped() {
        EntityFluxRift rift = rift();
        rift.resize(6);
        ServerLevel level = mock(ServerLevel.class);
        when(level.hasChunkAt(any(BlockPos.class))).thenReturn(true);
        when(level.damageSources()).thenReturn(mock(DamageSources.class));
        when(level.getEntities(eq(rift), any(AABB.class))).thenReturn(List.of());
        BlockState shortGrass = Blocks.SHORT_GRASS.defaultBlockState();
        assertTrue(shortGrass.getCollisionShape(level, BlockPos.ZERO).isEmpty());
        when(level.clip(any(ClipContext.class))).thenAnswer(invocation -> {
            ClipContext context = invocation.getArgument(0);
            assertTrue(context.getBlockShape(shortGrass, level, BlockPos.ZERO).isEmpty());
            BlockHitResult miss = mock(BlockHitResult.class);
            when(miss.getType()).thenReturn(HitResult.Type.MISS);
            return miss;
        });

        new RiftSegmentSweep().tick(level, rift);

        verify(level).clip(any(ClipContext.class));
    }

    @Test
    void outlineIsSeedDeterministic() {
        List<Vec3> firstPoints = new ArrayList<>();
        List<Float> firstWidths = new ArrayList<>();
        List<Vec3> secondPoints = new ArrayList<>();
        List<Float> secondWidths = new ArrayList<>();
        RiftOutline.build(24, 42, firstPoints, firstWidths);
        RiftOutline.build(24, 42, secondPoints, secondWidths);
        assertEquals(firstPoints, secondPoints);
        assertEquals(firstWidths, secondWidths);
        assertEquals(firstPoints.size(), firstWidths.size());
    }
}
