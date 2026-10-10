package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper.InvFilter;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class ProvideBehavior implements ISealBehavior {
    public static final SealSetting SINGLE_ITEM = new SealSetting("single_item", "gui.thaumaturge.seal.setting.single", false);
    public static final SealSetting LEAVE_ONE = new SealSetting("leave_one", "gui.thaumaturge.seal.setting.leave", false);

    private static final int STAGGER = 88;
    private static final int TIDY_PERIOD = 100;
    private static final int SCAN_PERIOD = 20;
    private static final double REACH = 64.0;
    private static final byte OPEN_REQUEST_PRIORITY = 5;
    private static final int SEAL_REQUEST_LIFE = 10;
    private static final int LONG_LIFE = 31000;
    private static final int COLLECT_XP = 1;
    private static final double PUSH_SPEED = 0.3;

    private final SealClock clock = new SealClock(STAGGER);

    public static boolean supplies(ISealEntity seal, ItemStack stack) {
        return ItemMatchSettings.accepts(seal, stack);
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
        GolemHelper.getProvisionRequests(level).removeIf(request -> request.isSpent() || time > request.expiresAt() || servingTaskIsDone(request) || requesterIsGone(level, request));
    }

    private static boolean servingTaskIsDone(ProvisionRequest request) {
        Task serving = request.getLinkedTask();
        return serving != null && (serving.isEnded() || serving.isCompleted());
    }

    private static boolean requesterIsGone(Level level, ProvisionRequest request) {
        ISealEntity requester = request.getSeal();
        return requester != null && GolemHelper.getSealEntity(level, requester.pos()) != requester;
    }

    private void scan(ServerLevel level, ISealEntity seal) {
        SealPos at = seal.pos();
        ResourceHandler<ItemResource> container = InvHelper.getItemHandlerAt(level, at.pos(), at.face());
        if (container == null) {
            return;
        }
        List<ProvisionRequest> queue = GolemHelper.getProvisionRequests(level);
        queue.removeIf(ProvisionRequest::isSpent);
        int needed = seal.setting(LEAVE_ONE) ? 2 : 1;
        for (ProvisionRequest request : queue) {
            if (canServe(seal, container, request, needed)) {
                postCollection(level, seal, request);
                return;
            }
        }
    }

    private static boolean canServe(ISealEntity seal, ResourceHandler<ItemResource> container, ProvisionRequest request, int needed) {
        if (request.isSpent() || request.getLinkedTask() != null) {
            return false;
        }
        BlockPos destination = destinationOf(request);
        if (destination == null || !destination.closerThan(seal.pos().pos(), REACH)) {
            return false;
        }
        ItemStack stack = request.getStack();
        return supplies(seal, stack) && InvHelper.countTotalItemsIn(container, stack, InvFilter.STRICT) >= needed;
    }

    private static void postCollection(ServerLevel level, ISealEntity seal, ProvisionRequest request) {
        ISealEntity requester = request.getSeal();
        Task task = Task.atBlock(seal.pos(), seal.pos().pos());
        task.setPriority(requester != null ? requester.priority() : OPEN_REQUEST_PRIORITY);
        task.setLife(requester != null ? SEAL_REQUEST_LIFE : LONG_LIFE);
        task.setData(Leg.COLLECT.ordinal());
        GolemHelper.addGolemTask(level, task);
        request.link(task);
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request == null || !isHomeOf(golem, destinationOf(request))) {
            return false;
        }
        ItemStack stack = request.getStack();
        if (Leg.of(task) != Leg.COLLECT) {
            return golem.hands().holds(stack);
        }
        return SealAccess.allows(request.getSeal(), golem) && !golem.hands().holds(stack) && golem.hands().room(stack) > 0;
    }

    private static boolean isHomeOf(IGolemAPI golem, @Nullable BlockPos destination) {
        if (destination == null) {
            return false;
        }
        return !(golem.asEntity() instanceof Mob mob) || mob.isWithinHome(destination);
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request != null) {
            if (Leg.of(task) == Leg.COLLECT) {
                collect(level, seal, golem, task, request);
            } else {
                deliver(level, golem, request);
            }
        }
        task.end();
        return true;
    }

    private static void collect(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task, ProvisionRequest request) {
        SealPos at = seal.pos();
        ResourceHandler<ItemResource> container = InvHelper.getItemHandlerAt(level, at.pos(), at.face());
        if (container == null) {
            return;
        }
        ItemStack wanted = request.getStack();
        int amount = seal.setting(SINGLE_ITEM) ? 1 : wanted.getCount();
        if (seal.setting(LEAVE_ONE)) {
            int stocked = InvHelper.countTotalItemsIn(container, wanted, InvFilter.STRICT);
            if (stocked <= amount) {
                amount = stocked - 1;
            }
        }
        amount = Math.min(amount, golem.hands().room(wanted));
        if (amount <= 0) {
            return;
        }
        ItemStack taken = InvHelper.removeStackFrom(container, wanted.copyWithCount(amount), InvFilter.STRICT, false);
        if (taken.isEmpty()) {
            return;
        }
        ItemStack unheld = golem.hands().hold(taken);
        if (!unheld.isEmpty()) {
            pushBack(level, at, container, unheld);
        }
        HandlingSound.play(golem, HandlingSound.HIGH);
        golem.addRankXp(COLLECT_XP);
        golem.swingArm();
        postDelivery(level, seal, task, request);
    }

    private static void pushBack(ServerLevel level, SealPos at, ResourceHandler<ItemResource> container, ItemStack unheld) {
        ItemStack refused = InvHelper.insertStack(container, unheld, false);
        LooseItems.spawn(level, LooseItems.inFrontOf(at.pos(), at.face()), refused, towards(at.face().getOpposite()));
    }

    private static void postDelivery(ServerLevel level, ISealEntity seal, Task collection, ProvisionRequest request) {
        Entity receiver = request.getEntity();
        BlockPos block = request.getPos();
        Task delivery;
        if (receiver != null) {
            delivery = Task.onEntity(seal.pos(), receiver);
            delivery.setData(Leg.TO_ENTITY.ordinal());
        } else if (block != null) {
            delivery = Task.atBlock(seal.pos(), block);
            delivery.setData(Leg.TO_BLOCK.ordinal());
        } else {
            return;
        }
        delivery.setPriority(collection.priority());
        delivery.setLife(LONG_LIFE);
        GolemHelper.addGolemTask(level, delivery);
        request.link(delivery);
    }

    private static void deliver(ServerLevel level, IGolemAPI golem, ProvisionRequest request) {
        ItemStack wanted = request.getStack();
        ItemStack given = golem.hands().release(wanted.copy());
        if (given.isEmpty()) {
            return;
        }
        int shortfall = wanted.getCount() - given.getCount();
        Entity receiver = request.getEntity();
        BlockPos block = request.getPos();
        Direction side = request.getSide();
        if (receiver != null) {
            if (shortfall > 0) {
                GolemHelper.requestProvisioning(level, receiver, wanted.copyWithCount(shortfall));
            }
            InvHelper.dropItemAtEntity(level, given, receiver);
        } else if (block != null && side != null) {
            if (shortfall > 0) {
                GolemHelper.requestProvisioning(level, block, side, wanted.copyWithCount(shortfall));
            }
            putAtBlock(level, golem, block, side, given);
        } else {
            LooseItems.giveOrDrop(level, golem, given);
            return;
        }
        HandlingSound.play(golem, HandlingSound.LOW);
        golem.swingArm();
        request.markSpent();
    }

    private static void putAtBlock(ServerLevel level, IGolemAPI golem, BlockPos block, Direction side, ItemStack given) {
        ResourceHandler<ItemResource> inventory = InvHelper.getItemHandlerAt(level, block, side);
        if (inventory != null) {
            ItemStack refused = InvHelper.insertStack(inventory, given, false);
            if (!refused.isEmpty()) {
                LooseItems.giveOrDrop(level, golem, refused);
            }
            return;
        }
        boolean solid = level.getBlockState(block).isCollisionShapeFullBlock(level, block);
        Vec3 spot = solid ? LooseItems.inFrontOf(block, side) : Vec3.atCenterOf(block);
        LooseItems.spawn(level, spot, given, towards(side.getOpposite()));
    }

    private static Vec3 towards(Direction direction) {
        return new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ()).scale(PUSH_SPEED);
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request != null && request.getLinkedTask() == task) {
            request.unlink();
        }
    }

    private static @Nullable BlockPos destinationOf(ProvisionRequest request) {
        ISealEntity requester = request.getSeal();
        if (requester != null) {
            return requester.pos().pos();
        }
        Entity receiver = request.getEntity();
        if (receiver != null) {
            return receiver.blockPosition();
        }
        return request.getPos();
    }

    private enum Leg {
        COLLECT, TO_ENTITY, TO_BLOCK;

        private static Leg of(Task task) {
            Leg[] legs = values();
            int index = task.data();
            return index >= 0 && index < legs.length ? legs[index] : COLLECT;
        }
    }
}
