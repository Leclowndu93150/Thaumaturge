package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.StackFilter;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandoff;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CollectBehavior implements ISealBehavior {
    private static final int STAGGER = 53;
    private static final int SCAN_PERIOD = 5;
    private static final int PURGE_PERIOD = 100;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<UUID> claimedItems = new TaskLedger<>();

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        int now = clock.advance();
        if (now % PURGE_PERIOD == 0) {
            claimedItems.dropFinished(level);
        }
        if (now % SCAN_PERIOD != 0) {
            return;
        }
        var bounds = SealArea.bounds(seal);
        var items =
                LevelSpaces.entitiesIn(level, ItemEntity.class, seal.pos().pos(), bounds, EntitySelector.NO_SPECTATORS);
        if (Thaumaturge.LOGGER.isDebugEnabled()) {
            Thaumaturge.LOGGER.debug(
                    "[golem-task] tick={} collect seal={} localBounds={} worldBounds={} items={}",
                    level.getGameTime(),
                    seal.pos(),
                    bounds,
                    LevelSpaces.worldBox(level, seal.pos().pos(), bounds),
                    items.size());
        }
        for (ItemEntity item : items) {
            if (Thaumaturge.LOGGER.isDebugEnabled()) {
                Thaumaturge.LOGGER.debug(
                        "[golem-task] tick={} item={} pos={} alive={} grounded={} pickupDelay={} empty={} tracked={}",
                        level.getGameTime(),
                        item.getUUID(),
                        item.position(),
                        item.isAlive(),
                        item.onGround(),
                        item.hasPickUpDelay(),
                        item.getItem().isEmpty(),
                        claimedItems.tracks(item.getUUID()));
            }
            if (isLoose(item) && !claimedItems.tracks(item.getUUID()) && wanted(seal, item.getItem())) {
                Task task = Task.onEntity(seal.pos(), item);
                task.setPriority(seal.priority());
                GolemDispatch.postTask(level, task);
                claimedItems.record(task, item.getUUID());
                Thaumaturge.LOGGER.debug(
                        "[golem-task] tick={} posted task={} seal={} item={} pos={}",
                        level.getGameTime(),
                        task.id(),
                        seal.pos(),
                        item.getUUID(),
                        item.position());
                break;
            } else if (Thaumaturge.LOGGER.isDebugEnabled() && isLoose(item) && !claimedItems.tracks(item.getUUID())) {
                Thaumaturge.LOGGER.debug(
                        "[golem-task] tick={} item={} rejected by collect filter", level.getGameTime(), item.getUUID());
            }
        }
    }

    private static boolean isLoose(ItemEntity item) {
        return item.isAlive()
                && item.onGround()
                && !item.hasPickUpDelay()
                && !item.getItem().isEmpty();
    }

    private static boolean wanted(ISealEntity seal, ItemStack stack) {
        ISealFilter filter = seal.filter().orElseThrow();
        return !new StackFilter(filter.stacks(), filter.limits(), filter.isBlacklist(), ItemMatchSettings.of(seal))
                .firstAccepted(List.of(stack))
                .isEmpty();
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ItemEntity item = claimedItem(task);
        if (item != null && !item.getItem().isEmpty() && wanted(seal, item.getItem())) {
            ItemStack leftover = golem.hands().hold(item.getItem());
            if (leftover.isEmpty()) {
                item.discard();
            } else {
                item.setItem(leftover);
            }
            HandlingSound.play(golem, HandlingSound.HIGH);
            golem.animateSwing();
        }
        task.end();
        claimedItems.forget(task);
        TaskHandoff.continueWith(level, golem, candidate -> seal.pos().equals(candidate.origin()));
        return true;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        claimedItems.forget(task);
    }

    private @Nullable ItemEntity claimedItem(Task task) {
        UUID claimedId = claimedItems.get(task);
        return task.entity() instanceof ItemEntity item && item.getUUID().equals(claimedId) ? item : null;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ItemEntity item = claimedItem(task);
        if (item == null || item.getItem().isEmpty()) {
            task.end();
            return false;
        }
        if (!item.isAlive()) {
            task.end();
            return false;
        }
        boolean canTake = golem.hands().canTake(item.getItem(), true);
        if (!canTake && Thaumaturge.LOGGER.isDebugEnabled()) {
            Thaumaturge.LOGGER.debug(
                    "[golem-task] tick={} golem={} task={} collect rejected=hands_full item={} stack={} hands={}",
                    golem.level().getGameTime(),
                    golem.asEntity().getUUID(),
                    task.id(),
                    item.getUUID(),
                    item.getItem(),
                    golem.hands().contents());
        }
        return canTake;
    }
}
