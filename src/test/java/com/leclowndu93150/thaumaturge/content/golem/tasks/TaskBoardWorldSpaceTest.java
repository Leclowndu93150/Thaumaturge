package com.leclowndu93150.thaumaturge.content.golem.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class TaskBoardWorldSpaceTest {
    @Test
    void ranksEntityTasksInWorldSpaceWithoutTransformingThemAgain() {
        Level level = mock(Level.class);
        Entity golem = mock(Entity.class);
        when(golem.level()).thenReturn(level);
        when(golem.position()).thenReturn(Vec3.ZERO);

        ItemEntity near = mock(ItemEntity.class);
        ItemEntity far = mock(ItemEntity.class);
        when(near.isAlive()).thenReturn(true);
        when(far.isAlive()).thenReturn(true);
        when(near.position()).thenReturn(new Vec3(2, 0, 0));
        when(far.position()).thenReturn(new Vec3(12, 0, 0));
        when(near.blockPosition()).thenReturn(new BlockPos(2, 0, 0));
        when(far.blockPosition()).thenReturn(new BlockPos(12, 0, 0));

        TaskBoard board = new TaskBoard();
        Task farTask = Task.onEntity(null, far);
        Task nearTask = Task.onEntity(null, near);
        board.post(farTask);
        board.post(nearTask);
        try (var spaces = mockStatic(LevelSpaces.class)) {
            List<Task> ordered = board.openEntityTasks(null, golem);
            assertEquals(2, ordered.size());
            assertSame(nearTask, ordered.getFirst());
            spaces.verifyNoInteractions();
        }
    }

    @Test
    void ranksBlockTasksUsingTheTransformedBlockCenter() {
        Level level = mock(Level.class);
        Entity golem = mock(Entity.class);
        when(golem.level()).thenReturn(level);
        when(golem.position()).thenReturn(Vec3.ZERO);
        BlockPos worldBlock = new BlockPos(200, 50, 200);
        BlockPos deckLocalBlock = new BlockPos(20_481_028, 128, 20_481_029);
        Task worldTask = Task.atBlock(null, worldBlock);
        Task deckTask = Task.atBlock(null, deckLocalBlock);
        TaskBoard board = new TaskBoard();
        board.post(worldTask);
        board.post(deckTask);
        try (var spaces = mockStatic(LevelSpaces.class)) {
            spaces.when(() -> LevelSpaces.worldCenter(level, worldBlock)).thenReturn(new Vec3(30, 0, 0));
            spaces.when(() -> LevelSpaces.worldCenter(level, deckLocalBlock)).thenReturn(new Vec3(3, 0, 0));
            assertSame(deckTask, board.openBlockTasks(null, golem).getFirst());
        }
    }
}
