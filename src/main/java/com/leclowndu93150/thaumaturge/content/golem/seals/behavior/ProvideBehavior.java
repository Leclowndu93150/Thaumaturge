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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class ProvideBehavior implements ISealBehavior {
    public static final SealSetting SINGLE_ITEM = new SealSetting("psing", "golem.prop.single", false);
    public static final SealSetting LEAVE_ONE = new SealSetting("pleave", "golem.prop.leave", false);

    private static final int STAGGER = 88;
    private static final int SCAN_PERIOD = 20;
    private static final int TIDY_PERIOD = 100;
    private static final double SERVICE_RANGE_SQR = 4096.0;
    private static final byte UNSEALED_PRIORITY = 5;
    private static final short SEAL_ERRAND_LIFESPAN = 10;
    private static final short DELIVERY_LIFESPAN = 31000;
    private static final int COLLECT = 0;
    private static final int DELIVER_TO_ENTITY = 1;
    private static final int DELIVER_TO_BLOCK = 2;

    private final SealClock clock = new SealClock(STAGGER);

    public static boolean supplies(ISealEntity seal, ItemStack stack) {
        ISealFilter filter = seal.filter().orElseThrow();
        return InvHelper.matchesFilters(filter.stacks(), filter.isBlacklist(), stack, ItemMatchSettings.of(seal));
    }

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        List<ProvisionRequest> queue = GolemHelper.getProvisionRequests(level);
        if (clock.at(TIDY_PERIOD)) {
            queue.removeIf(request -> isStale(level, request));
        }
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        ResourceHandler<ItemResource> stock = InvHelper.getItemHandlerAt(level, seal.pos().pos(), seal.pos().face());
        if (stock == null) {
            return;
        }
        queue.removeIf(ProvisionRequest::isInvalid);
        int reserve = seal.setting(LEAVE_ONE) ? 1 : 0;
        for (ProvisionRequest request : queue) {
            if (request.isInvalid() || request.getLinkedTask() != null || !inServiceRange(seal, request) || !offers(seal, request.getStack())
                    || InvHelper.countTotalItemsIn(stock, request.getStack(), InvHelper.InvFilter.STRICT) <= reserve) {
                continue;
            }
            Task errand = Task.atBlock(seal.pos(), seal.pos().pos());
            errand.setPriority(request.getSeal() != null ? request.getSeal().priority() : UNSEALED_PRIORITY);
            errand.setLifespan(request.getSeal() != null ? SEAL_ERRAND_LIFESPAN : DELIVERY_LIFESPAN);
            link(level, errand, request);
            return;
        }
    }

    private static boolean isStale(Level level, ProvisionRequest request) {
        Task linked = request.getLinkedTask();
        return request.isInvalid() || request.getTimeout() < level.getGameTime() || linked != null && (linked.isSuspended() || linked.isCompleted());
    }

    private static boolean offers(ISealEntity seal, ItemStack stack) {
        ISealFilter filter = seal.filter().orElseThrow();
        return !InvHelper.findFirstMatchFromFilter(filter.stacks(), filter.limits(), filter.isBlacklist(), List.of(stack), ItemMatchSettings.of(seal)).isEmpty();
    }

    private static void link(ServerLevel level, Task task, ProvisionRequest request) {
        TaskBoard.of(level).post(task);
        request.setLinkedTask(task);
        task.linkProvision(request);
    }

    private static boolean inServiceRange(ISealEntity seal, ProvisionRequest request) {
        BlockPos here = seal.pos().pos();
        if (request.getSeal() != null) {
            return request.getSeal().pos().pos().distSqr(here) < SERVICE_RANGE_SQR;
        }
        if (request.getEntity() != null) {
            return here.distToCenterSqr(request.getEntity().position()) < SERVICE_RANGE_SQR;
        }
        return request.getPos() != null && request.getPos().distSqr(here) < SERVICE_RANGE_SQR;
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request != null) {
            if (task.data() == COLLECT) {
                collect(level, seal, golem, task, request);
            } else {
                deliver(level, golem, task, request);
            }
        }
        task.suspend();
        return true;
    }

    private void collect(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task, ProvisionRequest request) {
        SealPos at = seal.pos();
        ResourceHandler<ItemResource> stock = InvHelper.getItemHandlerAt(level, at.pos(), at.face());
        if (stock == null) {
            return;
        }
        ItemStack order = request.getStack().copyWithCount(seal.setting(SINGLE_ITEM) ? 1 : request.getStack().getCount());
        if (!order.isEmpty() && seal.setting(LEAVE_ONE)) {
            int stored = InvHelper.countTotalItemsIn(stock, order, InvHelper.InvFilter.STRICT);
            if (stored <= order.getCount()) {
                order.setCount(stored - 1);
            }
        }
        int room = order.isEmpty() ? 0 : golem.hands().room(order);
        if (room <= 0) {
            return;
        }
        ItemStack spill = golem.hands().hold(InvHelper.removeStackFrom(stock, InvHelper.copyLimitedStack(order, room), InvHelper.InvFilter.STRICT, false));
        if (!spill.isEmpty()) {
            InvHelper.ejectStackAt(level, at.pos().relative(at.face()), at.face().getOpposite(), spill);
        }
        HandlingSound.play(golem, HandlingSound.HIGH);
        golem.addRankXp(1);
        golem.swingArm();
        dispatchDelivery(level, task, request);
    }

    private static void dispatchDelivery(ServerLevel level, Task errand, ProvisionRequest request) {
        Entity receiver = request.getEntity();
        BlockPos dock = request.getPos();
        if (receiver == null && dock == null) {
            return;
        }
        Task delivery = receiver != null ? Task.onEntity(errand.origin(), receiver) : Task.atBlock(errand.origin(), dock);
        delivery.setPriority(errand.priority());
        delivery.setData(receiver != null ? DELIVER_TO_ENTITY : DELIVER_TO_BLOCK);
        delivery.setLifespan(DELIVERY_LIFESPAN);
        link(level, delivery, request);
    }

    private static void deliver(ServerLevel level, IGolemAPI golem, Task task, ProvisionRequest request) {
        ItemStack ordered = request.getStack();
        ItemStack brought = golem.hands().release(ordered);
        boolean toEntity = task.data() == DELIVER_TO_ENTITY;
        if (brought.getCount() < ordered.getCount()) {
            ItemStack shortfall = ordered.copyWithCount(ordered.getCount() - brought.getCount());
            if (toEntity) {
                GolemHelper.requestProvisioning(level, request.getEntity(), shortfall);
            } else {
                GolemHelper.requestProvisioning(level, request.getPos(), request.getSide(), shortfall);
            }
        }
        if (toEntity) {
            InvHelper.dropItemAtEntity(level, brought, request.getEntity());
        } else {
            ItemStack refused = InvHelper.ejectStackAt(level, request.getPos().relative(request.getSide()), request.getSide().getOpposite(), brought, true);
            if (!refused.isEmpty()) {
                golem.hands().hold(refused);
            }
        }
        HandlingSound.play(golem, HandlingSound.LOW);
        golem.swingArm();
        request.setInvalid(true);
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request == null || !(golem.asEntity() instanceof Mob body) || !destinationInHome(body, request) || !SealAccess.allows(request.getSeal(), golem)) {
            return false;
        }
        if (task.data() == COLLECT) {
            return !golem.hands().holds(request.getStack()) && golem.hands().canTake(request.getStack(), true);
        }
        return golem.hands().holds(request.getStack());
    }

    private static boolean destinationInHome(Mob body, ProvisionRequest request) {
        return request.getSeal() != null && body.isWithinHome(request.getSeal().pos().pos()) || request.getEntity() != null && body.isWithinHome(request.getEntity().blockPosition())
                || request.getPos() != null && body.isWithinHome(request.getPos());
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        ProvisionRequest request = task.linkedProvision();
        if (request != null) {
            request.setLinkedTask(null);
        }
        task.linkProvision(null);
    }
}
