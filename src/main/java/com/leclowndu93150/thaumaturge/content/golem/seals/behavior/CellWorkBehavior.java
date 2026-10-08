package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

abstract class CellWorkBehavior implements ISealBehavior {
    private static final int TIDY_PERIOD = 100;
    protected static final int STEP_GRACE = 10;

    private final SealClock clock;
    private final TaskLedger<Long> claimed = new TaskLedger<>();

    protected CellWorkBehavior(int stagger) {
        this.clock = new SealClock(stagger);
    }

    protected abstract boolean isWorkable(Level level, ISealEntity seal, BlockPos pos);

    protected void prepare(ServerLevel level, BlockPos pos, Task task) {}

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.at(TIDY_PERIOD)) {
            claimed.dropFinished(level);
        }
        clock.advance();
        BlockPos cell = SealArea.cell(seal, clock.now());
        if (!claimed.tracks(cell.asLong()) && isWorkable(level, seal, cell)) {
            Task task = Task.atBlock(seal.pos(), cell);
            task.setPriority(seal.priority());
            prepare(level, cell, task);
            TaskBoard.of(level).post(task);
            claimed.record(task, cell.asLong());
        }
    }

    protected boolean stillMine(Level level, ISealEntity seal, Task task) {
        return claimed.has(task) && isWorkable(level, seal, task.pos());
    }

    protected void release(Task task) {
        claimed.forget(task);
    }

    protected static void keepAlive(Task task) {
        task.setLife(Math.max(task.life(), STEP_GRACE));
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        if (stillMine(golem.level(), seal, task)) {
            return true;
        }
        task.end();
        return false;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        claimed.forget(task);
    }
}
