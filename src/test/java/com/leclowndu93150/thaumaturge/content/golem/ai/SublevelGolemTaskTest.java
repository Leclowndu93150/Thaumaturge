package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
        SpaceFrame frame = mock(SpaceFrame.class);
        Vec3 worldItem = new Vec3(1.5, 111, -6.5);
        Vec3 plotItem = new Vec3(20_481_028.5, 129, 20_481_035.5);
        Node tooFar = new Node(20_481_024, 129, 20_481_035);
        Node withinReach = new Node(20_481_028, 129, 20_481_034);
        when(golem.level()).thenReturn(level);
        when(golem.getNavigation()).thenReturn(navigation);
        when(golem.distanceToSqr(item)).thenReturn(16.0);
        when(item.position()).thenReturn(worldItem);
        when(item.blockPosition()).thenReturn(BlockPos.containing(worldItem));
        when(golem.isWithinRestriction(BlockPos.containing(worldItem))).thenReturn(true);
        when(navigation.createPath(item, 1)).thenReturn(path);
        when(path.getEndNode()).thenReturn(tooFar, withinReach);
        when(task.entity()).thenReturn(item);
        when(task.canBePerformedBy(golem)).thenReturn(true);
        when(board.openEntityTasks(null, golem)).thenReturn(List.of(task));
        when(frame.toLocal(worldItem)).thenReturn(plotItem);

        try (var spaces = mockStatic(LevelSpaces.class);
                var boards = mockStatic(TaskBoard.class)) {
            boards.when(() -> TaskBoard.of(level)).thenReturn(board);
            spaces.when(() -> LevelSpaces.frameOf(level, tooFar.asBlockPos())).thenReturn(frame);
            spaces.when(() -> LevelSpaces.frameOf(level, withinReach.asBlockPos()))
                    .thenReturn(frame);
            spaces.when(() -> LevelSpaces.toWorld(level, worldItem)).thenReturn(worldItem);

            EntityTaskGoal goal = new EntityTaskGoal(golem);
            assertFalse(goal.claim(level));
            assertTrue(goal.claim(level));
            verify(task).claim();
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
