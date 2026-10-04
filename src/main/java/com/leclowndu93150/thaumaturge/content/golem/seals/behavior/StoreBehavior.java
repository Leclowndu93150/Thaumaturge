package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class StoreBehavior implements ISealBehavior {
    public static final SealSetting ONLY_EXISTING = new SealSetting("pexist", "golem.prop.exist", false);

    private static final int STAGGER = 50;
    private static final int SCAN_PERIOD = 20;
    private static final double NEARBY_RANGE = 1.5;
    private static final double DROP_DRAG_XZ = 0.2;
    private static final double DROP_DRAG_Y = 0.5;

    private final SealClock clock = new SealClock(STAGGER);
    private int pendingTask = Integer.MIN_VALUE;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        Task pending = TaskBoard.of(level).find(pendingTask);
        if (pending == null || pending.isReserved() || pending.isSuspended() || pending.isCompleted()) {
            offerWork(level, seal);
        }
    }

    private void offerWork(ServerLevel level, ISealEntity seal) {
        Task task = Task.atBlock(seal.pos(), seal.pos().pos());
        task.setPriority(seal.priority());
        TaskBoard.of(level).post(task);
        pendingTask = task.id();
    }

    @Override
    public void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (!seal.isStoppedByRedstone(level)) {
            offerWork(level, seal);
        }
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        InvHelper.InvFilter matching = ItemMatchSettings.of(seal);
        InvHelper.FilterMatch carried = carriedMatch(seal, golem, matching);
        if (!carried.stack().isEmpty()) {
            SealPos at = seal.pos();
            ResourceHandler<ItemResource> inventory = InvHelper.getItemHandlerAt(level, at.pos(), at.face());
            int amount = carried.stack().getCount();
            if (limited(seal, carried)) {
                int present = inventory == null
                        ? InvHelper.countStackInWorld(level, at.pos(), carried.stack(), NEARBY_RANGE, matching)
                        : InvHelper.countTotalItemsIn(inventory, carried.stack(), matching);
                amount = Math.max(0, carried.sizeLimit() - present);
            }
            if (amount > 0) {
                deliver(level, golem, at, inventory, golem.hands().release(carried.stack().copyWithCount(amount)));
                golem.addRankXp(1);
                golem.swingArm();
            }
        }
        task.suspend();
        return true;
    }

    private static void deliver(Level level, IGolemAPI golem, SealPos at, @Nullable ResourceHandler<ItemResource> inventory, ItemStack load) {
        if (inventory == null) {
            Vec3 spot = Vec3.atCenterOf(at.pos()).relative(at.face(), 1.0);
            ItemEntity dropped = new ItemEntity(level, spot.x, spot.y, spot.z, load);
            dropped.setDeltaMovement(dropped.getDeltaMovement().multiply(DROP_DRAG_XZ, DROP_DRAG_Y, DROP_DRAG_XZ));
            level.addFreshEntity(dropped);
        } else {
            golem.hands().hold(InvHelper.insertStack(inventory, load, false));
        }
        HandlingSound.play(golem, HandlingSound.LOW);
    }

    private static InvHelper.FilterMatch carriedMatch(ISealEntity seal, IGolemAPI golem, InvHelper.InvFilter matching) {
        ISealFilter filter = seal.filter().orElseThrow();
        return InvHelper.findFirstMatchFromFilterWithSize(filter.stacks(), filter.limits(), filter.isBlacklist(), golem.hands().contents(), matching);
    }

    private static boolean limited(ISealEntity seal, InvHelper.FilterMatch match) {
        return seal.filter().orElseThrow().usesLimits() && match.sizeLimit() > 0;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        InvHelper.InvFilter matching = ItemMatchSettings.of(seal);
        InvHelper.FilterMatch carried = carriedMatch(seal, golem, matching);
        if (carried.stack().isEmpty()) {
            return false;
        }
        SealPos at = seal.pos();
        Level level = golem.level();
        ResourceHandler<ItemResource> inventory = InvHelper.getItemHandlerAt(level, at.pos(), at.face());
        if (inventory == null) {
            return !limited(seal, carried) || InvHelper.countStackInWorld(level, at.pos(), carried.stack(), NEARBY_RANGE, matching) < carried.sizeLimit();
        }
        if (seal.setting(ONLY_EXISTING) && InvHelper.countTotalItemsIn(inventory, carried.stack(), matching) <= 0) {
            return false;
        }
        if (!InvHelper.hasRoomForSome(level, at.pos(), at.face(), carried.stack())) {
            return false;
        }
        return !limited(seal, carried) || InvHelper.countTotalItemsIn(inventory, carried.stack(), matching) < carried.sizeLimit();
    }
}
