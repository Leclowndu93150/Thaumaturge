package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GolemSpaceNavigationTest {
    @Test
    void elevatedDeckExitRequiresShorterPhysicallyReachableLanding() {
        SpaceFrame rotating = mock(SpaceFrame.class);
        when(rotating.isWorld()).thenReturn(false);
        Vec3 takeoff = new Vec3(56.31, -52.62, 36.30);
        // Exact lethal edge from the repeated void-fall trace: 1m gap, +0.62m rise.
        assertFalse(GolemSpaceNavigation.safeElevatedExit(
                rotating, WorldFrame.INSTANCE, takeoff, takeoff.add(0, 0.62, 1.00)));
        // Slightly shorter, safer corner of the stationary platform remains legal.
        assertTrue(GolemSpaceNavigation.safeElevatedExit(
                rotating, WorldFrame.INSTANCE, takeoff, takeoff.add(0, 0.62, 0.70)));
        // Flat inter-frame gaps and all vanilla stair jumps keep previous rules.
        assertTrue(GolemSpaceNavigation.safeElevatedExit(
                rotating, WorldFrame.INSTANCE, takeoff, takeoff.add(0, 0.10, 1.00)));
        assertTrue(GolemSpaceNavigation.safeElevatedExit(
                WorldFrame.INSTANCE, rotating, takeoff, takeoff.add(0, 0.62, 1.00)));
    }

    @Test
    void foreignFrameSearchAllowsMovingStartTransformButKeepsNormalBound() {
        SpaceFrame rotating = mock(SpaceFrame.class);
        when(rotating.isWorld()).thenReturn(false);
        assertTrue(GolemSpaceNavigation.allowedStartTransformDriftSqr(rotating, WorldFrame.INSTANCE)
                > GolemSpaceNavigation.allowedStartTransformDriftSqr(WorldFrame.INSTANCE, WorldFrame.INSTANCE));
        assertTrue(GolemSpaceNavigation.allowedStartTransformDriftSqr(rotating, WorldFrame.INSTANCE)
                > GolemSpaceNavigation.allowedStartTransformDriftSqr(rotating, rotating));
    }

    @Test
    void crossFrameJumpWaitsForActualTakeoffInsteadOfJumpingFromBehind() {
        Vec3 launch = new Vec3(0.5, 1, 0.5);
        Vec3 landing = new Vec3(1.7, 1, 0.5);
        assertTrue(!GolemSpaceNavigation.nearCrossFrameTakeoff(new Vec3(0.24, 1, 0.5), launch, landing));
        assertTrue(GolemSpaceNavigation.nearCrossFrameTakeoff(new Vec3(0.46, 1, 0.5), launch, landing));
        assertTrue(GolemSpaceNavigation.nearCrossFrameTakeoff(new Vec3(0.7, 1, 0.5), launch, landing));
        assertTrue(!GolemSpaceNavigation.nearCrossFrameTakeoff(new Vec3(0.5, 1, 0.80), launch, landing));
        assertTrue(!GolemSpaceNavigation.nearCrossFrameTakeoff(new Vec3(0.5, 0.1, 0.5), launch, landing));
    }

    @Test
    void losingGroundContactWithoutUpwardMotionIsNotACompletedJump() {
        Vec3 takeoff = new Vec3(56.0, -52.67, 36.4);
        assertFalse(GolemSpaceNavigation.hasJumpLift(new Vec3(56.0, -52.66, 36.4), takeoff, -0.0784));
        assertFalse(GolemSpaceNavigation.hasJumpLift(new Vec3(56.0, -52.66, 36.4), takeoff, 0.0));
        assertFalse(GolemSpaceNavigation.hasJumpLift(new Vec3(56.0, -53.0, 36.4), takeoff, 0.42));
        assertTrue(GolemSpaceNavigation.hasJumpLift(new Vec3(56.0, -52.64, 36.4), takeoff, 0.37));
    }

    @Test
    void sameMovingDeckKeepsLocalSearchWhenRotationChangesItsWorldPose() {
        SpaceFrame rotatingDeck = mock(SpaceFrame.class);
        SpaceFrame otherDeck = mock(SpaceFrame.class);
        when(rotatingDeck.isWorld()).thenReturn(false);
        when(otherDeck.isWorld()).thenReturn(false);
        // Reproduces the 3.008 / 3.177-block drift logged after eleven
        // ticks. Neither location changed on the deck: do not cancel A*.
        assertFalse(GolemSpaceNavigation.invalidatedByTransformDrift(
                rotatingDeck, rotatingDeck, 3.008 * 3.008, 3.177 * 3.177));
        // Even a larger rotation of one rigid deck preserves local routes.
        assertFalse(
                GolemSpaceNavigation.invalidatedByTransformDrift(rotatingDeck, rotatingDeck, 20.0 * 20.0, 18.0 * 18.0));
        // Inter-frame transitions still have bounded pose drift because the
        // relative location of the two supporting surfaces really changed.
        assertFalse(
                GolemSpaceNavigation.invalidatedByTransformDrift(rotatingDeck, WorldFrame.INSTANCE, 8.9 * 8.9, 0.0));
        assertTrue(GolemSpaceNavigation.invalidatedByTransformDrift(rotatingDeck, WorldFrame.INSTANCE, 9.1 * 9.1, 0.0));
        assertFalse(
                GolemSpaceNavigation.invalidatedByTransformDrift(WorldFrame.INSTANCE, rotatingDeck, 0.0, 3.3 * 3.3));
        assertTrue(GolemSpaceNavigation.invalidatedByTransformDrift(WorldFrame.INSTANCE, rotatingDeck, 0.0, 9.1 * 9.1));
        assertTrue(GolemSpaceNavigation.invalidatedByTransformDrift(rotatingDeck, otherDeck, 0.0, 10.0 * 10.0));
    }

    @Test
    void rotatingStairRiseRoundoffDoesNotInvalidateTheSameOneBlockJump() {
        assertTrue(GolemSpaceNavigation.allowableJumpRise(1.00000055, 1.0));
        assertTrue(GolemSpaceNavigation.allowableJumpRise(0.9999998, 1.0));
        assertFalse(GolemSpaceNavigation.allowableJumpRise(1.02, 1.0));
    }

    @Test
    void failedStairLandingsFollowLocalCoordinatesAcrossRotationAndTaskTargets() {
        SpaceFrame deck = mock(SpaceFrame.class);
        SpaceFrame anotherDeck = mock(SpaceFrame.class);
        Vec3 failedLocal = new Vec3(12.5, 64, 4.5);
        assertTrue(GolemSpaceNavigation.sameFailedAscent(deck, failedLocal, deck, failedLocal.add(0.15, 0.0, -0.1)));
        assertFalse(GolemSpaceNavigation.sameFailedAscent(deck, failedLocal, deck, failedLocal.add(0.5, 0.0, 0.0)));
        assertFalse(GolemSpaceNavigation.sameFailedAscent(deck, failedLocal, anotherDeck, failedLocal));
    }

    @Test
    void rotatingSableStairEdgesStageTakeoffWithoutChangingVanillaJumps() {
        SpaceFrame rotatingDeck = mock(SpaceFrame.class);
        // This is the regression: the departure and landing are on the same
        // moving frame, but a one-block stair jump still needs a real takeoff.
        assertTrue(GolemSpaceNavigation.shouldStageJump(false, rotatingDeck, rotatingDeck));
        assertTrue(GolemSpaceNavigation.shouldStageJump(false, rotatingDeck, WorldFrame.INSTANCE));
        assertTrue(GolemSpaceNavigation.shouldStageJump(false, WorldFrame.INSTANCE, rotatingDeck));
        assertFalse(GolemSpaceNavigation.shouldStageJump(false, WorldFrame.INSTANCE, WorldFrame.INSTANCE));
        assertFalse(GolemSpaceNavigation.shouldStageJump(true, rotatingDeck, rotatingDeck));

        Vec3 takeoff = new Vec3(73.3, -46, 50.3);
        Vec3 stairLanding = new Vec3(73.3, -45, 51.3);
        // The previous build jumped from ~0.91 blocks behind the stair.
        assertFalse(GolemSpaceNavigation.nearCrossFrameTakeoff(new Vec3(73.27, -46, 49.4), takeoff, stairLanding));
        assertTrue(GolemSpaceNavigation.nearCrossFrameTakeoff(new Vec3(73.29, -46, 50.28), takeoff, stairLanding));
    }

    @Test
    void rotatingStairAirAssistOnlyHelpsAnAirborneLedgeCatch() {
        SpaceFrame deck = mock(SpaceFrame.class);
        Vec3 launch = new Vec3(73.4, -47.0, 48.6);
        Vec3 landing = new Vec3(74.4, -46.0, 48.6);
        assertTrue(GolemSpaceNavigation.isMovingDeckStairAscent(deck, deck, launch, landing));
        assertFalse(GolemSpaceNavigation.isMovingDeckStairAscent(
                WorldFrame.INSTANCE, WorldFrame.INSTANCE, launch, landing));
        assertFalse(GolemSpaceNavigation.isMovingDeckStairAscent(deck, WorldFrame.INSTANCE, launch, landing));
        assertFalse(GolemSpaceNavigation.isMovingDeckStairAscent(deck, deck, launch, landing.add(0, -0.5, 0)));

        Vec3 motion = new Vec3(0.07, 0.12, 0);
        Vec3 nearLedge = new Vec3(73.7, -45.96, 48.6);
        Vec3 boosted = GolemSpaceNavigation.movingStairAirAssist(nearLedge, motion, launch, landing, 4);
        assertTrue(boosted.x > motion.x);
        assertTrue(boosted.x <= motion.x + 0.090001);
        assertTrue(boosted.y == motion.y && boosted.z == motion.z);
        // Too early, too low, too late, or already close to the target:
        // none of these may add horizontal speed.
        assertSame(motion, GolemSpaceNavigation.movingStairAirAssist(nearLedge, motion, launch, landing, 1));
        assertSame(
                motion,
                GolemSpaceNavigation.movingStairAirAssist(new Vec3(73.7, -46.3, 48.6), motion, launch, landing, 4));
        assertSame(motion, GolemSpaceNavigation.movingStairAirAssist(nearLedge, motion, launch, landing, 11));
        assertSame(
                motion,
                GolemSpaceNavigation.movingStairAirAssist(new Vec3(74.0, -45.96, 48.6), motion, launch, landing, 4));
        Vec3 alreadyFast = new Vec3(0.32, 0.12, 0);
        assertSame(alreadyFast, GolemSpaceNavigation.movingStairAirAssist(nearLedge, alreadyFast, launch, landing, 4));
    }

    @Test
    void rotatingStairClimbRejectsDiagonalCornerButAllowsHeadOnFace() {
        SpaceFrame deck = mock(SpaceFrame.class);
        when(deck.toLocal(any())).thenAnswer(call -> call.getArgument(0));
        Vec3 from = new Vec3(0.5, 1, 0.5);
        // The failing route took the 1.0-by-0.5 diagonal across a one-block riser.
        assertFalse(GolemSpaceNavigation.headOnMovingStairJump(deck, deck, from, new Vec3(1.5, 2, 1.0)));
        assertFalse(GolemSpaceNavigation.headOnMovingStairJump(deck, deck, from, new Vec3(1.0, 2, 1.0)));
        assertTrue(GolemSpaceNavigation.headOnMovingStairJump(deck, deck, from, new Vec3(1.5, 2, 0.5)));
        assertTrue(GolemSpaceNavigation.headOnMovingStairJump(deck, deck, from, new Vec3(0.5, 2, 1.5)));
        // Face-on still isn't safe if the destination is on the far side
        // edge of the upper block instead of near its center.
        assertFalse(GolemSpaceNavigation.headOnMovingStairJump(deck, deck, from, new Vec3(1.5, 2, 0.05)));
        assertTrue(GolemSpaceNavigation.headOnMovingStairJump(deck, deck, from, new Vec3(1.5, 2, 0.73)));
        // Shallow step-ups, inter-frame crossings and vanilla routes are unchanged.
        assertTrue(GolemSpaceNavigation.headOnMovingStairJump(deck, deck, from, new Vec3(1.5, 1.4, 1.0)));
        assertTrue(GolemSpaceNavigation.headOnMovingStairJump(deck, WorldFrame.INSTANCE, from, new Vec3(1.5, 2, 1.0)));
        assertTrue(GolemSpaceNavigation.headOnMovingStairJump(
                WorldFrame.INSTANCE, WorldFrame.INSTANCE, from, new Vec3(1.5, 2, 1.0)));
    }

    @Test
    void rotatingStairAlignmentUsesDeckLocalAxesRatherThanWorldAxes() {
        SpaceFrame deck = mock(SpaceFrame.class);
        // Rotate the local frame by 45 degrees around Y: a cardinal local
        // approach is diagonal in world coordinates, and must remain legal.
        when(deck.toLocal(any())).thenAnswer(call -> {
            Vec3 v = call.getArgument(0);
            double c = Math.sqrt(0.5);
            return new Vec3((v.x + v.z) * c, v.y, (-v.x + v.z) * c);
        });
        // Inverse of local (0.5, 1, 0.5) under the mock rotation.
        Vec3 from = new Vec3(0, 1, Math.sqrt(0.5));
        // Add one local X block, which is diagonal in world X/Z.
        assertTrue(GolemSpaceNavigation.headOnMovingStairJump(
                deck, deck, from, new Vec3(Math.sqrt(0.5), 2, Math.sqrt(2))));
        // Adding world X instead changes both local axes, making it diagonal.
        assertFalse(GolemSpaceNavigation.headOnMovingStairJump(deck, deck, from, from.add(1, 1, 0)));
    }

    @Test
    void saferShortCornerJumpCostsLessThanDangerousLongDiagonalGap() {
        Vec3 from = new Vec3(0.5, 1, 0.5);
        double shortPenalty = GolemSpaceNavigation.crossFrameJumpPenalty(from, new Vec3(1.2, 1, 0.5));
        double longPenalty = GolemSpaceNavigation.crossFrameJumpPenalty(from, new Vec3(1.6, 1.4, 1.5));
        assertTrue(longPenalty > shortPenalty + 3.0);
    }

    @Test
    void crossingBetweenFramesSteersToDepartureBeforeRequestingJump() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        MoveControl move = mock(MoveControl.class);
        JumpControl jump = mock(JumpControl.class);
        SpaceFrame deck = mock(SpaceFrame.class);
        var position = new java.util.concurrent.atomic.AtomicReference<>(new Vec3(0.5, 1, 0.5));
        when(mob.position()).thenAnswer(call -> position.get());
        when(mob.getX()).thenAnswer(call -> position.get().x);
        when(mob.getY()).thenAnswer(call -> position.get().y);
        when(mob.getZ()).thenAnswer(call -> position.get().z);
        when(mob.onGround()).thenReturn(true);
        when(mob.getMoveControl()).thenReturn(move);
        when(mob.getJumpControl()).thenReturn(jump);
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.6F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        when(deck.toLocal(any())).thenAnswer(call -> call.getArgument(0));
        when(deck.toWorld(any())).thenAnswer(call -> call.getArgument(0));
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            BlockPos target = new BlockPos(2, 1, 0);
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.frameOf(level, target)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.worldCenter(level, target)).thenReturn(new Vec3(2.2, 1, 0.5));
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, deck));
            geometry.when(() -> GolemPathGeometry.body(eq(mob), any())).thenCallRealMethod();
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> {
                        double x = call.getArgument(2), z = call.getArgument(3);
                        if (Math.abs(z - 0.5) > 0.01) return null;
                        if (x >= 0.5 && x < 1.0)
                            return new GolemPathGeometry.Surface(WorldFrame.INSTANCE, new Vec3(x, 1, z));
                        if (x >= 1.5 && x <= 2.5) return new GolemPathGeometry.Surface(deck, new Vec3(x, 1, z));
                        return null; // The remaining gap has no supporting geometry.
                    });
            var nav = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            Path route = nav.createPath(Set.of(target), 0, false, 1, 16);
            assertNotNull(route);
            assertTrue(GolemSpaceNavigation.isCooperativePath(route));
            assertTrue(nav.moveTo(route, 1));
            position.set(new Vec3(0.26, 1, 0.5)); // Close enough to advance root node.
            nav.tick();
            verify(jump, never()).jump();
            // The source is at x=0.5, but support continues to x=0.9.
            // Approach the supported edge rather than jumping from x=0.26.
            verify(move).setWantedPosition(0.9, 1.0, 0.5, 1.0);
            position.set(new Vec3(0.9, 1, 0.5));
            nav.tick();
            verify(jump).jump();
            // The jump has only been requested: do NOT steer toward the far
            // platform until the controller actually lifts the mob off ground.
            verify(move).setWantedPosition(0.9, 1.0, 0.5, 0.35);
        }
    }

    @Test
    void frameAnchoredHomePathKeepsItsMovingEndpointWithoutRestarting() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        SpaceFrame deck = mock(SpaceFrame.class);
        var shift = new java.util.concurrent.atomic.AtomicReference<>(0.0);
        Vec3 localHome = new Vec3(5.5, 1, 0.5);
        when(mob.position()).thenAnswer(call -> new Vec3(0.5 + shift.get(), 1, 0.5));
        when(mob.getAttributeValue(Attributes.FOLLOW_RANGE)).thenReturn(16.0);
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.6F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        when(deck.toWorld(any())).thenAnswer(call -> ((Vec3) call.getArgument(0)).add(shift.get(), 0, 0));
        when(deck.toLocal(any())).thenAnswer(call -> ((Vec3) call.getArgument(0)).add(-shift.get(), 0, 0));
        when(deck.directionToWorld(any())).thenAnswer(call -> call.getArgument(0));
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, deck));
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> {
                        double x = call.getArgument(2), z = call.getArgument(3);
                        if (x < 0 || x > 7 || Math.abs(z - 0.5) > 0.1) return null;
                        Vec3 world = new Vec3(x, 1, z);
                        return new GolemPathGeometry.Surface(deck, deck.toLocal(world));
                    });
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            assertTrue(navigation.moveToFrame(deck, localHome, 1.0));
            Path originalRoute = navigation.getPath();
            assertNotNull(originalRoute);
            Vec3 oldEndpoint = GolemSpaceNavigation.pathEndWorld(level, originalRoute);
            // The golem rides the same rigid frame as its destination.
            shift.set(0.8);
            assertTrue(navigation.moveToFrame(deck, localHome, 1.0));
            assertSame(originalRoute, navigation.getPath());
            Vec3 newEndpoint = GolemSpaceNavigation.pathEndWorld(level, originalRoute);
            assertTrue(Math.abs(newEndpoint.x - oldEndpoint.x - 0.8) < 0.001);
        }
    }

    @Test
    void ordinaryWorldUsesNativeNavigationWithoutSublevelSearch() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        Path nativePath = mock(Path.class);
        BlockPos target = new BlockPos(5, 1, 0);
        when(mob.position()).thenReturn(new Vec3(0.5, 1, 0.5));
        when(nativeNavigation.createPath(target, 1)).thenReturn(nativePath);
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.frameOf(eq(level), eq(target))).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(eq(level), eq(target))).thenReturn(Vec3.atCenterOf(target));
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any())).thenReturn(List.of(WorldFrame.INSTANCE));
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            assertSame(nativePath, navigation.createPath(Set.of(target), 0, false, 1, 16));
            geometry.verifyNoInteractions();
        }
    }

    @Test
    void usesSablesNativeLocalPathForAnItemOnTheSameSublevel() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        Entity item = mock(Entity.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        Path localPath = mock(Path.class);
        SpaceFrame deck = mock(SpaceFrame.class);
        Vec3 worldItem = new Vec3(4.2, -51.5, 9.1);
        BlockPos localEnd = new BlockPos(20_481_028, 128, 20_485_128);
        // Real Minecraft entities always have a UUID; Mockito defaults to null.
        when(item.getUUID()).thenReturn(java.util.UUID.randomUUID());
        when(item.isAlive()).thenReturn(true);
        when(item.position()).thenReturn(worldItem);
        when(mob.position()).thenReturn(new Vec3(8, -54, 11));
        when(mob.getAttributeValue(Attributes.FOLLOW_RANGE)).thenReturn(16.0);
        when(localPath.getEndNode())
                .thenReturn(new net.minecraft.world.level.pathfinder.Node(
                        localEnd.getX(), localEnd.getY(), localEnd.getZ()));
        when(nativeNavigation.createPath(item, 1)).thenReturn(localPath);
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.trackedFrame(item)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.worldCenter(level, localEnd)).thenReturn(worldItem);
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            assertSame(localPath, navigation.createPath(item, 1));
            verify(nativeNavigation).createPath(item, 1);
            geometry.verifyNoInteractions();
        }
    }

    @Test
    void rejectsNativeSablePathsWhoseEndpointIsNotNearTheItem() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        Entity item = mock(Entity.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        Path localPath = mock(Path.class);
        SpaceFrame deck = mock(SpaceFrame.class);
        Vec3 worldItem = new Vec3(4.2, -51.5, 9.1);
        BlockPos localEnd = new BlockPos(20_481_028, 128, 20_485_128);
        // Real Minecraft entities always have a UUID; Mockito defaults to null.
        when(item.getUUID()).thenReturn(java.util.UUID.randomUUID());
        when(item.isAlive()).thenReturn(true);
        when(item.position()).thenReturn(worldItem);
        when(mob.position()).thenReturn(new Vec3(8, -54, 11));
        when(mob.getAttributeValue(Attributes.FOLLOW_RANGE)).thenReturn(0.1);
        when(localPath.getEndNode())
                .thenReturn(new net.minecraft.world.level.pathfinder.Node(
                        localEnd.getX(), localEnd.getY(), localEnd.getZ()));
        when(nativeNavigation.createPath(item, 1)).thenReturn(localPath);
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.trackedFrame(item)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.worldCenter(level, localEnd)).thenReturn(new Vec3(30, -54, 11));
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            assertNull(navigation.createPath(item, 1));
            verify(nativeNavigation).createPath(item, 1);
        }
    }

    @Test
    void repeatedSableItemApproachKeepsAnActiveNativeRoute() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        Entity item = mock(Entity.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        Path route = mock(Path.class);
        SpaceFrame deck = mock(SpaceFrame.class);
        Vec3 golemPosition = new Vec3(-12.0, -52.5, 14.1);
        Vec3 itemPosition = new Vec3(-13.7, -47.7, 12.1);
        BlockPos plotEnd = new BlockPos(20_481_029, 129, 20_489_221);
        when(mob.position()).thenReturn(golemPosition);
        when(item.position()).thenReturn(itemPosition);
        // Real Minecraft entities always have a UUID; Mockito defaults to null.
        when(item.getUUID()).thenReturn(java.util.UUID.randomUUID());
        when(item.isAlive()).thenReturn(true);
        when(nativeNavigation.createPath(item, 1)).thenReturn(route);
        when(nativeNavigation.moveTo(route, 1.0)).thenReturn(true);
        when(route.getEndNode())
                .thenReturn(
                        new net.minecraft.world.level.pathfinder.Node(plotEnd.getX(), plotEnd.getY(), plotEnd.getZ()));
        when(deck.toLocal(any())).thenAnswer(call -> call.getArgument(0));
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.trackedFrame(item)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.worldCenter(level, plotEnd)).thenReturn(itemPosition);
            GolemSpaceNavigation nav = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            assertTrue(nav.moveTo(item, 1.0));
            for (int poll = 0; poll < 12; poll++) {
                assertTrue(nav.moveTo(item, 1.0));
            }
            // The old implementation created a fresh native Sable path on
            // every five-tick TaskGoal poll, preventing waypoint advancement.
            verify(nativeNavigation, times(1)).createPath(item, 1);
            verify(nativeNavigation, times(1)).moveTo(route, 1.0);
        }
    }

    @Test
    void stationaryNativeRouteFallsBackAfterBoundedTimeout() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        Entity item = mock(Entity.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        Path route = mock(Path.class);
        MoveControl control = mock(MoveControl.class);
        SpaceFrame deck = mock(SpaceFrame.class);
        var tick = new java.util.concurrent.atomic.AtomicLong(100);
        Vec3 stationary = new Vec3(-12.0, -52.5, 14.1);
        var position = new java.util.concurrent.atomic.AtomicReference<>(stationary);
        Vec3 goal = new Vec3(-13.7, -47.7, 12.1);
        BlockPos plotEnd = new BlockPos(20_481_029, 129, 20_489_221);
        when(level.getGameTime()).thenAnswer(call -> tick.get());
        when(mob.position()).thenAnswer(call -> position.get());
        when(mob.onGround()).thenAnswer(call -> position.get().equals(stationary));
        when(mob.getX()).thenReturn(stationary.x);
        when(mob.getY()).thenReturn(stationary.y);
        when(mob.getZ()).thenReturn(stationary.z);
        when(mob.getMoveControl()).thenReturn(control);
        when(mob.getAttributeValue(Attributes.FOLLOW_RANGE)).thenReturn(16.0);
        when(mob.getUUID()).thenReturn(java.util.UUID.randomUUID());
        when(item.getUUID()).thenReturn(java.util.UUID.randomUUID());
        when(item.position()).thenReturn(goal);
        when(item.isAlive()).thenReturn(true);
        when(nativeNavigation.createPath(item, 1)).thenReturn(route);
        when(nativeNavigation.moveTo(route, 1.0)).thenReturn(true);
        when(nativeNavigation.getPath()).thenReturn(route);
        when(route.getEndNode())
                .thenReturn(
                        new net.minecraft.world.level.pathfinder.Node(plotEnd.getX(), plotEnd.getY(), plotEnd.getZ()));
        // A tilt turns purely vertical world jumps into apparent local X/Z
        // movement. That must not reset the stuck timer while airborne.
        when(deck.toLocal(any())).thenAnswer(call -> {
            Vec3 v = call.getArgument(0);
            return new Vec3(v.x, v.y, v.z + (v.y - stationary.y) * 0.7);
        });
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.trackedFrame(item)).thenReturn(deck);
            spaces.when(() -> LevelSpaces.worldCenter(level, plotEnd)).thenReturn(goal);
            GolemSpaceNavigation nav = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            assertTrue(nav.moveTo(item, 1.0));
            for (int i = 0; i < 48; i++) {
                position.set(i % 10 == 0 ? stationary.add(0, 0.55, 0) : stationary);
                tick.incrementAndGet();
                nav.tick();
            }
            // After the timeout, the native route must be stopped rather than
            // endlessly reapplied to the same collision point.
            verify(nativeNavigation, times(1)).stop();
            // The fallback starts with a collision-aware surface query. Our
            // geometry mock has no surface, so no fallback route is expected.
            assertTrue(!nav.moveTo(item, 1.0));
            verify(nativeNavigation, times(1)).createPath(item, 1);
            geometry.verify(() ->
                    GolemPathGeometry.surface(eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()));
        }
    }

    @Test
    void entityTargetsRetainTheirSableFrameInsteadOfRediscoveringItFromWorldCoordinates() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        Entity item = mock(Entity.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        SpaceFrame deck = mock(SpaceFrame.class);
        // The tracked Sable frame must have usable coordinate transforms.
        // A bare Mockito SpaceFrame returns null for toLocal()/toWorld().
        when(deck.toLocal(any())).thenAnswer(call -> call.getArgument(0));
        when(deck.toWorld(any())).thenAnswer(call -> call.getArgument(0));
        Vec3 itemWorld = new Vec3(2.5, 1.0, 0.5);
        BlockPos itemWorldBlock = BlockPos.containing(itemWorld);
        when(mob.position()).thenReturn(new Vec3(0.5, 1.0, 0.5));
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.6F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        when(mob.getAttributeValue(Attributes.FOLLOW_RANGE)).thenReturn(16.0);
        // Real Minecraft entities always have a UUID; Mockito defaults to null.
        when(item.getUUID()).thenReturn(java.util.UUID.randomUUID());
        when(item.isAlive()).thenReturn(true);
        when(item.position()).thenReturn(itemWorld);
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.trackedFrame(item)).thenReturn(deck);
            // Asking which frame contains the *rounded world block* would
            // incorrectly report the ordinary world for this Sable item.
            spaces.when(() -> LevelSpaces.frameOf(level, itemWorldBlock)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, deck));
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> {
                        double x = call.getArgument(2), z = call.getArgument(3);
                        if (x < 0 || x > 4 || Math.abs(z - 0.5) > 0.1) return null;
                        return new GolemPathGeometry.Surface(WorldFrame.INSTANCE, new Vec3(x, 1.0, z));
                    });
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            Path route = navigation.createPath(item, 1);
            assertNotNull(route);
            assertTrue(GolemSpaceNavigation.pathEndWorld(level, route).distanceToSqr(itemWorld) <= 3.5);
            verify(nativeNavigation, never()).createPath(eq(item), eq(1));
            spaces.verify(() -> LevelSpaces.trackedFrame(item));
            spaces.verify(() -> LevelSpaces.frameOf(level, itemWorldBlock), never());
        }
    }

    @Test
    void findsRouteUpRealFullBlockStairs() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        for (int x = -2; x <= 7; x++)
            for (int z = -2; z <= 2; z++) {
                blocks.put(new BlockPos(x, 0, z), Blocks.STONE.defaultBlockState());
            }
        for (int x = 2; x <= 5; x++)
            for (int y = 1; y <= Math.min(x - 1, 3); y++) {
                blocks.put(new BlockPos(x, y, 0), Blocks.STONE.defaultBlockState());
            }
        when(level.isLoaded(any())).thenReturn(true);
        when(level.getBlockState(any()))
                .thenAnswer(call -> blocks.getOrDefault(call.getArgument(0), Blocks.AIR.defaultBlockState()));
        when(mob.position()).thenReturn(new Vec3(0.5, 1, 0.5));
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.6F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.frameOf(eq(level), any(BlockPos.class)))
                    .thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(eq(level), any(BlockPos.class)))
                    .thenAnswer(call -> Vec3.atCenterOf(call.getArgument(1)));
            // A nearby Sable deck forces collision-aware A*, but the deck is
            // outside the staircase itself. Geometry's narrow ray/body queries
            // should only see the world frame, while the broad route corridor
            // must still notice the deck and select the custom navigator.
            SpaceFrame nearbyDeck = mock(SpaceFrame.class);
            when(nearbyDeck.toLocal(any(Vec3.class))).thenAnswer(call -> ((Vec3) call.getArgument(0)).add(0, 0, -8));
            when(nearbyDeck.toWorld(any(Vec3.class))).thenAnswer(call -> ((Vec3) call.getArgument(0)).add(0, 0, 8));
            when(nearbyDeck.directionToLocal(any(Vec3.class))).thenAnswer(call -> call.getArgument(0));
            when(nearbyDeck.directionToWorld(any(Vec3.class))).thenAnswer(call -> call.getArgument(0));
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any())).thenAnswer(call -> {
                net.minecraft.world.phys.AABB area = call.getArgument(1);
                // The broad mob-to-target corridor includes the foreign
                // deck, but short collision/footing probes do not.
                return area.getXsize() > 6.0 && area.getZsize() > 3.0
                        ? List.of(WorldFrame.INSTANCE, nearbyDeck)
                        : List.of(WorldFrame.INSTANCE);
            });
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            assertNotNull(navigation.createPath(Set.of(new BlockPos(5, 4, 0)), 0, false, 1, 16));
        }
    }

    @Test
    void walksFromWorldAcrossDeckAndBackOntoWorld() {
        runScenario(false, false);
    }

    @Test
    void movingTheDeckAwayStopsTheRoute() {
        runScenario(true, false);
    }

    @Test
    void anUnsupportedGapHasNoRoute() {
        runScenario(false, true);
    }

    @Test
    void keepsAnExistingRouteWhenRepathingDuringAJump() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        var airborne = new java.util.concurrent.atomic.AtomicBoolean();
        when(mob.position()).thenReturn(new Vec3(0.5, 1, 0.5));
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(0.9F);
        when(mob.maxUpStep()).thenReturn(0.6F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        when(mob.getMoveControl()).thenReturn(mock(MoveControl.class));
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.frameOf(eq(level), any(BlockPos.class)))
                    .thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(eq(level), any(BlockPos.class)))
                    .thenAnswer(call -> Vec3.atCenterOf(call.getArgument(1)));
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, mock(SpaceFrame.class)));
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> airborne.get()
                            ? null
                            : new GolemPathGeometry.Surface(
                                    WorldFrame.INSTANCE, new Vec3(call.getArgument(2), 1, call.getArgument(3))));
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            var target = new BlockPos(3, 1, 0);
            var route = navigation.createPath(Set.of(target), 0, false, 1, 16);
            assertNotNull(route);
            assertTrue(navigation.moveTo(route, 1));
            airborne.set(true);
            for (int tick = 0; tick < 11; tick++) navigation.tick();
            assertSame(route, navigation.createPath(Set.of(target), 0, false, 1, 16));
        }
    }

    @Test
    void reachesAnItemOnARaisedDeckFromTheAdjacentFloor() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        when(mob.position()).thenReturn(new Vec3(0.5, 0, 0.5));
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.5F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.frameOf(eq(level), any(BlockPos.class)))
                    .thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(eq(level), any(BlockPos.class)))
                    .thenAnswer(call -> Vec3.atCenterOf(call.getArgument(1)));
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, mock(SpaceFrame.class)));
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> {
                        double x = call.getArgument(2), z = call.getArgument(3);
                        if (x >= 2 || x < 0 || Math.abs(z - 0.5) > 0.1) return null;
                        return new GolemPathGeometry.Surface(WorldFrame.INSTANCE, new Vec3(x, 0, z));
                    });
            GolemSpaceNavigation navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            BlockPos target = new BlockPos(2, 0, 0);
            assertNull(navigation.createPath(Set.of(target), 0, false, 0, 16));
            assertNotNull(navigation.createPath(Set.of(target), 0, false, 1, 16));
        }
    }

    @Test
    void climbsFullBlockStepsToAnUpperDeck() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        when(mob.position()).thenReturn(new Vec3(0.5, 1, 0.5));
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.6F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.frameOf(eq(level), any(BlockPos.class)))
                    .thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(eq(level), any(BlockPos.class)))
                    .thenAnswer(call -> Vec3.atCenterOf(call.getArgument(1)));
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, mock(SpaceFrame.class)));
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> {
                        double x = call.getArgument(2), z = call.getArgument(3);
                        double min = call.getArgument(4), max = call.getArgument(5);
                        if (x < 0 || x > 5 || Math.abs(z - 0.5) > 0.1) return null;
                        double y = 1 + Math.min(3, Math.max(0, Math.floor(x)));
                        if (y < min || y > max) return null;
                        return new GolemPathGeometry.Surface(WorldFrame.INSTANCE, new Vec3(x, y, z));
                    });
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            assertNotNull(navigation.createPath(Set.of(new BlockPos(4, 4, 0)), 0, false, 1, 16));
        }
    }

    @Test
    void walksLongUnobstructedWaypointsWithoutHopping() {
        assertTraversalJumpBehavior(false, false);
    }

    @Test
    void jumpsForwardToClimbAOneBlockLedge() {
        assertTraversalJumpBehavior(true, false);
    }

    @Test
    void jumpsAcrossAnOtherwiseUnsupportedEdge() {
        assertTraversalJumpBehavior(false, true);
    }

    private static void assertTraversalJumpBehavior(boolean ledge, boolean gap) {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        MoveControl moveControl = mock(MoveControl.class);
        JumpControl jumpControl = mock(JumpControl.class);
        var position = new java.util.concurrent.atomic.AtomicReference<>(new Vec3(0.5, 1.0, 0.5));
        when(mob.position()).thenAnswer(call -> position.get());
        when(mob.getX()).thenAnswer(call -> position.get().x);
        when(mob.getY()).thenAnswer(call -> position.get().y);
        when(mob.getZ()).thenAnswer(call -> position.get().z);
        when(mob.onGround()).thenReturn(true);
        when(mob.getMoveControl()).thenReturn(moveControl);
        when(mob.getJumpControl()).thenReturn(jumpControl);
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.6F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.frameOf(eq(level), any(BlockPos.class)))
                    .thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(eq(level), any(BlockPos.class)))
                    .thenAnswer(call -> Vec3.atCenterOf(call.getArgument(1)));
            // A nearby sublevel forces use of the custom navigator rather than vanilla navigation.
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, mock(SpaceFrame.class)));
            geometry.when(() -> GolemPathGeometry.body(eq(mob), any())).thenCallRealMethod();
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> {
                        double x = call.getArgument(2), z = call.getArgument(3);
                        double minY = call.getArgument(4), maxY = call.getArgument(5);
                        if (x < 0 || x > 5 || Math.abs(z - 0.5) > 0.1) return null;
                        if (gap && x > 0.5 && x < 1.5) return null;
                        double y = ledge && x >= 1 ? 2 : 1;
                        if (y < minY || y > maxY) return null;
                        return new GolemPathGeometry.Surface(WorldFrame.INSTANCE, new Vec3(x, y, z));
                    });
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            BlockPos target = new BlockPos(ledge || gap ? 2 : 4, ledge ? 2 : 1, 0);
            Path route = navigation.createPath(Set.of(target), 0, false, 1, 16);
            assertNotNull(route);
            assertTrue(navigation.moveTo(route, 1));
            navigation.tick(); // Consume the starting waypoint and select the first edge.
            if (ledge || gap) {
                verify(jumpControl).jump();
            } else {
                verify(jumpControl, never()).jump();
            }
            // A moving entity must not discard a valid planned jump just because it is
            // now closer than the original minimum horizontal jump distance.
            if (gap) {
                position.set(new Vec3(0.9, 1.0, 0.5));
                for (int tick = 0; tick < 6; tick++) navigation.tick();
                assertSame(route, navigation.getPath());
            }
        }
    }

    @Test
    void continuouslyRisingDeckDoesNotTriggerAnUnmarkedJump() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        JumpControl jumpControl = mock(JumpControl.class);
        when(mob.position()).thenReturn(new Vec3(0.5, 1.0, 0.5));
        when(mob.getY()).thenReturn(1.0);
        when(mob.onGround()).thenReturn(true);
        when(mob.getJumpControl()).thenReturn(jumpControl);
        when(mob.getMoveControl()).thenReturn(mock(MoveControl.class));
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.6F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        SpaceFrame tilted = mock(SpaceFrame.class);
        when(tilted.directionToWorld(any())).thenReturn(new Vec3(0.0, 0.7, 0.7));
        // SearchJob snapshots the golem position in the tracked frame.
        when(tilted.toLocal(any())).thenAnswer(call -> call.getArgument(0));
        when(tilted.toWorld(any())).thenAnswer(call -> call.getArgument(0));
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(tilted);
            spaces.when(() -> LevelSpaces.frameOf(eq(level), any(BlockPos.class)))
                    .thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(eq(level), any(BlockPos.class)))
                    .thenAnswer(call -> Vec3.atCenterOf(call.getArgument(1)));
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, tilted));
            geometry.when(() -> GolemPathGeometry.body(eq(mob), any())).thenCallRealMethod();
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> {
                        double x = call.getArgument(2), z = call.getArgument(3);
                        double minY = call.getArgument(4), maxY = call.getArgument(5);
                        if (x < 0.5 || x > 4 || Math.abs(z - 0.5) > 0.01) return null;
                        // A continuous 0.9-block rise per horizontal block. It is
                        // walkable in many short steps; no single support has a ledge.
                        double y = 1.0 + 0.9 * (x - 0.5);
                        if (y < minY || y > maxY) return null;
                        return new GolemPathGeometry.Surface(WorldFrame.INSTANCE, new Vec3(x, y, z));
                    });
            GolemSpaceNavigation navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            Path route = navigation.createPath(Set.of(new BlockPos(3, 3, 0)), 0, false, 1, 16);
            assertNotNull(route);
            assertTrue(navigation.moveTo(route, 1.0));
            navigation.tick();
            verify(jumpControl, never()).jump();
        }
    }

    private static void runScenario(boolean moveDeck, boolean missingDeck) {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        MoveControl control = mock(MoveControl.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        SpaceFrame deck = mock(SpaceFrame.class);
        State state = new State();
        state.present = !missingDeck;
        when(mob.position()).thenAnswer(call -> state.position);
        when(mob.getX()).thenAnswer(call -> state.position.x);
        when(mob.getY()).thenAnswer(call -> state.position.y);
        when(mob.getZ()).thenAnswer(call -> state.position.z);
        when(mob.getAttributeValue(Attributes.FOLLOW_RANGE)).thenReturn(16.0);
        when(mob.getMoveControl()).thenReturn(control);
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.5F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        when(deck.toWorld(any())).thenAnswer(call -> ((Vec3) call.getArgument(0)).add(state.shift, 0, 0));
        when(deck.toLocal(any())).thenAnswer(call -> ((Vec3) call.getArgument(0)).add(-state.shift, 0, 0));
        doAnswer(call -> {
                    if (call.getArgument(3, Double.class) > 0) {
                        state.position = new Vec3(call.getArgument(0), call.getArgument(1), call.getArgument(2));
                        state.crossedDeck |= state.position.x >= 1.5 && state.position.x <= 2.5;
                    }
                    return null;
                })
                .when(control)
                .setWantedPosition(anyDouble(), anyDouble(), anyDouble(), anyDouble());

        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.frameOf(eq(level), any(BlockPos.class)))
                    .thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(eq(level), any(BlockPos.class)))
                    .thenAnswer(call -> Vec3.atCenterOf(call.getArgument(1)));
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, deck));
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> {
                        double x = call.getArgument(2), z = call.getArgument(3);
                        double minY = call.getArgument(4), maxY = call.getArgument(5);
                        if (x < 0 || x > 5 || Math.abs(z - 0.5) > 0.1 || minY > 1 || maxY < 1) return null;
                        boolean onDeck = x >= 1.5 && x <= 2.5;
                        if (onDeck && !state.present) return null;
                        Vec3 feet = new Vec3(x, 1, z);
                        return new GolemPathGeometry.Surface(
                                onDeck ? deck : WorldFrame.INSTANCE, onDeck ? deck.toLocal(feet) : feet);
                    });
            GolemSpaceNavigation navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            var route = navigation.createPath(Set.of(new BlockPos(4, 1, 0)), 0, false, 0, 16);
            if (missingDeck) {
                assertNull(route);
                return;
            }
            assertNotNull(route);
            assertTrue(navigation.moveTo(route, 1));
            if (moveDeck) {
                state.shift = 10;
                state.present = false;
                navigation.tick();
                assertTrue(navigation.isDone());
                return;
            }
            for (int tick = 0; tick < 30 && !navigation.isDone(); tick++) navigation.tick();
            assertTrue(navigation.isDone());
            assertTrue(state.crossedDeck);
            assertTrue(state.position.x > 2.5);
        }
    }

    private static final class State {
        Vec3 position = new Vec3(0.5, 1, 0.5);
        boolean present;
        boolean crossedDeck;
        double shift;
    }

    @Test
    void serverCrossFrameSearchYieldsInsteadOfBlockingTheCaller() {
        net.minecraft.server.level.ServerLevel level = mock(net.minecraft.server.level.ServerLevel.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        var now = new java.util.concurrent.atomic.AtomicLong(100);
        var golemId = java.util.UUID.randomUUID();
        when(level.getGameTime()).thenAnswer(call -> now.get());
        when(mob.getUUID()).thenReturn(golemId);
        when(mob.position()).thenReturn(new Vec3(0.5, 1, 0.5));
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.6F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        BlockPos target = new BlockPos(3, 1, 0);
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.frameOf(eq(level), any(BlockPos.class)))
                    .thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(eq(level), any(BlockPos.class)))
                    .thenAnswer(call -> Vec3.atCenterOf(call.getArgument(1)));
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, mock(SpaceFrame.class)));
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> {
                        double x = call.getArgument(2), z = call.getArgument(3);
                        double minY = call.getArgument(4), maxY = call.getArgument(5);
                        if (x < 0 || x > 4 || Math.abs(z - 0.5) > 0.1 || minY > 1 || maxY < 1) return null;
                        return new GolemPathGeometry.Surface(WorldFrame.INSTANCE, new Vec3(x, 1, z));
                    });
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            // The expensive request must yield immediately, not run A* in createPath().
            assertNull(navigation.createPath(Set.of(target), 0, false, 1, 16));
            assertTrue(GolemPathSearchBudget.wasDeferred(level, golemId));
            Path route = null;
            for (int i = 0; i < 100 && route == null; i++) {
                now.incrementAndGet();
                navigation.tick();
                route = navigation.createPath(Set.of(target), 0, false, 1, 16);
            }
            assertNotNull(route);
        }
    }

    @Test
    void acceptedDiagonalEdgesRemainValidWhileFollowingTheRoute() {
        Level level = mock(Level.class);
        Mob mob = mock(Mob.class);
        PathNavigation nativeNavigation = mock(PathNavigation.class);
        when(mob.position()).thenReturn(new Vec3(0.5, 1, 0.5));
        when(mob.getX()).thenReturn(0.5);
        when(mob.getY()).thenReturn(1.0);
        when(mob.getZ()).thenReturn(0.5);
        when(mob.onGround()).thenReturn(true);
        when(mob.getMoveControl()).thenReturn(mock(MoveControl.class));
        when(mob.getBbWidth()).thenReturn(0.4F);
        when(mob.getBbHeight()).thenReturn(1.0F);
        when(mob.maxUpStep()).thenReturn(0.6F);
        when(mob.getMaxFallDistance()).thenReturn(3);
        try (var spaces = mockStatic(LevelSpaces.class);
                var geometry = mockStatic(GolemPathGeometry.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(mob)).thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.frameOf(eq(level), any(BlockPos.class)))
                    .thenReturn(WorldFrame.INSTANCE);
            spaces.when(() -> LevelSpaces.worldCenter(eq(level), any(BlockPos.class)))
                    .thenAnswer(call -> Vec3.atCenterOf(call.getArgument(1)));
            spaces.when(() -> LevelSpaces.framesTouching(eq(level), any()))
                    .thenReturn(List.of(WorldFrame.INSTANCE, mock(SpaceFrame.class)));
            geometry.when(() -> GolemPathGeometry.clear(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.safeJumpLanding(eq(level), eq(mob), any()))
                    .thenReturn(true);
            geometry.when(() -> GolemPathGeometry.surface(
                            eq(level), eq(mob), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                    .thenAnswer(call -> {
                        double x = call.getArgument(2), z = call.getArgument(3);
                        double minY = call.getArgument(4), maxY = call.getArgument(5);
                        if (x < 0 || x > 3 || z < 0 || z > 3 || minY > 1 || maxY < 1) return null;
                        return new GolemPathGeometry.Surface(WorldFrame.INSTANCE, new Vec3(x, 1, z));
                    });
            var navigation = new GolemSpaceNavigation(mob, level, nativeNavigation, false);
            Path route = navigation.createPath(Set.of(new BlockPos(2, 1, 2)), 0, false, 1, 16);
            assertNotNull(route);
            assertTrue(navigation.moveTo(route, 1));
            navigation.tick();
            assertSame(route, navigation.getPath());
        }
    }
}
