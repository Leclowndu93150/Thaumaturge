package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISeal;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Static facade for the golem task, seal and provisioning systems. Backed by the mod at
 * init via {@link #bind}; all calls before binding throw {@link IllegalStateException}.
 *
 * @since 1.0.0
 */
public final class GolemHelper {
    private static final int LIST_LIMIT = 1000;

    private static Bindings bindings;

    private GolemHelper() {}

    /**
     * Binds the implementation. Called once by the mod during construction.
     *
     * @param impl the implementation to delegate to
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
     * @param key the seal kind's registry id
     * @return a fresh behavior instance of that kind, or null when unknown
     */
    public static @Nullable ISeal createSeal(Identifier key) {
        return impl().createSeal(key);
    }

    /**
     * @param key the seal kind's registry id
     * @return the placer item stack for that kind, or an empty stack when unknown
     */
    public static ItemStack getSealStack(Identifier key) {
        return impl().getSealStack(key);
    }

    /**
     * @param level the level
     * @param pos   the placement to look up
     * @return the placed seal there, or null
     */
    public static @Nullable ISealEntity getSealEntity(Level level, @Nullable SealPos pos) {
        return impl().getSealEntity(level, pos);
    }

    /**
     * Adds a task to the level's task list. Server side only.
     *
     * @param level the level
     * @param task  the task to add
     */
    public static void addGolemTask(Level level, Task task) {
        impl().addGolemTask(level, task);
    }

    /**
     * Requests delivery of a stack to a seal.
     */
    public static void requestProvisioning(Level level, ISealEntity seal, ItemStack stack) {
        addRequest(level, new ProvisionRequest(level, seal, stack));
    }

    /**
     * Requests delivery of a stack to a block face.
     */
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack) {
        addRequest(level, new ProvisionRequest(level, pos, side, stack));
    }

    /**
     * Requests delivery of a stack to an entity.
     */
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack) {
        addRequest(level, new ProvisionRequest(level, entity, stack));
    }

    /**
     * Requests delivery of a stack to a block face with a requester discriminator.
     *
     * @param ui the discriminator keeping identical requests from different sources apart
     */
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack, int ui) {
        ProvisionRequest request = new ProvisionRequest(level, pos, side, stack);
        request.setUI(ui);
        addRequest(level, request);
    }

    /**
     * Requests delivery of a stack to an entity with a requester discriminator.
     *
     * @param ui the discriminator keeping identical requests from different sources apart
     */
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack, int ui) {
        ProvisionRequest request = new ProvisionRequest(level, entity, stack);
        request.setUI(ui);
        addRequest(level, request);
    }

    private static void addRequest(Level level, ProvisionRequest request) {
        List<ProvisionRequest> list = impl().getProvisionRequests(level);
        if (!list.contains(request)) {
            list.add(request);
        }
        if (list.size() > LIST_LIMIT) {
            list.remove(0);
        }
    }

    /**
     * @param level the level
     * @return the level's live provision request list
     */
    public static List<ProvisionRequest> getProvisionRequests(Level level) {
        return impl().getProvisionRequests(level);
    }

    /**
     * Enumerates positions inside a seal's working area.
     *
     * @param seal  the placed seal
     * @param count the enumeration index
     * @return the position for that index
     */
    public static BlockPos getPosInArea(ISealEntity seal, int count) {
        SealPos sealPos = seal.getSealPos();
        int xx = 1 + (seal.getArea().getX() - 1) * (sealPos.face().getStepX() == 0 ? 2 : 1);
        int yy = 1 + (seal.getArea().getY() - 1) * (sealPos.face().getStepY() == 0 ? 2 : 1);
        int zz = 1 + (seal.getArea().getZ() - 1) * (sealPos.face().getStepZ() == 0 ? 2 : 1);
        int qx = sealPos.face().getStepX() != 0 ? sealPos.face().getStepX() : 1;
        int qy = sealPos.face().getStepY() != 0 ? sealPos.face().getStepY() : 1;
        int qz = sealPos.face().getStepZ() != 0 ? sealPos.face().getStepZ() : 1;
        int y = qy * (count / zz / xx) % yy + sealPos.face().getStepY();
        int x = qx * (count / zz) % xx + sealPos.face().getStepX();
        int z = qz * count % zz + sealPos.face().getStepZ();
        return sealPos.pos().offset(x - (sealPos.face().getStepX() == 0 ? xx / 2 : 0), y - (sealPos.face().getStepY() == 0 ? yy / 2 : 0), z - (sealPos.face().getStepZ() == 0 ? zz / 2 : 0));
    }

    /**
     * @param seal the placed seal
     * @return the bounding box of the seal's working area
     */
    public static AABB getBoundsForArea(ISealEntity seal) {
        SealPos sealPos = seal.getSealPos();
        return new AABB(sealPos.pos().getX(), sealPos.pos().getY(), sealPos.pos().getZ(), sealPos.pos().getX() + 1, sealPos.pos().getY() + 1, sealPos.pos().getZ() + 1)
                .move(sealPos.face().getStepX(), sealPos.face().getStepY(), sealPos.face().getStepZ())
                .expandTowards(sealPos.face().getStepX() != 0 ? (seal.getArea().getX() - 1) * sealPos.face().getStepX() : 0.0,
                        sealPos.face().getStepY() != 0 ? (seal.getArea().getY() - 1) * sealPos.face().getStepY() : 0.0,
                        sealPos.face().getStepZ() != 0 ? (seal.getArea().getZ() - 1) * sealPos.face().getStepZ() : 0.0)
                .inflate(sealPos.face().getStepX() == 0 ? seal.getArea().getX() - 1 : 0.0, sealPos.face().getStepY() == 0 ? seal.getArea().getY() - 1 : 0.0,
                        sealPos.face().getStepZ() == 0 ? seal.getArea().getZ() - 1 : 0.0);
    }

    /**
     * Implementation hooks backing {@link GolemHelper}.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        @Nullable
        ISeal createSeal(Identifier key);

        ItemStack getSealStack(Identifier key);

        @Nullable
        ISealEntity getSealEntity(Level level, @Nullable SealPos pos);

        void addGolemTask(Level level, Task task);

        List<ProvisionRequest> getProvisionRequests(Level level);
    }
}
