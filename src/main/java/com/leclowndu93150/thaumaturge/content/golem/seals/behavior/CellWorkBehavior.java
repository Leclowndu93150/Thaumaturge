package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

abstract class CellWorkBehavior implements ISealBehavior {
    protected static final int STEP_GRACE = 10;

    private static final int PURGE_PERIOD = 100;

    private final SealClock clock;
    private final TaskLedger<BlockPos> claims = new TaskLedger<>();

    CellWorkBehavior(int stagger) {
        this.clock = new SealClock(stagger);
    }

    protected abstract boolean isWorkable(Level level, ISealEntity seal, BlockPos pos);

    protected void prepare(ServerLevel level, BlockPos pos, Task task) {}

    protected boolean stillMine(Level level, ISealEntity seal, Task task) {
        return claims.has(task) && isWorkable(level, seal, task.pos());
    }

    protected void release(Task task) {
        claims.forget(task);
    }

    protected boolean retire(Task task) {
        release(task);
        task.end();
        return true;
    }

    protected static void keepAlive(Task task) {
        if (task.life() < STEP_GRACE) {
            task.setLife(STEP_GRACE);
        }
    }

    boolean holdsClaim(Task task) {
        return claims.has(task);
    }

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        int step = clock.advance();
        if (step % PURGE_PERIOD == 0) {
            claims.dropFinished(level);
        }
        BlockPos cell = SealArea.cell(seal, step);
        if (!level.hasChunkAt(cell) || claims.tracks(cell) || !isWorkable(level, seal, cell)) {
            return;
        }
        Task task = Task.atBlock(seal.pos(), cell);
        task.setPriority(seal.priority());
        prepare(level, cell, task);
        GolemHelper.addGolemTask(level, task);
        claims.record(task, cell.immutable());
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
        release(task);
    }
}
