package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

abstract class HuntBehavior implements ISealBehavior {
    private static final short MARK_LIFESPAN = 10;

    protected abstract boolean isQuarry(ServerLevel level, ISealEntity seal, LivingEntity target);

    protected void onHuntOver() {}

    protected static void mark(ServerLevel level, ISealEntity seal, LivingEntity target) {
        Task task = Task.onEntity(seal.pos(), target);
        task.setPriority(seal.priority());
        task.setLifespan(MARK_LIFESPAN);
        TaskBoard.of(level).post(task);
    }

    @Override
    public void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (task.entity() instanceof LivingEntity target && isQuarry(level, seal, target) && golem.asEntity() instanceof Mob hunter) {
            hunter.setTarget(target);
            golem.addRankXp(1);
        }
        task.suspend();
        onHuntOver();
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        task.suspend();
        onHuntOver();
        return true;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        onHuntOver();
    }

    @Override
    public void onRemoved(ServerLevel level, ISealEntity seal) {
        onHuntOver();
    }
}
