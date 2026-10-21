package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Static facade for the golem system: seal lookup, task posting and the provisioning queue.
 *
 * <p>The mod binds the implementation once during construction. Calls made before that throw
 * {@link IllegalStateException}. Posting tasks and provisioning requests is server side and must run on the server thread;
 * only {@link #getSealEntity} also works on the client.
 *
 * @since 1.0.0
 */
public final class GolemHelper {
    private static final int QUEUE_LIMIT = 1000;
    private static final int DEFAULT_BATCH = 0;
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("GolemHelper");

    private GolemHelper() {}

    /**
     * Installs the implementation. Called once by the mod.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when an implementation is already bound
     */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    /**
     * @param id a seal type id
     * @return the seal type, or empty for an unknown id
     */
    public static Optional<SealType> sealType(Identifier id) {
        return bound().sealType(id);
    }

    /**
     * @param id a seal type id
     * @return one placer item of the type, or the empty stack for an unknown id
     */
    public static ItemStack getSealStack(Identifier id) {
        return sealType(id).map(type -> new ItemStack(type.placer())).orElse(ItemStack.EMPTY);
    }

    /**
     * Places a registered seal on the context's clicked face and consumes one item from its actual
     * player hand, except in creative mode. The context stack must be that same hand stack.
     * Checks ownership context, reach, loaded chunks, permissions, research and the type's placer
     * predicate before committing. Rejected attempts leave placement and payment unchanged.
     * No offline or remote delegation is supported. Must run on the server thread.
     *
     * @param context the player's use context, including the real hand slot
     * @param typeId the registered type to place
     * @return whether one seal was placed
     */
    public static boolean tryPlaceSeal(UseOnContext context, Identifier typeId) {
        return bound().tryPlaceSeal(context, typeId);
    }

    /**
     * Looks up a placed seal. Works on both logical sides; the client answers from the mirror the server sends.
     *
     * @param level the level
     * @param pos   the seal placement, or null
     * @return the seal, or null when there is none or {@code pos} is null
     */
    public static @Nullable ISealEntity getSealEntity(Level level, @Nullable SealPos pos) {
        return bound().getSealEntity(level, pos);
    }

    /**
     * Posts a task to the level's task board, which gives it a fresh id.
     *
     * @param level the level
     * @param task  the task
     */
    public static void addGolemTask(Level level, Task task) {
        bound().addGolemTask(level, task);
    }

    /**
     * Asks provider seals to bring a copy of the stack to a seal. A request equal to a queued one is not added.
     *
     * @param level the level
     * @param seal  the seal that needs the items
     * @param stack the stack; copied
     */
    public static void requestProvisioning(Level level, ISealEntity seal, ItemStack stack) {
        submit(level, DEFAULT_BATCH, new ProvisionRequest(level, seal, stack));
    }

    /**
     * Asks provider seals to bring a copy of the stack to a block face. A request equal to a queued one is not added.
     *
     * @param level the level
     * @param pos   the target block
     * @param side  the face the stack is inserted through
     * @param stack the stack; copied
     */
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack) {
        requestProvisioning(level, pos, side, stack, DEFAULT_BATCH);
    }

    /**
     * Asks provider seals to bring a copy of the stack to an entity. A request equal to a queued one is not added.
     *
     * @param level  the level
     * @param entity the target entity
     * @param stack  the stack; copied
     */
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack) {
        requestProvisioning(level, entity, stack, DEFAULT_BATCH);
    }

    /**
     * Like {@link #requestProvisioning(Level, BlockPos, Direction, ItemStack)} with a batch number, so identical requests of
     * one large order stay separate.
     *
     * @param level the level
     * @param pos   the target block
     * @param side  the face the stack is inserted through
     * @param stack the stack; copied
     * @param batch the batch number
     */
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack, int batch) {
        submit(level, batch, new ProvisionRequest(level, pos, side, stack));
    }

    /**
     * Like {@link #requestProvisioning(Level, Entity, ItemStack)} with a batch number, so identical requests of one large
     * order stay separate.
     *
     * @param level  the level
     * @param entity the target entity
     * @param stack  the stack; copied
     * @param batch  the batch number
     */
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack, int batch) {
        submit(level, batch, new ProvisionRequest(level, entity, stack));
    }

    /**
     * @param level the level
     * @return the level's live provisioning queue in posting order, not a copy; it supports removal by predicate and by
     *         index and concurrent iteration
     */
    public static List<ProvisionRequest> getProvisionRequests(Level level) {
        return bound().getProvisionRequests(level);
    }

    private static Bindings bound() {
        return BINDING.get();
    }

    private static void submit(Level level, int batch, ProvisionRequest request) {
        request.withBatch(batch);
        List<ProvisionRequest> pending = getProvisionRequests(level);
        if (pending.contains(request)) {
            return;
        }
        int overflow = pending.size() - QUEUE_LIMIT + 1;
        for (int i = 0; i < overflow; i++) {
            pending.remove(0);
        }
        pending.add(request);
    }

    /**
     * The implementation behind {@link GolemHelper}, bound by the mod once.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /** Performs validated server-side placement and hand-slot payment. */
        default boolean tryPlaceSeal(UseOnContext context, Identifier typeId) {
            return false;
        }

        /**
         * @param id a seal type id
         * @return the seal type, or empty
         */
        Optional<SealType> sealType(Identifier id);

        /**
         * @param level the level
         * @param pos   the placement, possibly null
         * @return the seal, or null
         */
        @Nullable
        ISealEntity getSealEntity(Level level, @Nullable SealPos pos);

        /**
         * @param level the level
         * @param task  the task to post
         */
        void addGolemTask(Level level, Task task);

        /**
         * @param level the level
         * @return the level's live provisioning queue
         */
        List<ProvisionRequest> getProvisionRequests(Level level);
    }
}
