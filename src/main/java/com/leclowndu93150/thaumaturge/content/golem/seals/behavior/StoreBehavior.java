package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.ItemMatch;
import com.leclowndu93150.thaumaturge.api.items.StackFilter;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jspecify.annotations.Nullable;

public final class StoreBehavior implements ISealBehavior {
    public static final SealSetting ONLY_EXISTING =
            new SealSetting("only_existing", "gui.thaumaturge.seal.setting.exist", false).readingAlso("pexist");

    private static final int STAGGER = 47;
    private static final int SCAN_PERIOD = 20;
    private static final double NEARBY_RANGE = 1.5;
    private static final double TOSS_SPREAD = 0.02;

    private final SealClock clock = new SealClock(STAGGER);
    private int pendingTask = Integer.MIN_VALUE;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        Task pending = TaskBoard.of(level).find(pendingTask);
        if (pending == null || pending.isClaimed() || pending.isEnded() || pending.isCompleted()) {
            offerWork(level, seal);
        }
    }

    private void offerWork(ServerLevel level, ISealEntity seal) {
        Task task = Task.atBlock(seal.pos(), seal.pos().pos());
        task.setPriority(seal.priority());
        GolemDispatch.postTask(level, task);
        pendingTask = task.id();
        Thaumaturge.LOGGER.debug(
                "[golem-task] tick={} posted store task={} seal={}", level.getGameTime(), task.id(), seal.pos());
    }

    @Override
    public void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (!seal.isStoppedByRedstone(level)) {
            offerWork(level, seal);
        }
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ItemMatch matching = ItemMatchSettings.of(seal);
        InvHelper.FilterMatch carried = carriedMatch(seal, golem, matching);
        if (!carried.stack().isEmpty()) {
            SealPos at = seal.pos();
            IItemHandler inventory = InvHelper.handlerAt(level, at.pos(), at.face());
            int amount = carried.stack().getCount();
            if (limited(seal, carried)) {
                int present = inventory == null
                        ? InvHelper.countDropped(level, at.pos(), carried.stack(), NEARBY_RANGE, matching)
                        : InvHelper.count(inventory, carried.stack(), matching);
                amount = Math.max(0, carried.sizeLimit() - present);
            }
            if (amount > 0) {
                ItemStack load = golem.hands().release(carried.stack().copyWithCount(amount));
                int delivered = deliver(level, golem, at, inventory, load);
                if (delivered > 0) {
                    HandlingSound.play(golem, HandlingSound.LOW);
                    golem.grantRankExperience(1);
                    golem.animateSwing();
                }
            }
        }
        task.end();
        return true;
    }

    private static int deliver(
            Level level, IGolemAPI golem, SealPos at, @Nullable IItemHandler inventory, ItemStack load) {
        if (load.isEmpty()) {
            return 0;
        }
        if (inventory == null) {
            Vec3 spot = Vec3.atCenterOf(at.pos()).relative(at.face(), 1.0);
            ItemEntity dropped = new ItemEntity(level, spot.x, spot.y, spot.z, load);
            Vec3 toss = tossVelocity(level.getRandom());
            dropped.setDeltaMovement(toss.x, toss.y, toss.z);
            if (level.addFreshEntity(dropped)) {
                return load.getCount();
            }
            golem.hands().hold(load);
            return 0;
        }
        int offered = load.getCount();
        ItemStack refused = InvHelper.insertStack(inventory, load, false);
        int delivered = placedCount(offered, refused);
        golem.hands().hold(refused);
        return delivered;
    }

    static int placedCount(int offered, ItemStack refused) {
        return Math.max(0, offered - (refused.isEmpty() ? 0 : refused.getCount()));
    }

    static Vec3 tossVelocity(RandomSource random) {
        return new Vec3(random.triangle(0.0, TOSS_SPREAD), 0.0, random.triangle(0.0, TOSS_SPREAD));
    }

    private static InvHelper.FilterMatch carriedMatch(ISealEntity seal, IGolemAPI golem, ItemMatch matching) {
        ISealFilter filter = seal.filter().orElseThrow();
        return new StackFilter(filter.stacks(), filter.limits(), filter.isBlacklist(), matching)
                .firstAcceptedWithLimit(golem.hands().contents());
    }

    private static boolean limited(ISealEntity seal, InvHelper.FilterMatch match) {
        return seal.filter().orElseThrow().usesLimits() && match.sizeLimit() > 0;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ItemMatch matching = ItemMatchSettings.of(seal);
        InvHelper.FilterMatch carried = carriedMatch(seal, golem, matching);
        if (carried.stack().isEmpty()) {
            if (Thaumaturge.LOGGER.isDebugEnabled()) {
                ISealFilter filter = seal.filter().orElseThrow();
                Thaumaturge.LOGGER.debug(
                        "[golem-task] tick={} golem={} task={} store seal={} rejected=no_matching_carried_item hands={} filter={} blacklist={} limits={} match={}",
                        golem.level().getGameTime(),
                        golem.asEntity().getUUID(),
                        task.id(),
                        seal.pos(),
                        golem.hands().contents(),
                        filter.stacks(),
                        filter.isBlacklist(),
                        filter.limits(),
                        matching);
            }
            return false;
        }
        SealPos at = seal.pos();
        Level level = golem.level();
        IItemHandler inventory = InvHelper.handlerAt(level, at.pos(), at.face());
        if (inventory == null) {
            if (!limited(seal, carried)) return true;
            int present = InvHelper.countDropped(level, at.pos(), carried.stack(), NEARBY_RANGE, matching);
            if (present >= carried.sizeLimit()) {
                Thaumaturge.LOGGER.debug(
                        "[golem-task] tick={} golem={} task={} store seal={} rejected=dropped_limit item={} present={} limit={}",
                        level.getGameTime(),
                        golem.asEntity().getUUID(),
                        task.id(),
                        at,
                        carried.stack(),
                        present,
                        carried.sizeLimit());
                return false;
            }
            return true;
        }
        if (seal.setting(ONLY_EXISTING) && InvHelper.count(inventory, carried.stack(), matching) <= 0) {
            Thaumaturge.LOGGER.debug(
                    "[golem-task] tick={} golem={} task={} store seal={} rejected=only_existing item={}",
                    level.getGameTime(),
                    golem.asEntity().getUUID(),
                    task.id(),
                    at,
                    carried.stack());
            return false;
        }
        if (!InvHelper.acceptsAny(level, at.pos(), at.face(), carried.stack())) {
            if (Thaumaturge.LOGGER.isDebugEnabled()) {
                java.util.List<ItemStack> slots = new java.util.ArrayList<>();
                for (int i = 0; i < inventory.getSlots(); i++) slots.add(inventory.getStackInSlot(i));
                Thaumaturge.LOGGER.debug(
                        "[golem-task] tick={} golem={} task={} store seal={} rejected=inventory_refused item={} slots={}",
                        level.getGameTime(),
                        golem.asEntity().getUUID(),
                        task.id(),
                        at,
                        carried.stack(),
                        slots);
            }
            return false;
        }
        if (!limited(seal, carried)) return true;
        int present = InvHelper.count(inventory, carried.stack(), matching);
        if (present >= carried.sizeLimit()) {
            Thaumaturge.LOGGER.debug(
                    "[golem-task] tick={} golem={} task={} store seal={} rejected=inventory_limit item={} present={} limit={}",
                    level.getGameTime(),
                    golem.asEntity().getUUID(),
                    task.id(),
                    at,
                    carried.stack(),
                    present,
                    carried.sizeLimit());
            return false;
        }
        return true;
    }
}
