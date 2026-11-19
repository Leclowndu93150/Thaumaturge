package com.leclowndu93150.thaumaturge.content.golem.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.Test;

class TaskGoalPortTest {
    @Test
    void stoppingReleasesAndClearsTheTaskItStillOwns() {
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        Task task = mock(Task.class);
        when(golem.getNavigation()).thenReturn(mock(net.minecraft.world.entity.ai.navigation.PathNavigation.class));
        when(golem.getTask()).thenReturn(task);

        goal(golem).stop();

        verify(task).release();
        verify(golem).setTask(null);
    }

    @Test
    void stoppingDoesNotClearAReplacementTask() {
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        Task task = mock(Task.class);
        Task replacement = mock(Task.class);
        when(golem.getNavigation()).thenReturn(mock(net.minecraft.world.entity.ai.navigation.PathNavigation.class));
        when(golem.getTask()).thenReturn(task, replacement);

        goal(golem).stop();

        verify(task).release();
        verify(golem, never()).setTask(null);
    }

    @Test
    void returningHomeWaitsAfterACompletedTask() {
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        doCallRealMethod().when(golem).setTask(org.mockito.ArgumentMatchers.nullable(Task.class));
        when(golem.recentlyFinishedTask()).thenCallRealMethod();
        golem.tickCount = 100;
        golem.setTask(mock(Task.class));
        golem.setTask(null);
        assertTrue(golem.recentlyFinishedTask());
        assertFalse(new ReturnHomeGoal(golem).canUse());
        golem.tickCount = 120;
        assertFalse(golem.recentlyFinishedTask());
    }

    private static TaskGoal goal(EntityThaumaturgeGolem golem) {
        return new TaskGoal(golem) {
            @Override
            protected boolean claim(ServerLevel level) {
                return false;
            }

            @Override
            protected void approach(Task task) {}

            @Override
            protected double distanceSqrTo(Task task) {
                return 0;
            }
        };
    }
}
