package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

class GolemPathGeometryTest {
    @Test
    void worldSupportRequiresBodyClearanceAndLoadedTerrain() {
        Fixture fixture = new Fixture();
        fixture.blocks.put(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.framesTouching(any(), any())).thenReturn(List.of(WorldFrame.INSTANCE));
            var point = GolemPathGeometry.surface(fixture.level, fixture.mob, 0.5, 0.5, 0.5, 1.5);
            assertNotNull(point);
            assertEquals(1, point.worldFeet().y, 1.0E-6);
            fixture.blocks.put(new BlockPos(0, 1, 0), Blocks.STONE.defaultBlockState());
            assertNull(GolemPathGeometry.surface(fixture.level, fixture.mob, 0.5, 0.5, 0.5, 1.5));
            fixture.blocks.remove(new BlockPos(0, 1, 0));
            when(fixture.level.isLoaded(any())).thenReturn(false);
            assertFalse(GolemPathGeometry.clear(fixture.level, fixture.mob, new AABB(0.1, 1, 0.1, 0.9, 2, 0.9)));
            assertNull(GolemPathGeometry.surface(fixture.level, fixture.mob, 0.5, 0.5, 0.5, 1.5));
        }
    }

    @Test
    void startingSurfaceToleratesOnlyTinyDeckEdgeOverlap() {
        Fixture fixture = new Fixture();
        fixture.blocks.put(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
        fixture.blocks.put(new BlockPos(10, 0, 0), Blocks.STONE.defaultBlockState());
        MovingFrame frame = new MovingFrame(new Vec3(-9.0008, 1, 0), new Quaterniond());
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.framesTouching(any(), any())).thenReturn(List.of(WorldFrame.INSTANCE, frame));
            assertNull(GolemPathGeometry.surface(fixture.level, fixture.mob, 0.7999, 0.5, 0, 1.1));
            assertNotNull(GolemPathGeometry.startingSurface(fixture.level, fixture.mob, 0.7999, 0.5, 0, 1.1));
            assertNull(GolemPathGeometry.startingSurface(fixture.level, fixture.mob, 0.81, 0.5, 0, 1.1));
            fixture.blocks.put(new BlockPos(10, 0, 0), Blocks.MAGMA_BLOCK.defaultBlockState());
            assertNull(GolemPathGeometry.startingSurface(fixture.level, fixture.mob, 0.7999, 0.5, 0, 1.1));
        }
    }

    @Test
    void translatedDeckAnchorsFollowMotionAndDisappearFromTheirOldPosition() {
        Fixture fixture = new Fixture();
        fixture.blocks.put(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
        MovingFrame frame = new MovingFrame(new Vec3(3, 2, 4), new Quaterniond());
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.framesTouching(any(), any())).thenReturn(List.of(WorldFrame.INSTANCE, frame));
            var point = GolemPathGeometry.surface(fixture.level, fixture.mob, 3.5, 4.5, 2.5, 3.5);
            assertNotNull(point);
            assertEquals(new Vec3(3.5, 3, 4.5), point.worldFeet());
            frame.translation = frame.translation.add(5, 0, 0);
            assertEquals(new Vec3(8.5, 3, 4.5), point.worldFeet());
            assertNull(GolemPathGeometry.surface(fixture.level, fixture.mob, 3.5, 4.5, 2.5, 3.5));
        }
    }

    @Test
    void rotatedShapeUsesExactBoxSeparationAndActualTopSurface() {
        Fixture fixture = new Fixture();
        fixture.blocks.put(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
        MovingFrame frame = new MovingFrame(new Vec3(3, 2, 4), new Quaterniond().rotateY(Math.PI / 4));
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.framesTouching(any(), any())).thenReturn(List.of(WorldFrame.INSTANCE, frame));
            assertTrue(GolemPathGeometry.clear(fixture.level, fixture.mob, new AABB(3.01, 2.1, 3.3, 3.06, 2.9, 3.35)));
            assertFalse(GolemPathGeometry.clear(fixture.level, fixture.mob, new AABB(3.5, 2.1, 3.9, 3.8, 2.9, 4.1)));
            var point = GolemPathGeometry.surface(fixture.level, fixture.mob, 3 + Math.sqrt(0.5), 4, 2.5, 3.5);
            assertNotNull(point);
            assertEquals(3, point.worldFeet().y, 1.0E-6);
        }
    }

    @Test
    void tiltedDeckSupportsAnUprightBodyAtItsUphillCorner() {
        Fixture fixture = new Fixture();
        fixture.blocks.put(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
        MovingFrame frame = new MovingFrame(new Vec3(3, 2, 4), new Quaterniond().rotateZ(Math.PI / 6));
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.framesTouching(any(), any())).thenReturn(List.of(WorldFrame.INSTANCE, frame));
            var point = GolemPathGeometry.surface(fixture.level, fixture.mob, 3, 4.5, 2, 4);
            assertNotNull(point);
            assertEquals(2 + (1 + 0.2 * Math.sin(Math.PI / 6)) / Math.cos(Math.PI / 6), point.worldFeet().y, 1.0E-6);
        }
    }

    @Test
    void verticalSurfaceDoesNotProvideWalkingSupport() {
        Fixture fixture = new Fixture();
        fixture.blocks.put(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
        MovingFrame frame = new MovingFrame(new Vec3(3, 2, 4), new Quaterniond().rotateZ(Math.PI / 2));
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.framesTouching(any(), any())).thenReturn(List.of(WorldFrame.INSTANCE, frame));
            assertNull(GolemPathGeometry.surface(fixture.level, fixture.mob, 2.5, 4.5, 2.1, 2.9));
        }
    }

    @Test
    void slightlyTiltedChestProvidesSupportAtItsEdge() {
        Fixture fixture = new Fixture();
        when(fixture.mob.getBbHeight()).thenReturn(0.9F);
        for (int x = 20481032; x <= 20481037; x++)
            for (int z = 20483073; z <= 20483080; z++)
                fixture.blocks.put(new BlockPos(x, 128, z), Blocks.JUNGLE_WOOD.defaultBlockState());
        fixture.blocks.put(new BlockPos(20481034, 129, 20483078), Blocks.JUNGLE_WOOD.defaultBlockState());
        fixture.blocks.put(new BlockPos(20481034, 130, 20483077), Blocks.JUNGLE_WOOD.defaultBlockState());
        fixture.blocks.put(new BlockPos(20481034, 131, 20483076), Blocks.JUNGLE_WOOD.defaultBlockState());
        fixture.blocks.put(new BlockPos(20481034, 129, 20483075), Blocks.CHEST.defaultBlockState());
        var rotation = new Quaterniond(-0.00003798537, -0.00345219625, 0.00014551207, 0.99999403954).normalize();
        var pivot = new Vector3d(20481034.805882357, 130.1345588235294, 20483077.158823516);
        var rotatedPivot = rotation.transform(pivot);
        var frame = new MovingFrame(
                new Vec3(
                        -13.03687858581543 - rotatedPivot.x,
                        -57.866676330566406 - rotatedPivot.y,
                        -2.4940266609191895 - rotatedPivot.z),
                rotation);
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.framesTouching(any(), any())).thenReturn(List.of(WorldFrame.INSTANCE, frame));
            assertNotNull(GolemPathGeometry.surface(
                    fixture.level,
                    fixture.mob,
                    -12.824113519422195,
                    -4.641003550940565,
                    -62.00120125558357,
                    -57.99120125558357));
        }
    }

    @Test
    void hazardousSupportAndLavaClearanceAreRejected() {
        Fixture fixture = new Fixture();
        fixture.blocks.put(BlockPos.ZERO, Blocks.MAGMA_BLOCK.defaultBlockState());
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.framesTouching(any(), any())).thenReturn(List.of(WorldFrame.INSTANCE));
            assertNull(GolemPathGeometry.surface(fixture.level, fixture.mob, 0.5, 0.5, 0.5, 1.5));
            fixture.blocks.put(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
            fixture.blocks.put(new BlockPos(0, 1, 0), Blocks.LAVA.defaultBlockState());
            assertNull(GolemPathGeometry.surface(fixture.level, fixture.mob, 0.5, 0.5, 0.5, 1.5));
        }
    }

    private static final class Fixture {
        final Level level = mock(Level.class);
        final Mob mob = mock(Mob.class);
        final Map<BlockPos, BlockState> blocks = new HashMap<>();

        Fixture() {
            when(level.isLoaded(any())).thenReturn(true);
            when(level.getBlockState(any()))
                    .thenAnswer(invocation ->
                            blocks.getOrDefault(invocation.getArgument(0), Blocks.AIR.defaultBlockState()));
            when(mob.getBbWidth()).thenReturn(0.4F);
            when(mob.getBbHeight()).thenReturn(1.0F);
        }
    }

    private static final class MovingFrame implements SpaceFrame {
        Vec3 translation;
        final Quaterniond rotation;

        MovingFrame(Vec3 translation, Quaterniond rotation) {
            this.translation = translation;
            this.rotation = rotation;
        }

        @Override
        public boolean isWorld() {
            return false;
        }

        @Override
        public Vec3 toWorld(Vec3 local) {
            return directionToWorld(local).add(translation);
        }

        @Override
        public Vec3 toLocal(Vec3 world) {
            return directionToLocal(world.subtract(translation));
        }

        @Override
        public Vec3 directionToWorld(Vec3 local) {
            Vector3d vector = rotation.transform(new Vector3d(local.x, local.y, local.z));
            return new Vec3(vector.x, vector.y, vector.z);
        }

        @Override
        public Vec3 directionToLocal(Vec3 world) {
            Vector3d vector = rotation.transformInverse(new Vector3d(world.x, world.y, world.z));
            return new Vec3(vector.x, vector.y, vector.z);
        }

        @Override
        public void push(BlockPos source, Vec3 point, Vec3 direction, double lift, double speed) {}
    }
}
