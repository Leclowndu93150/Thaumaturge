package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemEvent;
import com.leclowndu93150.thaumaturge.content.golem.GolemTrace;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandoff;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class TaskGoal extends Goal {
    private static final int CLAIM_COOLDOWN = 5;
    private static final int UNREACHABLE_RETRY_TICKS = 40;
    private static final int MAX_REMEMBERED_FAILURES = 64;
    private static final int GIVE_UP_AFTER = 1000;
    private static final int REPATH_EVERY = 5;
    private static final int RETRY_DELAY = 10;
    private static final int DETOUR_RANGE = 4;
    private static final int DETOUR_HEIGHT = 2;
    private static final double DETOUR_OFFSET = 0.5;
    private static final double BLOCK_REACH_SQR = 4.0;

    protected final EntityThaumaturgeGolem golem;
    protected double reachSqr = BLOCK_REACH_SQR;
    private int steps = -1;
    private int cooldown;
    private int pause;
    private @Nullable BlockPos lastFoothold;
    // A task search must not retry the same expensive failed path every five ticks.
    private final Map<BlockPos, Long> unreachableUntil = new HashMap<>();

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
        if (current != null && !current.isEnded() || !(golem.level() instanceof ServerLevel level)) {
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
        if (!taskAllowed(task)) return false;
        if (!golem.isWithinRestriction(target)) {
            if (GolemTrace.DEBUG_TRACE && GolemTrace.VERBOSE_TASK_TRACE) {
                GolemTrace.log(
                        "[golem-task] tick={} golem={} task={} rejected=restriction target={} home={}",
                        golem.level().getGameTime(),
                        golem.getUUID(),
                        task.id(),
                        target,
                        golem.getRestrictCenter());
            }
            return false;
        }
        return true;
    }

    /** An entity's blockPosition is world-space, not the local block key used by a Sable home. */
    protected boolean mayTakeEntity(Task task, Entity target) {
        if (!taskAllowed(task)) return false;
        if (!golem.hasRestriction()) return true;
        Vec3 homeWorld = LevelSpaces.worldCenter(golem.level(), golem.getRestrictCenter());
        double radius = golem.getRestrictRadius();
        boolean within = homeWorld.distanceToSqr(target.position()) < radius * radius;
        if (!within && GolemTrace.DEBUG_TRACE && GolemTrace.VERBOSE_TASK_TRACE) {
            GolemTrace.log(
                    "[golem-task] tick={} golem={} task={} rejected=entity_restriction "
                            + "target={} worldHome={} radius={}",
                    golem.level().getGameTime(),
                    golem.getUUID(),
                    task.id(),
                    target.position(),
                    homeWorld,
                    radius);
        }
        return within;
    }

    private boolean taskAllowed(Task task) {
        if (!SealAccess.allows(SealHandler.getSealEntity(golem.level(), task.origin()), golem)) {
            if (GolemTrace.DEBUG_TRACE && GolemTrace.VERBOSE_TASK_TRACE) {
                GolemTrace.log(
                        "[golem-task] tick={} golem={} task={} rejected=seal_access",
                        golem.level().getGameTime(),
                        golem.getUUID(),
                        task.id());
            }
            return false;
        }
        if (!task.canBePerformedBy(golem)) {
            if (GolemTrace.DEBUG_TRACE && GolemTrace.VERBOSE_TASK_TRACE) {
                GolemTrace.log(
                        "[golem-task] tick={} golem={} task={} rejected=task_rules_or_capacity",
                        golem.level().getGameTime(),
                        golem.getUUID(),
                        task.id());
            }
            return false;
        }
        return true;
    }

    protected boolean shouldTestPath(BlockPos target) {
        return golem.level().getGameTime() >= unreachableUntil.getOrDefault(target, 0L);
    }

    protected void rememberFailedPath(BlockPos target) {
        rememberFailedPath(target, UNREACHABLE_RETRY_TICKS);
    }

    protected void rememberFailedPath(BlockPos target, int cooldownTicks) {
        if (unreachableUntil.size() >= MAX_REMEMBERED_FAILURES) {
            unreachableUntil.clear();
        }
        unreachableUntil.put(target.immutable(), golem.level().getGameTime() + cooldownTicks);
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
        return steps >= 0 && steps <= GIVE_UP_AFTER && task != null && !task.isEnded();
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
        if (golem.getNavigation() instanceof GolemSpaceNavigation) {
            approach(task);
            return;
        }
        Vec3 aside = DefaultRandomPos.getPosTowards(
                golem, DETOUR_RANGE, DETOUR_HEIGHT, LevelSpaces.worldCenter(golem.level(), task.pos()), Math.PI / 2.0);
        if (aside != null) {
            golem.getNavigation()
                    .moveTo(
                            aside.x + DETOUR_OFFSET,
                            aside.y + DETOUR_OFFSET,
                            aside.z + DETOUR_OFFSET,
                            golem.getGolemMoveSpeed());
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
        golem.getNavigation().stop();
        Task task = golem.getTask();
        if (task == null) {
            return;
        }
        if (!task.isCompleted() && task.isClaimed()) {
            GolemEvent.TASK_FAILED.broadcast(golem);
        }
        if (task.isCompleted() && !task.isEnded()) {
            task.end();
        }
        task.release();
        if (golem.getTask() == task) {
            golem.setTask(null);
        }
    }
}
