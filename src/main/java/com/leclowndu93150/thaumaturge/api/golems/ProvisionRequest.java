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
 * A request for golems to deliver an item stack somewhere: to a seal, a block face or an
 * entity. Requests live in the per-level provision list and expire on a game-time timeout
 * unless a task links to them.
 *
 * @since 1.0.0
 */
public final class ProvisionRequest {
    private static final long TIMEOUT_TICKS = 200L;
    private static final long LINKED_TIMEOUT_TICKS = 2400L;

    private final ISealEntity seal;
    private final Entity entity;
    private BlockPos pos;
    private Direction side;
    private final ItemStack stack;
    private int id;
    private int ui;
    private Task linkedTask;
    private boolean invalid;
    private long timeout;
    private final Level level;

    ProvisionRequest(Level level, ISealEntity seal, ItemStack stack) {
        this.level = level;
        this.seal = seal;
        this.entity = null;
        this.stack = stack.copy();
        this.id = Objects.hash(seal.getSealPos().pos(), seal.getSealPos().face(), ItemStack.hashItemAndComponents(stack), stack.getCount());
        this.timeout = level.getGameTime() + TIMEOUT_TICKS;
    }

    ProvisionRequest(Level level, BlockPos pos, Direction side, ItemStack stack) {
        this.level = level;
        this.seal = null;
        this.entity = null;
        this.pos = pos;
        this.side = side;
        this.stack = stack.copy();
        this.id = Objects.hash(pos, side, ItemStack.hashItemAndComponents(stack), stack.getCount());
        this.timeout = level.getGameTime() + TIMEOUT_TICKS;
    }

    ProvisionRequest(Level level, Entity entity, ItemStack stack) {
        this.level = level;
        this.seal = null;
        this.entity = entity;
        this.stack = stack.copy();
        this.id = Objects.hash(entity.getId(), ItemStack.hashItemAndComponents(stack), stack.getCount());
        this.timeout = level.getGameTime() + TIMEOUT_TICKS;
    }

    /**
     * @return the game time after which the request expires
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Pushes the timeout out to the linked-task horizon.
     */
    public void extendTimeout() {
        this.timeout = level.getGameTime() + LINKED_TIMEOUT_TICKS;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Tags this request with a requester UI discriminator so identical requests from
     * different sources do not collapse.
     *
     * @param ui the discriminator
     */
    public void setUI(int ui) {
        this.ui = ui;
    }

    public @Nullable ISealEntity getSeal() {
        return seal;
    }

    public @Nullable Entity getEntity() {
        return entity;
    }

    public ItemStack getStack() {
        return stack;
    }

    public @Nullable BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public @Nullable Direction getSide() {
        return side;
    }

    public void setSide(Direction side) {
        this.side = side;
    }

    public @Nullable Task getLinkedTask() {
        return linkedTask;
    }

    /**
     * Links the task fulfilling this request and extends the timeout.
     *
     * @param linkedTask the fulfilling task
     */
    public void setLinkedTask(@Nullable Task linkedTask) {
        this.linkedTask = linkedTask;
        extendTimeout();
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ProvisionRequest request && request.id == id && request.ui == ui;
    }

    @Override
    public int hashCode() {
        return 31 * id + ui;
    }
}
