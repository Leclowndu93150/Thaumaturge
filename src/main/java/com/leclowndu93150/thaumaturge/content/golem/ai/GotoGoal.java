package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class GotoGoal extends Goal {
    protected static final int TASK_TIMEOUT = 1000;
    private static final int COOLDOWN_TICKS = 5;
    private static final int RAMBLE_INTERVAL = 5;
    private static final int RETRY_PAUSE = 10;
    private static final byte EVENT_EMOTE_FAIL = 6;

    protected final EntityThaumaturgeGolem golem;
    protected int taskCounter = -1;
    protected int cooldown;
    protected double minDist = 4.0;
    protected BlockPos targetBlock;
    private BlockPos prevRamble;
    private int pause;

    protected GotoGoal(EntityThaumaturgeGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

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
        cooldown = COOLDOWN_TICKS;
        if (golem.getTask() != null && !golem.getTask().isSuspended()) {
            return false;
        }
        targetBlock = null;
        boolean start = findDestination();
        if (start && golem.getTask() != null && golem.getTask().getSealPos() != null) {
            ISealEntity seal = GolemHelper.getSealEntity(golem.level(), golem.getTask().getSealPos());
            if (seal != null) {
                seal.getSeal().onTaskStarted(golem.level(), golem, golem.getTask());
            }
        }
        return start;
    }

    @Override
    public void start() {
        moveTo();
        taskCounter = 0;
    }

    protected abstract void moveTo();

    protected abstract boolean findDestination();

    @Override
    public boolean canContinueToUse() {
        return taskCounter >= 0 && taskCounter <= TASK_TIMEOUT && golem.getTask() != null && !golem.getTask().isSuspended() && isValidDestination(golem.level(), golem.getTask().getPos());
    }

    @Override
    public void tick() {
        Task task = golem.getTask();
        if (task == null || pause-- > 0) {
            return;
        }
        double dist = task.getType() == Task.TYPE_BLOCK ? golem.distanceToSqr(Vec3.atCenterOf(targetBlock == null ? task.getPos() : targetBlock)) : golem.distanceToSqr(task.getEntity());
        if (dist > minDist) {
            task.setCompletion(false);
            taskCounter++;
            if (taskCounter % RAMBLE_INTERVAL == 0) {
                if (prevRamble != null && prevRamble.equals(golem.blockPosition())) {
                    Vec3 ramble = DefaultRandomPos.getPosTowards(golem, 6, 4, Vec3.atLowerCornerOf(task.getPos()), Math.PI / 2.0);
                    if (ramble != null) {
                        golem.getNavigation().moveTo(ramble.x + 0.5, ramble.y + 0.5, ramble.z + 0.5, golem.getGolemMoveSpeed());
                    }
                } else {
                    moveTo();
                }
                prevRamble = golem.blockPosition();
            }
        } else {
            TaskHandler.completeTask(task, golem);
            if (golem.getTask() != null && golem.getTask().isCompleted()) {
                if (taskCounter >= 0) {
                    taskCounter = 0;
                }
                pause = 0;
            } else {
                pause = RETRY_PAUSE;
                taskCounter++;
            }
            taskCounter--;
        }
    }

    @Override
    public void stop() {
        Task task = golem.getTask();
        if (task == null) {
            return;
        }
        if (!task.isCompleted() && task.isReserved() && ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get()) {
            golem.level().broadcastEntityEvent(golem, EVENT_EMOTE_FAIL);
        }
        if (task.isCompleted() && !task.isSuspended()) {
            task.setSuspended(true);
        }
        task.setReserved(false);
    }

    protected boolean isValidDestination(Level level, BlockPos pos) {
        return true;
    }

    protected boolean areGolemTagsValidForTask(Task task) {
        ISealEntity seal = GolemHelper.getSealEntity(golem.level(), task.getSealPos());
        if (seal == null || seal.getSeal() == null) {
            return true;
        }
        if (seal.isLocked() && (golem.getOwnerReference() == null || !golem.getOwnerReference().getUUID().equals(seal.getOwner()))) {
            return false;
        }
        GolemTrait[] required = seal.getSeal().getRequiredTags();
        if (required != null && !golem.getProperties().getTraits().containsAll(List.of(required))) {
            return false;
        }
        GolemTrait[] forbidden = seal.getSeal().getForbiddenTags();
        if (forbidden != null) {
            for (GolemTrait tag : forbidden) {
                if (golem.getProperties().getTraits().contains(tag)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected @Nullable BlockPos targetBlock() {
        return targetBlock;
    }
}
