package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandoff;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CollectBehavior implements ISealBehavior {
    private static final int STAGGER = 53;
    private static final int SCAN_PERIOD = 5;
    private static final int PURGE_PERIOD = 100;

    private final SealClock clock = new SealClock(STAGGER);
    private final TaskLedger<UUID> targets = new TaskLedger<>();

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        int now = clock.advance();
        if (now % PURGE_PERIOD == 0) {
            targets.dropFinished(level);
        }
        if (now % SCAN_PERIOD != 0) {
            return;
        }
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, SealArea.bounds(seal))) {
            if (isCollectable(seal, item)) {
                Task task = Task.onEntity(seal.pos(), item);
                task.setPriority(seal.priority());
                GolemHelper.addGolemTask(level, task);
                targets.record(task, item.getUUID());
                return;
            }
        }
    }

    private boolean isCollectable(ISealEntity seal, ItemEntity item) {
        return item.isAlive() && item.onGround() && !item.hasPickUpDelay() && !targets.tracks(item.getUUID()) && ItemMatchSettings.accepts(seal, item.getItem());
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        ItemEntity item = droppedItem(task);
        if (item == null) {
            task.end();
            return false;
        }
        return golem.hands().room(item.getItem()) > 0;
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        ItemEntity item = droppedItem(task);
        if (item != null && ItemMatchSettings.accepts(seal, item.getItem())) {
            pickUp(golem, item);
        }
        task.end();
        targets.forget(task);
        TaskHandoff.continueWith(level, golem, candidate -> seal.pos().equals(candidate.origin()));
        return true;
    }

    @Override
    public void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {
        targets.forget(task);
    }

    private static void pickUp(IGolemAPI golem, ItemEntity item) {
        ItemStack lying = item.getItem();
        int taken = Math.min(golem.hands().room(lying), lying.getCount());
        if (taken <= 0) {
            return;
        }
        golem.hands().hold(lying.copyWithCount(taken));
        ItemStack rest = lying.copyWithCount(lying.getCount() - taken);
        if (rest.isEmpty()) {
            item.discard();
        } else {
            item.setItem(rest);
        }
        HandlingSound.play(golem, HandlingSound.HIGH);
        golem.swingArm();
    }

    private static @Nullable ItemEntity droppedItem(Task task) {
        return task.entity() instanceof ItemEntity item && item.isAlive() && !item.getItem().isEmpty() ? item : null;
    }
}
