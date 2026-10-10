package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemEvent;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandoff;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class TaskGoal extends Goal {
    private static final int MAX_STEPS = 1000;
    private static final int CLAIM_COOLDOWN_ASKS = 5;
    private static final int PAUSE_TICKS = 10;
    private static final int PATH_REFRESH_INTERVAL = 5;
    private static final double QUARTER_TURN = Math.PI / 2.0D;
    private static final int UNSTICK_HORIZONTAL = 4;
    private static final int UNSTICK_VERTICAL = 2;

    private enum Phase {
        IDLE, APPROACHING, WORKING, PAUSED, FINISHED
    }

    protected final EntityThaumaturgeGolem golem;
    protected double reachSqr;
    private Phase phase = Phase.IDLE;
    private @Nullable Task current;
    private @Nullable BlockPos lastCheck;
    private int steps;
    private int cooldown;
    private int pausedTicks;

    protected TaskGoal(EntityThaumaturgeGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    protected abstract boolean claim(ServerLevel level);

    protected abstract void approach(Task task);

    protected abstract double distanceSqrTo(Task task);

    boolean adopts(Task task) {
        return false;
    }

    void retarget(Task task) {}

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (!(golem.level() instanceof ServerLevel level)) {
            return false;
        }
        Task held = golem.activeJob();
        if (held != null && !held.isEnded()) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (claim(level)) {
            return true;
        }
        cooldown = CLAIM_COOLDOWN_ASKS;
        return false;
    }

    protected boolean mayTake(Task task, BlockPos where) {
        SealPos origin = task.origin();
        ISealEntity seal = origin == null ? null : GolemHelper.getSealEntity(golem.level(), origin);
        return SealAccess.allows(seal, golem) && task.canBePerformedBy(golem) && golem.isWithinHome(where);
    }

    protected void take(Task task) {
        SealPos origin = task.origin();
        ISealEntity seal = origin == null ? null : GolemHelper.getSealEntity(golem.level(), origin);
        if (seal != null && golem.level() instanceof ServerLevel level) {
            seal.behavior().onTaskStarted(level, seal, golem, task);
        }
        TaskHandoff.assign(golem, task);
        current = task;
        steps = 0;
        phase = Phase.APPROACHING;
    }

    @Override
    public void start() {
        lastCheck = null;
        pausedTicks = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return phase != Phase.FINISHED && steps <= MAX_STEPS && current != null && golem.activeJob() == current && !current.isEnded();
    }

    @Override
    public void tick() {
        Task task = current;
        if (task == null) {
            return;
        }
        switch (phase) {
            case APPROACHING -> approachStep(task);
            case WORKING -> work(task);
            case PAUSED -> pause(task);
            default -> {
            }
        }
    }

    @Override
    public void stop() {
        Task task = current;
        current = null;
        phase = Phase.IDLE;
        if (task == null) {
            return;
        }
        if (!task.isCompleted() && task.isClaimed()) {
            GolemEvent.TASK_FAILED.broadcast(golem);
        }
        if (task.isCompleted() && !task.isEnded()) {
            task.end();
        }
        if (task.isClaimed()) {
            task.release();
        }
        if (golem.activeJob() == task) {
            golem.assignJob(null);
        }
    }

    private void approachStep(Task task) {
        if (distanceSqrTo(task) > reachSqr) {
            walk(task);
        } else {
            phase = Phase.WORKING;
        }
    }

    private void walk(Task task) {
        if (steps++ % PATH_REFRESH_INTERVAL != 0) {
            return;
        }
        BlockPos here = golem.blockPosition();
        boolean stuck = here.equals(lastCheck);
        lastCheck = here;
        if (!stuck) {
            approach(task);
            return;
        }
        Vec3 detour = DefaultRandomPos.getPosTowards(golem, UNSTICK_HORIZONTAL, UNSTICK_VERTICAL, aimPoint(task), QUARTER_TURN);
        if (detour != null) {
            golem.getNavigation().moveTo(detour.x, detour.y, detour.z, golem.travelSpeed());
        }
    }

    private void work(Task task) {
        TaskBoard.attempt((ServerLevel) golem.level(), task, golem);
        if (!task.isCompleted()) {
            phase = Phase.PAUSED;
            pausedTicks = 0;
            return;
        }
        Task next = golem.activeJob();
        if (next != null && next != task && !next.isEnded() && adopts(next)) {
            if (!task.isEnded()) {
                task.end();
            }
            current = next;
            steps = 0;
            lastCheck = null;
            retarget(next);
            phase = Phase.APPROACHING;
        } else {
            phase = Phase.FINISHED;
        }
    }

    private void pause(Task task) {
        if (++pausedTicks >= PAUSE_TICKS) {
            phase = distanceSqrTo(task) > reachSqr ? Phase.APPROACHING : Phase.WORKING;
        }
    }

    private static Vec3 aimPoint(Task task) {
        Entity entity = task.entity();
        return entity == null ? Vec3.atCenterOf(task.pos()) : entity.position();
    }
}
