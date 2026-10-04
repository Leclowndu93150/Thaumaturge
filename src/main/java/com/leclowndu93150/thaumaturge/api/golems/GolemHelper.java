package com.leclowndu93150.thaumaturge.api.golems;

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
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Static facade for golem seals, the task board and provisioning. The mod binds it during construction through {@link #bind}; calls
 * made before that throw {@link IllegalStateException}.
 *
 * <p>Task and provisioning methods are server-side and must run on the server thread.
 *
 * @since 1.0.0
 */
public final class GolemHelper {
    private static final int PROVISION_QUEUE_LIMIT = 1000;
    private static @Nullable Bindings bindings;

    private GolemHelper() {}

    /**
     * Installs the implementation. Called once by the mod.
     *
     * @param impl the implementation
     */
    public static void bind(Bindings impl) {
        bindings = impl;
    }

    private static Bindings impl() {
        if (bindings == null) {
            throw new IllegalStateException("GolemHelper used before Thaumaturge bound it");
        }
        return bindings;
    }

    /**
     * @param id a seal type id
     * @return the registered seal type, or empty
     */
    public static Optional<SealType> sealType(Identifier id) {
        return impl().sealType(id);
    }

    /**
     * @param id a seal type id
     * @return a stack of the type's placer item, or an empty stack for an unknown id
     */
    public static ItemStack getSealStack(Identifier id) {
        return sealType(id).map(type -> new ItemStack(type.placer())).orElse(ItemStack.EMPTY);
    }

    /**
     * Looks a placed seal up. Works on both sides; the client sees the synced mirror.
     *
     * @param level the level
     * @param pos   the seal position, or null
     * @return the seal, or null when there is none at that position
     */
    public static @Nullable ISealEntity getSealEntity(Level level, @Nullable SealPos pos) {
        return impl().getSealEntity(level, pos);
    }

    /**
     * Posts a task to the level's task board and assigns its id.
     *
     * @param level the level
     * @param task  the task
     */
    public static void addGolemTask(Level level, Task task) {
        impl().addGolemTask(level, task);
    }

    /**
     * Asks provider seals to bring items to a seal.
     *
     * @param level the level
     * @param seal  the seal that needs the items
     * @param stack the items, copied
     */
    public static void requestProvisioning(Level level, ISealEntity seal, ItemStack stack) {
        queue(level, new ProvisionRequest(level, seal, stack));
    }

    /**
     * Asks provider seals to bring items to a block face, usually an inventory.
     *
     * @param level the level
     * @param pos   the block
     * @param side  the face to deliver into
     * @param stack the items, copied
     */
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack) {
        queue(level, new ProvisionRequest(level, pos, side, stack));
    }

    /**
     * Asks provider seals to bring items to an entity.
     *
     * @param level  the level
     * @param entity the receiver
     * @param stack  the items, copied
     */
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack) {
        queue(level, new ProvisionRequest(level, entity, stack));
    }

    /**
     * Like {@link #requestProvisioning(Level, BlockPos, Direction, ItemStack)}, with a discriminator that keeps otherwise identical
     * requests apart.
     *
     * @param level the level
     * @param pos   the block
     * @param side  the face to deliver into
     * @param stack the items, copied
     * @param ui    the discriminator
     */
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack, int ui) {
        ProvisionRequest request = new ProvisionRequest(level, pos, side, stack);
        request.setUI(ui);
        queue(level, request);
    }

    /**
     * Like {@link #requestProvisioning(Level, Entity, ItemStack)}, with a discriminator that keeps otherwise identical requests
     * apart.
     *
     * @param level  the level
     * @param entity the receiver
     * @param stack  the items, copied
     * @param ui     the discriminator
     */
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack, int ui) {
        ProvisionRequest request = new ProvisionRequest(level, entity, stack);
        request.setUI(ui);
        queue(level, request);
    }

    private static void queue(Level level, ProvisionRequest request) {
        List<ProvisionRequest> queue = impl().getProvisionRequests(level);
        if (!queue.contains(request)) {
            queue.add(request);
        }
        if (queue.size() > PROVISION_QUEUE_LIMIT) {
            queue.remove(0);
        }
    }

    /**
     * @param level the level
     * @return the live provisioning queue; duplicate requests are folded and the oldest are dropped past 1000
     */
    public static List<ProvisionRequest> getProvisionRequests(Level level) {
        return impl().getProvisionRequests(level);
    }

    /**
     * The hooks the mod implements behind this facade.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * @param id a seal type id
         * @return the registered type, or empty
         */
        Optional<SealType> sealType(Identifier id);

        /**
         * @param level the level
         * @param pos   the seal position, or null
         * @return the placed seal, or null
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
