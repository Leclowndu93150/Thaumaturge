package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GolemSpaceNavigationTest {
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
}
