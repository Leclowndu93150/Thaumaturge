package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jspecify.annotations.Nullable;

public final class ProvideBehavior implements ISealBehavior {
    public static final SealSetting SINGLE_ITEM =
            new SealSetting("psing", "gui.thaumaturge.seal.setting.single", false);
    public static final SealSetting LEAVE_ONE = new SealSetting("pleave", "gui.thaumaturge.seal.setting.leave", false);

    private static final int CLOCK_SPREAD = 88;
    private static final int SCAN_PERIOD = 20;
    private static final int TIDY_PERIOD = 100;
    private static final double SERVICE_RANGE = 64.0;
    private static final byte OPEN_REQUEST_PRIORITY = 5;
    private static final int SEAL_COLLECTION_LIFE = 10;
    private static final int CARRY_LIFE = 31000;
    private static final int LEG_COLLECT = 0;
    private static final int LEG_TO_ENTITY = 1;
    private static final int LEG_TO_BLOCK = 2;
    private static final double DROP_PUSH = 0.3;
    private static final double HALF = 0.5;

    private final SealClock clock = new SealClock(CLOCK_SPREAD);

    public static boolean supplies(ISealEntity seal, ItemStack stack) {
        Optional<ISealFilter> filter = seal.filter();
        if (filter.isEmpty()) {
            return true;
        }
        InvHelper.InvFilter matching = ItemMatchSettings.of(seal);
        boolean listed = filter.get().stacks().stream()
                .anyMatch(slot -> !slot.isEmpty() && InvHelper.areItemStacksEqual(slot, stack, matching));
        return filter.get().isBlacklist() != listed;
    }

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        int now = clock.advance();
        if (now % TIDY_PERIOD == 0) {
            tidy(level);
        }
        if (now % SCAN_PERIOD == 0) {
            scan(level, seal);
        }
    }

    private static void tidy(ServerLevel level) {
        long time = level.getGameTime();
        GolemHelper.getProvisionRequests(level)
                .removeIf(request -> request.isSpent()
                        || time > request.expiresAt()
                        || isServedOut(request.getLinkedTask())
                        || sealGone(level, request));
    }

    private static boolean isServedOut(@Nullable Task task) {
        return task != null && (task.isEnded() || task.isCompleted());
    }

    private static boolean sealGone(ServerLevel level, ProvisionRequest request) {
        ISealEntity requester = request.getSeal();
        return requester != null && GolemHelper.getSealEntity(level, requester.pos()) == null;
    }

    private static void scan(ServerLevel level, ISealEntity seal) {
        SealPos at = seal.pos();
        IItemHandler store = InvHelper.getItemHandlerAt(level, at.pos(), at.face());
        if (store == null) {
            return;
        }
        List<ProvisionRequest> queue = GolemHelper.getProvisionRequests(level);
        queue.removeIf(ProvisionRequest::isSpent);
        int minimumStock = seal.setting(LEAVE_ONE) ? 2 : 1;
        for (ProvisionRequest request : queue) {
            if (request.isSpent() || request.getLinkedTask() != null) {
                continue;
            }
            BlockPos destination = destination(request);
            if (destination == null
                    || destination.distSqr(at.pos()) >= SERVICE_RANGE * SERVICE_RANGE
                    || !supplies(seal, request.getStack())
                    || InvHelper.countTotalItemsIn(store, request.getStack(), InvHelper.InvFilter.STRICT)
                            < minimumStock) {
                continue;
            }
            ISealEntity requester = request.getSeal();
            Task collection = Task.atBlock(at, at.pos());
            collection.setPriority(requester != null ? requester.priority() : OPEN_REQUEST_PRIORITY);
            collection.setLife(requester != null ? SEAL_COLLECTION_LIFE : CARRY_LIFE);
            collection.setData(LEG_COLLECT);
            post(level, collection, request);
            return;
        }
    }

    private static void post(ServerLevel level, Task task, ProvisionRequest request) {
        TaskBoard.of(level).post(task);
        request.link(task);
    }

    private static @Nullable BlockPos destination(ProvisionRequest request) {
        if (request.getSeal() != null) {
            return request.getSeal().pos().pos();
        }
        if (request.getEntity() != null) {
            return request.getEntity().blockPosition();
        }
        return request.getPos();
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request == null) {
            return false;
        }
        BlockPos destination = destination(request);
        if (destination == null || !(golem.asEntity() instanceof Mob body) || !body.isWithinRestriction(destination)) {
            return false;
        }
        ItemStack wanted = request.getStack();
        if (task.data() != LEG_COLLECT) {
            return golem.hands().holds(wanted);
        }
        return SealAccess.allows(request.getSeal(), golem)
                && !golem.hands().holds(wanted)
                && golem.hands().room(wanted) > 0;
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request != null) {
            if (task.data() == LEG_COLLECT) {
                collect(level, seal, golem, task, request);
            } else {
                deliver(level, golem, request);
            }
        }
        task.end();
        return true;
    }

    private static void collect(
            ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task, ProvisionRequest request) {
        SealPos at = seal.pos();
        IItemHandler store = InvHelper.getItemHandlerAt(level, at.pos(), at.face());
        if (store == null) {
            return;
        }
        ItemStack wanted = request.getStack();
        int amount = seal.setting(SINGLE_ITEM) ? 1 : wanted.getCount();
        if (seal.setting(LEAVE_ONE)) {
            int stocked = InvHelper.countTotalItemsIn(store, wanted, InvHelper.InvFilter.STRICT);
            if (stocked <= amount) {
                amount = stocked - 1;
            }
        }
        amount = Math.min(amount, golem.hands().room(wanted));
        if (amount <= 0) {
            return;
        }
        ItemStack taken =
                InvHelper.removeStackFrom(store, wanted.copyWithCount(amount), InvHelper.InvFilter.STRICT, false);
        if (taken.isEmpty()) {
            return;
        }
        ItemStack spill = InvHelper.insertStack(store, golem.hands().hold(taken), false);
        if (!spill.isEmpty()) {
            dropToward(level, at.pos(), at.face(), spill);
        }
        HandlingSound.play(golem, HandlingSound.HIGH);
        golem.addRankXp(1);
        golem.swingArm();
        if (request.getSeal() != null) {
            return;
        }
        Entity receiver = request.getEntity();
        Task delivery = receiver != null ? Task.onEntity(at, receiver) : Task.atBlock(at, request.getPos());
        delivery.setPriority(task.priority());
        delivery.setLife(CARRY_LIFE);
        delivery.setData(receiver != null ? LEG_TO_ENTITY : LEG_TO_BLOCK);
        post(level, delivery, request);
    }

    private static void deliver(ServerLevel level, IGolemAPI golem, ProvisionRequest request) {
        ItemStack wanted = request.getStack();
        ItemStack given = golem.hands().release(wanted);
        int shortfall = wanted.getCount() - given.getCount();
        Entity receiver = request.getEntity();
        if (shortfall > 0) {
            if (receiver != null) {
                GolemHelper.requestProvisioning(level, receiver, wanted.copyWithCount(shortfall));
            } else if (request.getPos() != null && request.getSide() != null) {
                GolemHelper.requestProvisioning(
                        level, request.getPos(), request.getSide(), wanted.copyWithCount(shortfall));
            }
        }
        if (!given.isEmpty()) {
            if (receiver != null) {
                level.addFreshEntity(new ItemEntity(
                        level,
                        receiver.getX(),
                        receiver.getY() + receiver.getEyeHeight() * HALF,
                        receiver.getZ(),
                        given));
            } else if (request.getPos() != null && request.getSide() != null) {
                handOver(level, golem, request.getPos(), request.getSide(), given);
            }
        }
        HandlingSound.play(golem, HandlingSound.LOW);
        golem.swingArm();
        request.markSpent();
    }

    private static void handOver(ServerLevel level, IGolemAPI golem, BlockPos pos, Direction side, ItemStack stack) {
        IItemHandler inventory = InvHelper.getItemHandlerAt(level, pos, side);
        if (inventory == null) {
            dropToward(level, pos, side, stack);
            return;
        }
        ItemStack refused = golem.hands().hold(InvHelper.insertStack(inventory, stack, false));
        if (!refused.isEmpty()) {
            dropToward(level, pos, side, refused);
        }
    }

    private static void dropToward(ServerLevel level, BlockPos pos, Direction face, ItemStack stack) {
        BlockPos spot = level.getBlockState(pos).isCollisionShapeFullBlock(level, pos) ? pos.relative(face) : pos;
        Vec3 centre = Vec3.atCenterOf(spot);
        ItemEntity item = new ItemEntity(level, centre.x, centre.y, centre.z, stack);
        item.setDeltaMovement(
                Vec3.atLowerCornerOf(face.getOpposite().getNormal()).scale(DROP_PUSH));
        level.addFreshEntity(item);
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request != null && task.equals(request.getLinkedTask())) {
            request.unlink();
        }
    }
}
