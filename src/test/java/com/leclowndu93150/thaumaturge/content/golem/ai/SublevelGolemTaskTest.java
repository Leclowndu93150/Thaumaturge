package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.BlockGolemFetter;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class SublevelGolemTaskTest {
    @Test
    void pickupComparesAPathEndWithTheItemInTheSameFrame() {
        ServerLevel level = mock(ServerLevel.class);
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        ItemEntity item = mock(ItemEntity.class);
        PathNavigation navigation = mock(PathNavigation.class);
        Path path = mock(Path.class);
        Task task = mock(Task.class);
        TaskBoard board = mock(TaskBoard.class);
        Vec3 worldItem = new Vec3(1.5, 111, -6.5);
        Node tooFar = new Node(20_481_024, 129, 20_481_035);
        Node withinReach = new Node(20_481_028, 129, 20_481_034);
        AtomicLong time = new AtomicLong(0);
        when(level.getGameTime()).thenAnswer(call -> time.get());
        when(golem.level()).thenReturn(level);
        when(golem.getNavigation()).thenReturn(navigation);
        when(golem.distanceToSqr(item)).thenReturn(16.0);
        when(item.position()).thenReturn(worldItem);
        when(item.blockPosition()).thenReturn(BlockPos.containing(worldItem));
        when(golem.isWithinRestriction(BlockPos.containing(worldItem))).thenReturn(true);
        when(navigation.createPath(item, 1)).thenReturn(path);
        when(path.getEndNode()).thenReturn(tooFar, tooFar, withinReach, withinReach);
        when(task.entity()).thenReturn(item);
        when(task.canBePerformedBy(golem)).thenReturn(true);
        when(board.openEntityTasks(null, golem)).thenReturn(List.of(task));

        try (var spaces = mockStatic(LevelSpaces.class);
                var boards = mockStatic(TaskBoard.class)) {
            boards.when(() -> TaskBoard.of(level)).thenReturn(board);
            spaces.when(() -> LevelSpaces.worldCenter(level, tooFar.asBlockPos()))
                    .thenReturn(worldItem.add(4.0, 0, 0));
            spaces.when(() -> LevelSpaces.worldCenter(level, withinReach.asBlockPos()))
                    .thenReturn(worldItem.add(0.5, 0, 0.5));

            EntityTaskGoal goal = new EntityTaskGoal(golem);
            assertFalse(goal.claim(level));
            time.set(40);
            assertTrue(goal.claim(level));
            verify(task).claim();
        }
    }

    @Test
    void pickupPathDirectlyBelowTheItemIsNotReachable() {
        ServerLevel level = mock(ServerLevel.class);
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        ItemEntity item = mock(ItemEntity.class);
        PathNavigation navigation = mock(PathNavigation.class);
        Path path = mock(Path.class);
        Task task = mock(Task.class);
        TaskBoard board = mock(TaskBoard.class);
        Vec3 worldItem = new Vec3(3.5, 10.0, 2.5);
        BlockPos under = new BlockPos(3, 6, 2);
        when(golem.level()).thenReturn(level);
        when(golem.getNavigation()).thenReturn(navigation);
        when(golem.distanceToSqr(item)).thenReturn(25.0);
        when(item.position()).thenReturn(worldItem);
        when(item.blockPosition()).thenReturn(BlockPos.containing(worldItem));
        when(golem.isWithinRestriction(item.blockPosition())).thenReturn(true);
        when(navigation.createPath(item, 1)).thenReturn(path);
        when(path.getEndNode()).thenReturn(new Node(under.getX(), under.getY(), under.getZ()));
        when(task.entity()).thenReturn(item);
        when(task.canBePerformedBy(golem)).thenReturn(true);
        when(board.openEntityTasks(null, golem)).thenReturn(List.of(task));

        try (var spaces = mockStatic(LevelSpaces.class);
                var boards = mockStatic(TaskBoard.class)) {
            boards.when(() -> TaskBoard.of(level)).thenReturn(board);
            spaces.when(() -> LevelSpaces.worldCenter(level, under)).thenReturn(new Vec3(3.5, 6.5, 2.5));
            assertFalse(new EntityTaskGoal(golem).claim(level));
            org.mockito.Mockito.verify(task, org.mockito.Mockito.never()).claim();
        }
    }

    @Test
    void unreachableItemDoesNotStarveAReachableItem() {
        ServerLevel level = mock(ServerLevel.class);
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        ItemEntity blocked = mock(ItemEntity.class);
        ItemEntity reachable = mock(ItemEntity.class);
        PathNavigation navigation = mock(PathNavigation.class);
        Path goodPath = mock(Path.class);
        Task blockedTask = mock(Task.class);
        Task goodTask = mock(Task.class);
        TaskBoard board = mock(TaskBoard.class);
        Vec3 reachablePos = new Vec3(3.5, 1.0, 0.5);
        BlockPos end = new BlockPos(3, 1, 0);
        when(golem.level()).thenReturn(level);
        when(golem.getNavigation()).thenReturn(navigation);
        when(golem.distanceToSqr(blocked)).thenReturn(16.0);
        when(golem.distanceToSqr(reachable)).thenReturn(16.0);
        when(blocked.getUUID()).thenReturn(UUID.randomUUID());
        when(reachable.getUUID()).thenReturn(UUID.randomUUID());
        when(blocked.position()).thenReturn(reachablePos);
        when(reachable.position()).thenReturn(reachablePos);
        when(blockedTask.entity()).thenReturn(blocked);
        when(goodTask.entity()).thenReturn(reachable);
        when(blockedTask.canBePerformedBy(golem)).thenReturn(true);
        when(goodTask.canBePerformedBy(golem)).thenReturn(true);
        when(board.openEntityTasks(null, golem)).thenReturn(List.of(blockedTask, goodTask));
        when(navigation.createPath(reachable, 1)).thenReturn(goodPath);
        when(goodPath.getEndNode()).thenReturn(new Node(end.getX(), end.getY(), end.getZ()));
        try (var spaces = mockStatic(LevelSpaces.class);
                var boards = mockStatic(TaskBoard.class)) {
            boards.when(() -> TaskBoard.of(level)).thenReturn(board);
            spaces.when(() -> LevelSpaces.worldCenter(level, end)).thenReturn(reachablePos);
            assertTrue(new EntityTaskGoal(golem).claim(level));
            verify(navigation).createPath(blocked, 1);
            verify(navigation).createPath(reachable, 1);
            verify(goodTask).claim();
            verify(blockedTask, never()).claim();
        }
    }

    @Test
    void entityRestrictionUsesWorldSpaceNotAnEntitiesRoundedBlock() {
        ServerLevel level = mock(ServerLevel.class);
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        ItemEntity item = mock(ItemEntity.class);
        Task task = mock(Task.class);
        BlockPos localHome = new BlockPos(20_481_028, 128, 20_481_028);
        Vec3 homeInWorld = new Vec3(2.0, 0, 2.0);
        when(golem.level()).thenReturn(level);
        when(golem.hasRestriction()).thenReturn(true);
        when(golem.getRestrictCenter()).thenReturn(localHome);
        when(golem.getRestrictRadius()).thenReturn(32.0F);
        when(item.position()).thenReturn(new Vec3(3, 0, 3));
        when(task.canBePerformedBy(golem)).thenReturn(true);
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.worldCenter(level, localHome)).thenReturn(homeInWorld);
            var goal = new EntityTaskGoal(golem);
            assertTrue(goal.mayTakeEntity(task, item));
        }
    }

    @Test
    void blockTasksSearchForAnApproachRatherThanTheTargetCenter() {
        ServerLevel level = mock(ServerLevel.class);
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        PathNavigation navigation = mock(PathNavigation.class);
        Task task = mock(Task.class);
        TaskBoard board = mock(TaskBoard.class);
        BlockPos target = new BlockPos(4, 1, 0);
        Vec3 center = Vec3.atCenterOf(target);
        when(golem.level()).thenReturn(level);
        when(golem.getNavigation()).thenReturn(navigation);
        when(golem.distanceToSqr(center)).thenReturn(16.0);
        when(golem.isWithinRestriction(target)).thenReturn(true);
        when(task.pos()).thenReturn(target);
        when(task.canBePerformedBy(golem)).thenReturn(true);
        when(board.openBlockTasks(null, golem)).thenReturn(List.of(task));
        try (var spaces = mockStatic(LevelSpaces.class);
                var boards = mockStatic(TaskBoard.class)) {
            boards.when(() -> TaskBoard.of(level)).thenReturn(board);
            spaces.when(() -> LevelSpaces.worldCenter(level, target)).thenReturn(center);
            assertFalse(new BlockTaskGoal(golem).claim(level));
            verify(navigation).createPath(target, 1);
        }
    }

    @Test
    void fetterChecksTheDeckForPowerAcrossSmallContactGaps() {
        ServerLevel level = mock(ServerLevel.class);
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        SpaceFrame frame = mock(SpaceFrame.class);
        Vec3 worldFeet = new Vec3(1.5, 111.0, -6.5);
        BlockPos deck = new BlockPos(20_481_028, 128, 20_481_035);
        var fetter = TTBlocks.GOLEM_FETTER.get().defaultBlockState();
        when(golem.level()).thenReturn(level);
        when(golem.position()).thenReturn(worldFeet);
        when(golem.isFettered()).thenCallRealMethod();
        when(frame.toLocal(worldFeet))
                .thenReturn(
                        new Vec3(deck.getX() + 0.5, 128.97, deck.getZ() + 0.5),
                        new Vec3(deck.getX() + 0.5, 129.03, deck.getZ() + 0.5));
        when(level.getBlockState(deck))
                .thenReturn(
                        fetter.setValue(BlockGolemFetter.POWERED, true),
                        fetter.setValue(BlockGolemFetter.POWERED, true),
                        fetter);

        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.trackedFrame(golem)).thenReturn(frame);

            assertTrue(golem.isFettered());
            assertTrue(golem.isFettered());
            assertFalse(golem.isFettered());
        }
    }
}
