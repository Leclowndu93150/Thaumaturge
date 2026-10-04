package com.leclowndu93150.thaumaturge.content.golem.tasks;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class TaskBoard {
    private static final int CAPACITY = 10000;
    private static final double PRIORITY_WEIGHT = 256.0;

    private Map<Integer, Task> pinned = new ConcurrentHashMap<>();
    private final List<ProvisionRequest> wants = new CopyOnWriteArrayList<>();
    private int lastId;

    public static TaskBoard of(Level level) {
        return level.getData(TCAttachments.GOLEM_TASKS);
    }

    public List<ProvisionRequest> wants() {
        return wants;
    }

    public void post(Task task) {
        task.assignId(++lastId);
        if (pinned.size() > CAPACITY) {
            Iterator<Task> oldest = pinned.values().iterator();
            oldest.next();
            oldest.remove();
        }
        pinned.put(task.id(), task);
    }

    public @Nullable Task find(int id) {
        return pinned.get(id);
    }

    public boolean isLive(int id) {
        return pinned.containsKey(id);
    }

    public void suspendAllFrom(SealPos origin) {
        pinned.values().stream().filter(task -> origin.equals(task.origin())).forEach(Task::suspend);
    }

    public List<Task> openBlockTasks(@Nullable UUID golemId, Entity golem) {
        return open(golemId, golem, task -> !task.isEntityTask());
    }

    public List<Task> openEntityTasks(@Nullable UUID golemId, Entity golem) {
        return open(golemId, golem, TaskBoard::hasLiveEntity);
    }

    private static boolean hasLiveEntity(Task task) {
        if (!task.isEntityTask()) {
            return false;
        }
        Entity entity = task.entity();
        if (entity == null || !entity.isAlive()) {
            task.suspend();
            return false;
        }
        return true;
    }

    private List<Task> open(@Nullable UUID golemId, Entity golem, Predicate<Task> kind) {
        return pinned.values().stream().filter(task -> !task.isReserved() && (golemId == null || task.claimant() == null || golemId.equals(task.claimant()))).filter(kind)
                .sorted(Comparator.comparingDouble(task -> task.pos().distToCenterSqr(golem.position()) - task.priority() * PRIORITY_WEIGHT)).toList();
    }

    public static void attempt(ServerLevel level, Task task, IGolemAPI golem) {
        if (task.isCompleted() || task.isSuspended()) {
            return;
        }
        ISealEntity seal = SealHandler.getSealEntity(level, task.origin());
        task.recordAttempt(seal == null || seal.behavior().completeTask(level, seal, golem, task));
    }

    public void sweep(ServerLevel level) {
        Map<Integer, Task> survivors = new ConcurrentHashMap<>();
        for (Task task : pinned.values()) {
            if (!task.isSuspended() && task.lifespan() > 0) {
                task.setLifespan((short) (task.lifespan() - 1));
                survivors.put(task.id(), task);
                continue;
            }
            ISealEntity seal = SealHandler.getSealEntity(level, task.origin());
            if (seal != null) {
                seal.behavior().onTaskSuspended(level, seal, task);
            }
        }
        pinned = survivors;
    }
}
