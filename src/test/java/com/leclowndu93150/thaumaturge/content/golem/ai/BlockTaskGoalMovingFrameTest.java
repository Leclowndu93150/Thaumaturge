package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class BlockTaskGoalMovingFrameTest {
    @Test
    void rotatingSableBlockTaskUsesStableLocalAnchor() {
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        GolemSpaceNavigation navigation = mock(GolemSpaceNavigation.class);
        Level level = mock(Level.class);
        Task task = mock(Task.class);
        SpaceFrame rotating = mock(SpaceFrame.class);
        BlockPos plotPosition = new BlockPos(20_481_030, 130, 20_499_458);
        when(task.pos()).thenReturn(plotPosition);
        when(golem.level()).thenReturn(level);
        when(golem.getNavigation()).thenReturn(navigation);
        when(golem.getGolemMoveSpeed()).thenReturn(1.0F);
        when(rotating.isWorld()).thenReturn(false);
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.frameOf(level, plotPosition)).thenReturn(rotating);
            new BlockTaskGoal(golem).approach(task);
            verify(navigation).moveToFrame(rotating, Vec3.atCenterOf(plotPosition), 1.0);
            verify(navigation, never()).moveTo(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        }
    }

    @Test
    void rotatingBlockEndpointAcceptanceDoesNotDependOnIntegerWorldCells() {
        // Physically close results across a Minecraft block boundary should
        // remain equally reachable as the entire deck rotates.
        Vec3 target = new Vec3(73.99, -47.0, 49.99);
        assertTrue(BlockTaskGoal.pathEndpointReaches(new Vec3(74.31, -47.0, 50.18), target));
        assertTrue(BlockTaskGoal.pathEndpointReaches(new Vec3(73.70, -47.0, 49.78), target));
        assertFalse(BlockTaskGoal.pathEndpointReaches(new Vec3(76.0, -47.0, 52.0), target));
        // Keep the historic special-case for a two-block-high block endpoint.
        assertTrue(BlockTaskGoal.pathEndpointReaches(new Vec3(74.0, -45.0, 50.0), target));
    }

    @Test
    void vanillaBlockTaskStillUsesNormalNavigation() {
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        GolemSpaceNavigation navigation = mock(GolemSpaceNavigation.class);
        Level level = mock(Level.class);
        Task task = mock(Task.class);
        BlockPos block = new BlockPos(10, 64, 7);
        Vec3 goal = Vec3.atCenterOf(block);
        when(task.pos()).thenReturn(block);
        when(golem.level()).thenReturn(level);
        when(golem.getNavigation()).thenReturn(navigation);
        when(golem.getGolemMoveSpeed()).thenReturn(1.0F);
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.frameOf(level, block)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(level, block)).thenReturn(goal);
            new BlockTaskGoal(golem).approach(task);
            verify(navigation).moveTo(goal.x, goal.y, goal.z, 1.0);
            verify(navigation, never()).moveToFrame(WorldFrame.INSTANCE, goal, 1.0);
        }
    }
}
