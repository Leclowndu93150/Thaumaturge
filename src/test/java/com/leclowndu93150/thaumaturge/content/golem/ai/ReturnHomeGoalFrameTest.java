package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class ReturnHomeGoalFrameTest {
    @Test
    void aGolemHoldingItemsDoesNotCompeteWithItsChestDeliveryGoal() {
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        var hands = mock(com.leclowndu93150.thaumaturge.api.golems.IGolemHands.class);
        var cargo = mock(net.minecraft.world.item.ItemStack.class);
        when(hands.contents()).thenReturn(java.util.List.of(cargo));
        when(cargo.isEmpty()).thenReturn(false);
        when(golem.hands()).thenReturn(hands);
        ReturnHomeGoal goal = new ReturnHomeGoal(golem);
        for (int i = 0; i < 30; i++) assertFalse(goal.canUse());
        // No path query or random home movement is permitted while carrying.
        verify(golem, never()).getRestrictCenter();
    }

    @Test
    void homeRoutingKeepsTheOriginalLocalAnchorAfterDeckMoves() {
        Level level = mock(Level.class);
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        GolemSpaceNavigation navigation = mock(GolemSpaceNavigation.class);
        SpaceFrame deck = mock(SpaceFrame.class);
        BlockPos localHome = new BlockPos(10, 20, 30);
        Vec3 localFeet = Vec3.atBottomCenterOf(localHome);
        AtomicReference<Vec3> translation = new AtomicReference<>(new Vec3(-10, -20, -30));
        when(golem.level()).thenReturn(level);
        when(golem.getNavigation()).thenReturn(navigation);
        when(golem.getRestrictCenter()).thenReturn(localHome);
        when(golem.getGolemMoveSpeed()).thenReturn(1.0F);
        when(golem.getAttributeValue(Attributes.FOLLOW_RANGE)).thenReturn(16.0);
        when(golem.position()).thenReturn(new Vec3(3.5, 0.0, 0.5));
        when(deck.toWorld(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(call -> ((Vec3) call.getArgument(0)).add(translation.get()));
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.frameOf(level, localHome)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.trackedFrame(golem)).thenReturn(WorldFrame.INSTANCE);
            ReturnHomeGoal goal = new ReturnHomeGoal(golem);
            for (int tick = 0; tick < 10; tick++) assertFalse(goal.canUse());
            assertTrue(goal.canUse());
            goal.start();
            verify(navigation).moveToFrame(deck, localFeet, 1.0);
            translation.set(new Vec3(-8, -20, -30));
            // The same home block now has a different world coordinate, but
            // the return goal must continue to track its local anchor.
            assertTrue(goal.canContinueToUse());
        }
    }

    @Test
    void fastRotatingDeckUsesActualHomeWithinRealFollowRangeNotRandomIntermediateTargets() {
        Level level = mock(Level.class);
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        GolemSpaceNavigation navigation = mock(GolemSpaceNavigation.class);
        SpaceFrame deck = mock(SpaceFrame.class);
        BlockPos homeBlock = new BlockPos(0, 64, 0);
        when(golem.level()).thenReturn(level);
        when(golem.getNavigation()).thenReturn(navigation);
        when(golem.getRestrictCenter()).thenReturn(homeBlock);
        when(golem.getGolemMoveSpeed()).thenReturn(1.0F);
        when(golem.getAttributeValue(Attributes.FOLLOW_RANGE)).thenReturn(40.0);
        // Twenty blocks away exceeds the previous arbitrary fourteen-block cap,
        // but is safely inside the golem's actual forty-block search range.
        when(golem.position()).thenReturn(new Vec3(20.5, 64, 0.5));
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.frameOf(level, homeBlock)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.trackedFrame(golem)).thenReturn(deck);
            ReturnHomeGoal goal = new ReturnHomeGoal(golem);
            for (int i = 0; i < 10; i++) assertFalse(goal.canUse());
            assertTrue(goal.canUse());
            goal.start();
            // A rotating golem must search for the stationary world home, not
            // randomized world-space targets that change on each goal restart.
            verify(navigation).moveTo(0.5, 64.0, 0.5, 1.0);
            verify(navigation, never()).moveToFrame(eq(deck), org.mockito.ArgumentMatchers.any(), anyDouble());
        }
    }

    @Test
    void homeRequiresTheOriginalFrameAndPreciseCenter() {
        Vec3 home = new Vec3(10.5, 64, 20.5);
        SpaceFrame deck = mock(SpaceFrame.class);
        assertFalse(
                ReturnHomeGoal.arrivedAtHome(new Vec3(11.0, 64, 20.5), home, WorldFrame.INSTANCE, WorldFrame.INSTANCE));
        assertFalse(ReturnHomeGoal.arrivedAtHome(home, home, deck, WorldFrame.INSTANCE));
        assertFalse(
                ReturnHomeGoal.arrivedAtHome(new Vec3(10.5, 65, 20.5), home, WorldFrame.INSTANCE, WorldFrame.INSTANCE));
        assertTrue(ReturnHomeGoal.arrivedAtHome(
                new Vec3(10.6, 64, 20.55), home, WorldFrame.INSTANCE, WorldFrame.INSTANCE));
    }

    @Test
    void finalApproachSteersToExactSpawnCenterInsteadOfStoppingAtNativePathTolerance() {
        Level level = mock(Level.class);
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        GolemSpaceNavigation navigation = mock(GolemSpaceNavigation.class);
        MoveControl moveControl = mock(MoveControl.class);
        BlockPos homeBlock = new BlockPos(10, 64, 20);
        AtomicReference<Vec3> position = new AtomicReference<>(new Vec3(10.95, 64, 20.5));
        when(golem.level()).thenReturn(level);
        when(golem.getNavigation()).thenReturn(navigation);
        when(golem.getMoveControl()).thenReturn(moveControl);
        when(golem.getRestrictCenter()).thenReturn(homeBlock);
        when(golem.position()).thenAnswer(call -> position.get());
        when(golem.onGround()).thenReturn(true);
        when(golem.getGolemMoveSpeed()).thenReturn(1.0F);
        when(golem.getAttributeValue(Attributes.FOLLOW_RANGE)).thenReturn(40.0);
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.frameOf(level, homeBlock)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.trackedFrame(golem)).thenReturn(WorldFrame.INSTANCE);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(golem), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> new GolemPathGeometry.Surface(
                            WorldFrame.INSTANCE, new Vec3(call.getArgument(2), 64, call.getArgument(3))));
            geometry.when(() ->
                            GolemPathGeometry.safeJumpLanding(eq(level), eq(golem), org.mockito.ArgumentMatchers.any()))
                    .thenReturn(true);
            ReturnHomeGoal goal = new ReturnHomeGoal(golem);
            for (int i = 0; i < 10; i++) assertFalse(goal.canUse());
            assertTrue(goal.canUse());
            goal.start();
            verify(navigation).stop();
            verify(moveControl).setWantedPosition(10.5, 64.0, 20.5, 0.45);
            assertTrue(goal.canContinueToUse());
            position.set(new Vec3(10.61, 64, 20.5));
            assertFalse(goal.canContinueToUse());
        }
    }
}
