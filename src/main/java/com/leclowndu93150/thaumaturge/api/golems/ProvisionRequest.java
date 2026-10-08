package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A request to bring an item stack to a seal (into a golem's hands), a block face or an entity. Provide seals answer requests by posting collection and delivery tasks.
 *
 * <p>
 * Requests are created through {@link GolemHelper#requestProvisioning}. They are server side only, never saved, and kept in posting order in a per-level queue. Two requests are equal
 * when they have the same target, the same item with the same components, the same count and the same batch number; an equal request is not queued twice.
 *
 * @since 1.0.0
 */
public final class ProvisionRequest {
    private static final long TIMEOUT_TICKS = 200L;
    private static final long LINKED_TIMEOUT_TICKS = 2400L;

    private final Level level;
    private final @Nullable ISealEntity seal;
    private final @Nullable Entity entity;
    private @Nullable BlockPos pos;
    private @Nullable Direction side;
    private final ItemStack stack;
    private int id;
    private int batch;
    private @Nullable Task serving;
    private boolean spent;
    private long expiresAt;

    ProvisionRequest(Level level, ISealEntity seal, ItemStack stack) {
        this(level, seal, null, null, null, stack);
    }

    ProvisionRequest(Level level, BlockPos pos, Direction side, ItemStack stack) {
        this(level, null, null, pos.immutable(), side, stack);
    }

    ProvisionRequest(Level level, Entity entity, ItemStack stack) {
        this(level, null, entity, null, null, stack);
    }

    private ProvisionRequest(
            Level level,
            @Nullable ISealEntity seal,
            @Nullable Entity entity,
            @Nullable BlockPos pos,
            @Nullable Direction side,
            ItemStack stack) {
        this.level = level;
        this.seal = seal;
        this.entity = entity;
        this.pos = pos;
        this.side = side;
        this.stack = stack.copy();
        this.expiresAt = level.getGameTime() + TIMEOUT_TICKS;
    }

    /**
     * @return the game time after which the request drops out of the queue: 10 seconds after posting, or 2 minutes after a task was last linked or unlinked
     * @since 1.1.0
     */
    public long expiresAt() {
        return expiresAt;
    }

    /**
     * @return the game time after which the request drops out of the queue
     * @since 1.0.0
     * @deprecated use {@link #expiresAt()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public long getTimeout() {
        return expiresAt();
    }

    /**
     * Moves the expiry to 2 minutes from now.
     *
     * @since 1.0.0
     */
    public void extendTimeout() {
        expiresAt = level.getGameTime() + LINKED_TIMEOUT_TICKS;
    }

    /**
     * @return an identifier with no meaning to the mod
     * @since 1.0.0
     * @deprecated requests are identified by {@link #equals(Object)}; nothing reads this value
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public int getId() {
        return id;
    }

    /**
     * @param id an identifier with no meaning to the mod
     * @since 1.0.0
     * @deprecated requests are identified by {@link #equals(Object)}; nothing reads this value
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the batch number, which keeps the identical stacks of one large order from being folded together as duplicates
     * @since 1.1.0
     */
    public int batch() {
        return batch;
    }

    /**
     * @param batch the batch number, normally 0
     * @since 1.1.0
     */
    public void setBatch(int batch) {
        this.batch = batch;
    }

    /**
     * @param ui the batch number
     * @since 1.0.0
     * @deprecated use {@link #setBatch(int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setUI(int ui) {
        setBatch(ui);
    }

    /**
     * @return the requesting seal for a seal target, otherwise {@code null}
     * @since 1.0.0
     */
    public @Nullable ISealEntity getSeal() {
        return seal;
    }

    /**
     * @return the receiving entity for an entity target, otherwise {@code null}
     * @since 1.0.0
     */
    public @Nullable Entity getEntity() {
        return entity;
    }

    /**
     * @return the stack wanted, copied when the request was made
     * @since 1.0.0
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * @return the receiving block for a block target, otherwise {@code null}
     * @since 1.0.0
     */
    public @Nullable BlockPos getPos() {
        return pos;
    }

    /**
     * @param pos the receiving block
     * @since 1.0.0
     * @deprecated a request's target is fixed when it is made; post a new request through {@link GolemHelper#requestProvisioning} instead
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setPos(@Nullable BlockPos pos) {
        this.pos = pos;
    }

    /**
     * @return the face items are inserted through for a block target, otherwise {@code null}
     * @since 1.0.0
     */
    public @Nullable Direction getSide() {
        return side;
    }

    /**
     * @param side the face items are inserted through
     * @since 1.0.0
     * @deprecated a request's target is fixed when it is made; post a new request through {@link GolemHelper#requestProvisioning} instead
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setSide(@Nullable Direction side) {
        this.side = side;
    }

    /**
     * @return the task currently serving the request, or {@code null} when it is unclaimed
     * @since 1.0.0
     */
    public @Nullable Task getLinkedTask() {
        return serving;
    }

    /**
     * Links the request and the task that serves it, on both sides, and moves the expiry to 2 minutes from now.
     *
     * @param task the serving task
     * @since 1.1.0
     */
    @SuppressWarnings("removal")
    public void link(Task task) {
        serving = task;
        task.linkProvision(this);
        extendTimeout();
    }

    /**
     * Releases the request from its serving task, on both sides, so a later scan may claim it again, and moves the expiry to 2 minutes from now.
     *
     * @since 1.1.0
     */
    @SuppressWarnings("removal")
    public void unlink() {
        if (serving != null && serving.linkedProvision() == this) {
            serving.linkProvision(null);
        }
        serving = null;
        extendTimeout();
    }

    /**
     * Sets the request's side of a task link only, and moves the expiry to 2 minutes from now.
     *
     * @param linkedTask the serving task, or {@code null} to release the request
     * @since 1.0.0
     * @deprecated use {@link #link(Task)} and {@link #unlink()}, which keep both sides of the link in step
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setLinkedTask(@Nullable Task linkedTask) {
        this.serving = linkedTask;
        extendTimeout();
    }

    /**
     * @return whether the request has been delivered and is waiting to be removed
     * @since 1.1.0
     */
    public boolean isSpent() {
        return spent;
    }

    /**
     * Marks the request as delivered. The next tidy of the queue removes it.
     *
     * @since 1.1.0
     */
    public void markSpent() {
        this.spent = true;
    }

    /**
     * @return whether the request has been delivered
     * @since 1.0.0
     * @deprecated use {@link #isSpent()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public boolean isInvalid() {
        return isSpent();
    }

    /**
     * @param invalid {@code true} once the request has been delivered
     * @since 1.0.0
     * @deprecated use {@link #markSpent()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setInvalid(boolean invalid) {
        this.spent = invalid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ProvisionRequest other)
                || other.batch != batch
                || other.stack.getCount() != stack.getCount()
                || !ItemStack.isSameItemSameComponents(other.stack, stack)) {
            return false;
        }
        if (seal != null || other.seal != null) {
            return seal != null && other.seal != null && seal.pos().equals(other.seal.pos());
        }
        if (entity != null || other.entity != null) {
            return entity == other.entity;
        }
        return Objects.equals(pos, other.pos) && side == other.side;
    }

    @Override
    public int hashCode() {
        Object target = seal != null ? seal.pos() : entity != null ? entity : pos;
        return Objects.hash(target, side, batch, stack.getCount(), ItemStack.hashItemAndComponents(stack));
    }
}
