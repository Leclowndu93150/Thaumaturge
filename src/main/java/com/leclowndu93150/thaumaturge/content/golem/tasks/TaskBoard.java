package com.leclowndu93150.thaumaturge.content.golem.tasks;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.ProvisionRequest;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class TaskBoard {
    private static final int MAX_TASKS = 10000;
    private static final double PRIORITY_WEIGHT = 256.0;
    private static final double BLOCK_CENTER = 0.5;
    private static final int LIFE_DECAY = 1;

    private final ConcurrentHashMap<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final List<ProvisionRequest> wants = new CopyOnWriteArrayList<>();
    private final AtomicInteger lastId = new AtomicInteger();

    public TaskBoard() {}

    public static TaskBoard of(Level level) {
        return level.getData(TTAttachments.GOLEM_TASKS);
    }

    public List<ProvisionRequest> wants() {
        return wants;
    }

    public void post(Task task) {
        if (tasks.size() > MAX_TASKS) {
            tasks.keySet().stream().findFirst().ifPresent(tasks::remove);
        }
        int id = lastId.incrementAndGet();
        task.assignId(id);
        tasks.put(task.id(), task);
    }

    @Nullable
    public Task find(int id) {
        return tasks.get(id);
    }

    public boolean isLive(int id) {
        return tasks.containsKey(id);
    }

    public void endAllFrom(SealPos seal) {
        tasks.values().stream().filter(task -> seal.equals(task.origin())).forEach(Task::end);
    }

    public void cancelFor(ServerLevel level, EntityThaumaturgeGolem golem) {
        for (Task task : List.copyOf(tasks.values())) {
            if (task != golem.activeJob() && !golem.getUUID().equals(task.assignedGolem())) {
                continue;
            }
            tasks.remove(task.id());
            notifySuspended(level, task);
            if (task.linkedProvision() != null) {
                task.linkedProvision().unlink();
            }
            task.release();
            task.end();
        }
    }

    public List<Task> openBlockTasks(@Nullable UUID golemId, Entity golem) {
        return rankedOpen(golemId, golem, task -> !task.isEntityTask());
    }

    public List<Task> openEntityTasks(@Nullable UUID golemId, Entity golem) {
        return rankedOpen(golemId, golem, this::hasLiveTarget);
    }

    private List<Task> rankedOpen(@Nullable UUID golemId, Entity golem, Predicate<Task> kind) {
        return tasks.values().stream().filter(task -> isOpenFor(task, golemId) && kind.test(task)).map(task -> new ScoredTask(task, score(golem, task)))
                .sorted(Comparator.comparingDouble(ScoredTask::score)).map(ScoredTask::task).toList();
    }

    private boolean hasLiveTarget(Task task) {
        if (!task.isEntityTask()) {
            return false;
        }
        Entity target = task.entity();
        boolean gone = target == null || !target.isAlive() || target.isRemoved();
        if (gone) {
            task.end();
        }
        return !gone;
    }

    private static boolean isOpenFor(Task task, @Nullable UUID golemId) {
        UUID assigned = task.assignedGolem();
        return !task.isClaimed() && (assigned == null || golemId == null || assigned.equals(golemId));
    }

    private static double score(Entity golem, Task task) {
        BlockPos pos = task.pos();
        double distance = golem.distanceToSqr(pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER);
        return distance - task.priority() * PRIORITY_WEIGHT;
    }

    public static void attempt(ServerLevel level, Task task, IGolemAPI golem) {
        if (task.isCompleted() || task.isEnded()) {
            return;
        }
        SealPos origin = task.origin();
        ISealEntity seal = origin == null ? null : SealHandler.lookup(level, origin);
        boolean succeeded = seal == null || seal.behavior().completeTask(level, seal, golem, task);
        task.recordAttempt(succeeded);
    }

    public void sweep(ServerLevel level) {
        for (Task task : tasks.values()) {
            if (!task.isEnded() && task.life() > 0) {
                task.setLife(task.life() - LIFE_DECAY);
                continue;
            }
            tasks.remove(task.id());
            notifySuspended(level, task);
            task.end();
        }
    }

    private static void notifySuspended(ServerLevel level, Task task) {
        SealPos origin = task.origin();
        ISealEntity seal = origin == null ? null : SealHandler.lookup(level, origin);
        if (seal != null) {
            seal.behavior().onTaskSuspended(level, seal, task);
        }
    }

    private record ScoredTask(Task task, double score) {
    }
}
