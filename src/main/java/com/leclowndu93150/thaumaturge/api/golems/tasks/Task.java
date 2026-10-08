package com.leclowndu93150.thaumaturge.api.golems.tasks;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * One unit of golem work on a level's task board: a block or an entity that a golem walks to so the posting seal can act on it.
 *
 * <p>
 * Tasks are server side only and are never saved. A task lives on the board until it ends or its life counter runs out; the board removes it at its next once-a-second sweep and tells
 * the posting seal.
 *
 * @since 1.0.0
 */
public final class Task {
    private static final int DEFAULT_LIFE = 300;
    private static final int CLAIM_LIFE_BONUS = 120;
    private static final int ATTEMPT_LIFE_BONUS = 1;

    private final @Nullable SealPos origin;
    private final TaskTarget target;
    private int id;
    private @Nullable UUID assignee;
    private byte priority;
    private int life = DEFAULT_LIFE;
    private int sealData;
    private boolean claimed;
    private boolean ended;
    private boolean finished;
    private @Nullable ProvisionRequest provision;

    private Task(@Nullable SealPos origin, TaskTarget target) {
        this.origin = origin;
        this.target = target;
    }

    /**
     * Creates a task aimed at a block.
     *
     * @param origin the posting seal, or {@code null} for free-standing work that any golem may take and that finishes as soon as a golem reaches it
     * @param pos    the block to work
     * @return a new, unposted task
     * @since 1.0.0
     */
    public static Task atBlock(@Nullable SealPos origin, BlockPos pos) {
        return new Task(origin, new BlockTarget(pos.immutable()));
    }

    /**
     * Creates a task aimed at an entity. Its position follows the entity.
     *
     * @param origin the posting seal, or {@code null} for free-standing work
     * @param entity the entity to work
     * @return a new, unposted task
     * @since 1.0.0
     */
    public static Task onEntity(@Nullable SealPos origin, Entity entity) {
        return new Task(origin, new EntityTarget(entity));
    }

    /**
     * @return the posting seal's position, or {@code null} for free-standing work
     * @since 1.0.0
     */
    public @Nullable SealPos origin() {
        return origin;
    }

    /**
     * @return what the task points a golem at
     * @since 1.0.0
     */
    public TaskTarget target() {
        return target;
    }

    /**
     * @return the target block, or the block the target entity currently stands in
     * @since 1.0.0
     */
    public BlockPos pos() {
        return target.pos();
    }

    /**
     * @return the target entity, or {@code null} for a block task
     * @since 1.0.0
     */
    public @Nullable Entity entity() {
        return target instanceof EntityTarget onEntity ? onEntity.entity() : null;
    }

    /**
     * @return whether the task targets an entity
     * @since 1.0.0
     */
    public boolean isEntityTask() {
        return target instanceof EntityTarget;
    }

    /**
     * @return the identifier the board gave the task when it was posted; two tasks are equal exactly when their identifiers are
     * @since 1.0.0
     */
    public int id() {
        return id;
    }

    /**
     * Sets the identifier. Called by the board when the task is posted.
     *
     * @param id the identifier, unique within the board
     * @since 1.0.0
     */
    public void assignId(int id) {
        this.id = id;
    }

    /**
     * @return the only golem allowed to claim the task, or {@code null} when any golem may
     * @since 1.1.0
     */
    public @Nullable UUID assignedGolem() {
        return assignee;
    }

    /**
     * Restricts the task to one golem.
     *
     * @param golem the golem's UUID, or {@code null} to let any golem claim it
     * @since 1.1.0
     */
    public void assignTo(@Nullable UUID golem) {
        this.assignee = golem;
    }

    /**
     * @return the only golem allowed to claim the task, or {@code null} when any golem may
     * @since 1.0.0
     * @deprecated use {@link #assignedGolem()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable UUID claimant() {
        return assignedGolem();
    }

    /**
     * @param claimant the golem's UUID, or {@code null} to let any golem claim it
     * @since 1.0.0
     * @deprecated use {@link #assignTo(UUID)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setClaimant(@Nullable UUID claimant) {
        assignTo(claimant);
    }

    /**
     * @return the priority; each point is worth 256 square blocks of distance when golems rank open tasks
     * @since 1.0.0
     */
    public byte priority() {
        return priority;
    }

    /**
     * @param priority the new priority, normally copied from the posting seal (-5 to 5)
     * @since 1.0.0
     */
    public void setPriority(byte priority) {
        this.priority = priority;
    }

    /**
     * @return the remaining life in board sweeps (one per second); the task is removed at the sweep after this reaches 0
     * @since 1.1.0
     */
    public int life() {
        return life;
    }

    /**
     * @param life the remaining life in board sweeps; a new task starts with 300
     * @since 1.1.0
     */
    public void setLife(int life) {
        this.life = life;
    }

    /**
     * @return the remaining life in board sweeps, capped at {@link Short#MAX_VALUE}
     * @since 1.0.0
     * @deprecated use {@link #life()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public short lifespan() {
        return (short) Math.min(Short.MAX_VALUE, life);
    }

    /**
     * @param lifespan the remaining life in board sweeps
     * @since 1.0.0
     * @deprecated use {@link #setLife(int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setLifespan(short lifespan) {
        setLife(lifespan);
    }

    /**
     * @return a number owned by the posting seal for its own per-task state
     * @since 1.0.0
     */
    public int data() {
        return sealData;
    }

    /**
     * @param data the posting seal's per-task state
     * @since 1.0.0
     */
    public void setData(int data) {
        this.sealData = data;
    }

    /**
     * @return whether a golem has claimed the task
     * @since 1.1.0
     */
    public boolean isClaimed() {
        return claimed;
    }

    /**
     * Marks the task as claimed by a golem and adds 120 sweeps of life, so work a golem is doing does not expire under it.
     *
     * @since 1.1.0
     */
    public void claim() {
        setClaimed(true);
    }

    /**
     * Releases the golem's claim so the task is open again, adding 120 sweeps of life.
     *
     * @since 1.1.0
     */
    public void release() {
        setClaimed(false);
    }

    /**
     * @return whether a golem has claimed the task
     * @since 1.0.0
     * @deprecated use {@link #isClaimed()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public boolean isReserved() {
        return isClaimed();
    }

    /**
     * @param reserved {@code true} to claim, {@code false} to release
     * @since 1.0.0
     * @deprecated use {@link #claim()} or {@link #release()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setReserved(boolean reserved) {
        setClaimed(reserved);
    }

    private void setClaimed(boolean claimed) {
        this.claimed = claimed;
        extendLife(CLAIM_LIFE_BONUS);
    }

    /**
     * @return whether the task has ended (finished or cancelled); an ended task is removed at the next sweep
     * @since 1.1.0
     */
    public boolean isEnded() {
        return ended;
    }

    /**
     * Ends the task and drops its link to any provisioning request. Ending is one-way.
     *
     * @since 1.1.0
     */
    public void end() {
        this.ended = true;
        this.provision = null;
    }

    /**
     * @return whether the task has ended
     * @since 1.0.0
     * @deprecated use {@link #isEnded()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public boolean isSuspended() {
        return isEnded();
    }

    /**
     * Ends the task.
     *
     * @since 1.0.0
     * @deprecated use {@link #end()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void suspend() {
        end();
    }

    /**
     * @return whether the most recent completion attempt finished the work
     * @since 1.0.0
     */
    public boolean isCompleted() {
        return finished;
    }

    /**
     * Records a completion attempt, or a tick spent walking to the task when {@code completed} is {@code false}. Adds one sweep of life.
     *
     * @param completed whether the posting seal finished the work
     * @since 1.0.0
     */
    public void recordAttempt(boolean completed) {
        this.finished = completed;
        extendLife(ATTEMPT_LIFE_BONUS);
    }

    /**
     * @return the provisioning request this task is serving, or {@code null}
     * @since 1.0.0
     */
    public @Nullable ProvisionRequest linkedProvision() {
        return provision;
    }

    /**
     * Sets the task's side of a request link only.
     *
     * @param request the provisioning request this task serves, or {@code null} to unlink
     * @since 1.0.0
     * @deprecated use {@link ProvisionRequest#link(Task)} and {@link ProvisionRequest#unlink()}, which keep both sides of the link in step
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void linkProvision(@Nullable ProvisionRequest request) {
        this.provision = request;
    }

    /**
     * Checks the posting seal's colour and its own rules for this golem. Lock and trait rules are checked separately by the golem before this. A task whose seal no longer exists passes.
     *
     * @param golem the golem asking
     * @return whether the golem may take the task
     * @since 1.0.0
     */
    public boolean canBePerformedBy(IGolemAPI golem) {
        if (origin == null) {
            return true;
        }
        ISealEntity seal = GolemHelper.getSealEntity(golem.asEntity().level(), origin);
        if (seal == null) {
            return true;
        }
        if (golem.color() != 0 && seal.color() != 0 && golem.color() != seal.color()) {
            return false;
        }
        return seal.behavior().canPerform(seal, golem, this);
    }

    private void extendLife(int amount) {
        life = (int) Math.min(Integer.MAX_VALUE, (long) life + amount);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Task other && other.id == id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
