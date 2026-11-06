package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.SupplyOrder;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.ItemMatch;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
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
            new SealSetting("single_item", "gui.thaumaturge.seal.setting.single", false).readingAlso("psing");
    public static final SealSetting LEAVE_ONE =
            new SealSetting("leave_one", "gui.thaumaturge.seal.setting.leave", false).readingAlso("pleave");

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
        ItemMatch matching = ItemMatchSettings.of(seal);
        boolean listed = filter.get().stacks().stream().anyMatch(slot -> !slot.isEmpty() && matching.test(slot, stack));
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
        GolemDispatch.supplyQueue(level)
                .removeIf(request -> request.isSpent()
                        || time > request.expiresAt()
                        || isServedOut(request.servingTask())
                        || sealGone(level, request));
    }

    private static boolean isServedOut(@Nullable Task task) {
        return task != null && (task.isEnded() || task.isCompleted());
    }

    private static boolean sealGone(ServerLevel level, SupplyOrder request) {
        ISealEntity requester = request.requestingSeal();
        return requester != null && GolemDispatch.sealAt(level, requester.pos()).isEmpty();
    }

    private static void scan(ServerLevel level, ISealEntity seal) {
        SealPos at = seal.pos();
        IItemHandler store = InvHelper.handlerAt(level, at.pos(), at.face());
        if (store == null) {
            return;
        }
        List<SupplyOrder> queue = GolemDispatch.supplyQueue(level);
        queue.removeIf(SupplyOrder::isSpent);
        int minimumStock = seal.setting(LEAVE_ONE) ? 2 : 1;
        for (SupplyOrder request : queue) {
            if (request.isSpent() || request.servingTask() != null) {
                continue;
            }
            BlockPos destination = destination(request);
            if (destination == null
                    || LevelSpaces.distanceSqr(level, destination, at.pos()) >= SERVICE_RANGE * SERVICE_RANGE
                    || !supplies(seal, request.wanted())
                    || InvHelper.count(store, request.wanted(), ItemMatch.EXACT) < minimumStock) {
                continue;
            }
            ISealEntity requester = request.requestingSeal();
            Task collection = Task.atBlock(at, at.pos());
            collection.setPriority(requester != null ? requester.priority() : OPEN_REQUEST_PRIORITY);
            collection.setLife(requester != null ? SEAL_COLLECTION_LIFE : CARRY_LIFE);
            collection.setData(LEG_COLLECT);
            post(level, collection, request);
            return;
        }
    }

    private static void post(ServerLevel level, Task task, SupplyOrder request) {
        GolemDispatch.postTask(level, task);
        request.assign(task);
    }

    private static @Nullable BlockPos destination(SupplyOrder request) {
        if (request.requestingSeal() != null) {
            return request.requestingSeal().pos().pos();
        }
        if (request.recipient() != null) {
            return request.recipient().blockPosition();
        }
        return request.destinationBlock();
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        SupplyOrder request = task.servedOrder();
        if (request == null) {
            return false;
        }
        BlockPos destination = destination(request);
        if (destination == null || !(golem.asEntity() instanceof Mob body) || !body.isWithinRestriction(destination)) {
            return false;
        }
        ItemStack wanted = request.wanted();
        if (legOf(task.data()) != LEG_COLLECT) {
            return golem.hands().holds(wanted);
        }
        return SealAccess.allows(request.requestingSeal(), golem)
                && !golem.hands().holds(wanted)
                && golem.hands().room(wanted) > 0;
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        SupplyOrder request = task.servedOrder();
        if (request != null) {
            if (legOf(task.data()) == LEG_COLLECT) {
                collect(level, seal, golem, task, request);
            } else {
                deliver(level, seal, golem, request);
            }
        }
        task.end();
        return true;
    }

    static int legOf(int data) {
        return data >= LEG_COLLECT && data <= LEG_TO_BLOCK ? data : LEG_COLLECT;
    }

    private static void collect(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task, SupplyOrder request) {
        SealPos at = seal.pos();
        IItemHandler store = InvHelper.handlerAt(level, at.pos(), at.face());
        if (store == null) {
            return;
        }
        ItemStack wanted = request.wanted();
        int amount = seal.setting(SINGLE_ITEM) ? 1 : wanted.getCount();
        if (seal.setting(LEAVE_ONE)) {
            int stocked = InvHelper.count(store, wanted, ItemMatch.EXACT);
            if (stocked <= amount) {
                amount = stocked - 1;
            }
        }
        amount = Math.min(amount, golem.hands().room(wanted));
        if (amount <= 0) {
            return;
        }
        ItemStack taken = InvHelper.extract(store, wanted.copyWithCount(amount), ItemMatch.EXACT, false);
        if (taken.isEmpty()) {
            return;
        }
        ItemStack spill = InvHelper.insertStack(store, golem.hands().hold(taken), false);
        if (!spill.isEmpty()) {
            dropToward(level, at.pos(), at.face(), spill);
        }
        HandlingSound.play(golem, HandlingSound.HIGH);
        golem.grantRankExperience(1);
        golem.animateSwing();
        if (request.requestingSeal() != null) {
            return;
        }
        Entity receiver = request.recipient();
        Task delivery = receiver != null ? Task.onEntity(at, receiver) : Task.atBlock(at, request.destinationBlock());
        delivery.setPriority(task.priority());
        delivery.setLife(CARRY_LIFE);
        delivery.setData(receiver != null ? LEG_TO_ENTITY : LEG_TO_BLOCK);
        post(level, delivery, request);
    }

    private static void deliver(ServerLevel level, ISealEntity seal, IGolemAPI golem, SupplyOrder request) {
        ItemStack wanted = request.wanted();
        ItemStack given = golem.hands().release(wanted);
        int shortfall = wanted.getCount() - given.getCount();
        Entity receiver = request.recipient();
        BlockPos destination = request.destinationBlock();
        Direction face = request.destinationFace();
        if (shortfall > 0) {
            if (receiver != null) {
                GolemDispatch.orderSupply(level, receiver, wanted.copyWithCount(shortfall));
            } else if (destination != null && face != null) {
                GolemDispatch.orderSupply(level, destination, face, wanted.copyWithCount(shortfall));
            }
        }
        if (!given.isEmpty()) {
            if (!hasDeliveryTarget(receiver, destination, face)) {
                ItemStack spill = golem.hands().hold(given);
                if (!spill.isEmpty()) {
                    SealPos provider = seal.pos();
                    dropToward(level, provider.pos(), provider.face(), spill);
                }
            } else if (receiver != null) {
                level.addFreshEntity(new ItemEntity(
                        level,
                        receiver.getX(),
                        receiver.getY() + receiver.getEyeHeight() * HALF,
                        receiver.getZ(),
                        given));
            } else if (destination != null && face != null) {
                handOver(level, golem, destination, face, given);
            }
        }
        HandlingSound.play(golem, HandlingSound.LOW);
        golem.animateSwing();
        request.markSpent();
    }

    private static void handOver(ServerLevel level, IGolemAPI golem, BlockPos pos, Direction side, ItemStack stack) {
        IItemHandler inventory = InvHelper.handlerAt(level, pos, side);
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

    static boolean hasDeliveryTarget(
            @Nullable Entity receiver, @Nullable BlockPos destination, @Nullable Direction face) {
        return receiver != null || destination != null && face != null;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        SupplyOrder request = task.servedOrder();
        if (request != null && task.equals(request.servingTask())) {
            request.release();
        }
    }
}
