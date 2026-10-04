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
 * A unit of golem work, posted by a seal or by the provisioning system with {@link GolemHelper#addGolemTask}.
 *
 * <p>A task lives on its level's task board until it is suspended or its lifespan runs out. The board sweeps once per second and
 * takes one point of lifespan per sweep. A golem claims a task by reserving it, walks to its target and asks the posting seal to
 * complete it; a seal that needs several steps answers "not done" and the golem retries.
 *
 * <p>Tasks are server-side, live objects. They are not saved: the board starts empty after a restart and seals post fresh work.
 *
 * @since 1.0.0
 */
public final class Task {
    private static final short DEFAULT_LIFESPAN = 300;
    private static final short RESERVATION_GRACE = 120;

    private final @Nullable SealPos origin;
    private final TaskTarget target;
    private int id;
    private @Nullable UUID claimant;
    private byte priority;
    private short lifespan = DEFAULT_LIFESPAN;
    private int data;
    private boolean reserved;
    private boolean suspended;
    private boolean completed;
    private @Nullable ProvisionRequest linkedProvision;

    private Task(@Nullable SealPos origin, TaskTarget target) {
        this.origin = origin;
        this.target = target;
    }

    /**
     * @param origin the seal posting the task, or null for free-standing work
     * @param pos    the block to work on
     * @return a new block task with the default lifespan of 300 sweeps
     */
    public static Task atBlock(@Nullable SealPos origin, BlockPos pos) {
        return new Task(origin, new BlockTarget(pos));
    }

    /**
     * @param origin the seal posting the task, or null for free-standing work
     * @param entity the entity to work on
     * @return a new entity task with the default lifespan of 300 sweeps
     */
    public static Task onEntity(@Nullable SealPos origin, Entity entity) {
        return new Task(origin, new EntityTarget(entity));
    }

    /**
     * @return the seal that posted the task, or null
     */
    public @Nullable SealPos origin() {
        return origin;
    }

    /**
     * @return the target
     */
    public TaskTarget target() {
        return target;
    }

    /**
     * @return the target block; for an entity task, the block the entity stands in now
     */
    public BlockPos pos() {
        return target.pos();
    }

    /**
     * @return the target entity, or null for a block task
     */
    public @Nullable Entity entity() {
        return target instanceof EntityTarget entityTarget ? entityTarget.entity() : null;
    }

    /**
     * @return whether the task targets an entity
     */
    public boolean isEntityTask() {
        return target instanceof EntityTarget;
    }

    /**
     * @return the id the task board assigned, or 0 before the task is posted
     */
    public int id() {
        return id;
    }

    /**
     * Assigns the board id. Called once by the task board when the task is posted.
     *
     * @param id the id
     */
    public void assignId(int id) {
        this.id = id;
    }

    /**
     * @return the golem the task is reserved for, or null when any golem may take it
     */
    public @Nullable UUID claimant() {
        return claimant;
    }

    /**
     * @param claimant the only golem allowed to take the task, or null for any golem
     */
    public void setClaimant(@Nullable UUID claimant) {
        this.claimant = claimant;
    }

    /**
     * @return the priority; each point takes 256 off the squared distance golems use to rank work
     */
    public byte priority() {
        return priority;
    }

    /**
     * @param priority the priority, usually the posting seal's
     */
    public void setPriority(byte priority) {
        this.priority = priority;
    }

    /**
     * @return the remaining lifespan in board sweeps
     */
    public short lifespan() {
        return lifespan;
    }

    /**
     * @param lifespan the remaining lifespan in board sweeps
     */
    public void setLifespan(short lifespan) {
        this.lifespan = lifespan;
    }

    /**
     * @return free-form data owned by the posting seal
     */
    public int data() {
        return data;
    }

    /**
     * @param data free-form data owned by the posting seal
     */
    public void setData(int data) {
        this.data = data;
    }

    /**
     * @return whether a golem has claimed the task
     */
    public boolean isReserved() {
        return reserved;
    }

    /**
     * Claims or releases the task. Every call extends the lifespan by 120 sweeps so claimed work does not expire under a golem.
     *
     * @param reserved whether a golem holds the task
     */
    public void setReserved(boolean reserved) {
        this.reserved = reserved;
        lifespan += RESERVATION_GRACE;
    }

    /**
     * @return whether the task is finished or abandoned; the next sweep removes it
     */
    public boolean isSuspended() {
        return suspended;
    }

    /**
     * Ends the task. Unlinks any provision request; the next sweep removes the task and notifies the posting seal.
     */
    public void suspend() {
        linkProvision(null);
        suspended = true;
    }

    /**
     * @return whether the last completion attempt finished the work
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Records the outcome of a completion attempt and adds one sweep of lifespan.
     *
     * @param finished whether the attempt finished the work
     */
    public void recordAttempt(boolean finished) {
        completed = finished;
        lifespan++;
    }

    /**
     * @return the provision request this task serves, or null
     */
    public @Nullable ProvisionRequest linkedProvision() {
        return linkedProvision;
    }

    /**
     * Links the task to a provision request and extends the request's timeout.
     *
     * @param request the request, or null to unlink
     */
    public void linkProvision(@Nullable ProvisionRequest request) {
        linkedProvision = request;
        if (request != null) {
            request.extendTimeout();
        }
    }

    /**
     * Asks the posting seal whether a golem may do this task. A golem with a colour may only serve seals of the same colour or with
     * no colour. Tasks without a living seal can be done by anyone.
     *
     * @param golem the golem asking
     * @return whether the golem may take the task now
     */
    public boolean canBePerformedBy(IGolemAPI golem) {
        ISealEntity seal = GolemHelper.getSealEntity(golem.level(), origin);
        if (seal == null) {
            return true;
        }
        if (golem.color() > 0 && seal.color() > 0 && golem.color() != seal.color()) {
            return false;
        }
        return seal.behavior().canPerform(seal, golem, this);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Task task && task.id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
