package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandoff;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class CollectBehavior implements ISealBehavior {
    private static final int STAGGER = 100;
    private static final int SCAN_PERIOD = 5;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<Integer> claimedItems = new TaskLedger<>();

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, SealArea.bounds(seal))) {
            if (isLoose(item) && !claimedItems.tracks(item.getId()) && wanted(seal, item.getItem())) {
                Task task = Task.onEntity(seal.pos(), item);
                task.setPriority(seal.priority());
                TaskBoard.of(level).post(task);
                claimedItems.record(task, item.getId());
                break;
            }
        }
        claimedItems.dropValues(id -> {
            Entity entity = level.getEntity(id);
            return entity == null || !entity.isAlive();
        });
    }

    private static boolean isLoose(ItemEntity item) {
        return item.onGround() && !item.hasPickUpDelay() && !item.getItem().isEmpty();
    }

    private static boolean wanted(ISealEntity seal, ItemStack stack) {
        ISealFilter filter = seal.filter().orElseThrow();
        return !InvHelper.findFirstMatchFromFilter(
                        filter.stacks(),
                        filter.limits(),
                        filter.isBlacklist(),
                        List.of(stack),
                        ItemMatchSettings.of(seal))
                .isEmpty();
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ItemEntity item = claimedItem(level, task);
        if (item != null && !item.getItem().isEmpty() && wanted(seal, item.getItem())) {
            ItemStack leftover = golem.hands().hold(item.getItem());
            if (leftover.isEmpty()) {
                item.discard();
            } else {
                item.setItem(leftover);
            }
            HandlingSound.play(golem, HandlingSound.HIGH);
            golem.swingArm();
        }
        task.end();
        claimedItems.forget(task);
        TaskHandoff.continueWith(level, golem, claimedItems::has);
        return true;
    }

    private @Nullable ItemEntity claimedItem(Level level, Task task) {
        Integer id = claimedItems.get(task);
        return id != null && level.getEntity(id) instanceof ItemEntity item ? item : null;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ItemEntity item = claimedItem(golem.level(), task);
        if (item == null || item.getItem().isEmpty()) {
            return false;
        }
        if (!item.isAlive()) {
            task.end();
            return false;
        }
        return golem.hands().canTake(item.getItem(), true);
    }
}
