package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandoff;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class TaskGoal extends Goal {
    private static final int CLAIM_COOLDOWN = 5;
    private static final int GIVE_UP_AFTER = 1000;
    private static final int REPATH_EVERY = 5;
    private static final int RETRY_DELAY = 10;
    private static final int DETOUR_RANGE = 6;
    private static final int DETOUR_HEIGHT = 4;
    private static final double DETOUR_OFFSET = 0.5;
    private static final byte GIVE_UP_EMOTE = 6;
    private static final double BLOCK_REACH_SQR = 4.0;

    protected final EntityThaumaturgeGolem golem;
    protected double reachSqr = BLOCK_REACH_SQR;
    private int steps = -1;
    private int cooldown;
    private int pause;
    private @Nullable BlockPos lastFoothold;

    protected TaskGoal(EntityThaumaturgeGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    protected abstract boolean claim(ServerLevel level);

    protected abstract void approach(Task task);

    protected abstract double distanceSqrTo(Task task);

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        cooldown = CLAIM_COOLDOWN;
        Task current = golem.getTask();
        if (current != null && !current.isSuspended() || !(golem.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!claim(level)) {
            return false;
        }
        Task claimed = golem.getTask();
        ISealEntity seal = claimed == null ? null : SealHandler.getSealEntity(level, claimed.origin());
        if (seal != null) {
            seal.behavior().onTaskStarted(level, seal, golem, claimed);
        }
        return true;
    }

    protected boolean mayTake(Task task, BlockPos target) {
        return SealAccess.allows(SealHandler.getSealEntity(golem.level(), task.origin()), golem) && task.canBePerformedBy(golem) && golem.isWithinHome(target);
    }

    protected void take(Task task) {
        TaskHandoff.assign(golem, task);
    }

    @Override
    public void start() {
        Task task = golem.getTask();
        if (task != null) {
            approach(task);
        }
        steps = 0;
    }

    @Override
    public boolean canContinueToUse() {
        Task task = golem.getTask();
        return steps >= 0 && steps <= GIVE_UP_AFTER && task != null && !task.isSuspended();
    }

    @Override
    public void tick() {
        Task task = golem.getTask();
        if (task == null || pause-- > 0 || !(golem.level() instanceof ServerLevel level)) {
            return;
        }
        if (distanceSqrTo(task) > reachSqr) {
            walk(task);
        } else {
            work(level, task);
        }
    }

    private void walk(Task task) {
        task.recordAttempt(false);
        steps++;
        if (steps % REPATH_EVERY != 0) {
            return;
        }
        BlockPos foothold = golem.blockPosition();
        if (foothold.equals(lastFoothold)) {
            detour(task);
        } else {
            approach(task);
        }
        lastFoothold = foothold;
    }

    private void detour(Task task) {
        Vec3 aside = DefaultRandomPos.getPosTowards(golem, DETOUR_RANGE, DETOUR_HEIGHT, Vec3.atLowerCornerOf(task.pos()), Math.PI / 2.0);
        if (aside != null) {
            golem.getNavigation().moveTo(aside.x + DETOUR_OFFSET, aside.y + DETOUR_OFFSET, aside.z + DETOUR_OFFSET, golem.getGolemMoveSpeed());
        }
    }

    private void work(ServerLevel level, Task task) {
        TaskBoard.attempt(level, task, golem);
        Task after = golem.getTask();
        if (after != null && after.isCompleted()) {
            steps = Math.min(steps, 0) - 1;
            pause = 0;
        } else {
            pause = RETRY_DELAY;
        }
    }

    @Override
    public void stop() {
        Task task = golem.getTask();
        if (task == null) {
            return;
        }
        if (!task.isCompleted() && task.isReserved() && ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get()) {
            golem.level().broadcastEntityEvent(golem, GIVE_UP_EMOTE);
        }
        if (task.isCompleted() && !task.isSuspended()) {
            task.suspend();
        }
        task.setReserved(false);
    }
}
