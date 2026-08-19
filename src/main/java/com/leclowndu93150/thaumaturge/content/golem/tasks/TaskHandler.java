package com.leclowndu93150.thaumaturge.content.golem.tasks;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class TaskHandler {
    private static final int TASK_LIMIT = 10000;
    private static final double PRIORITY_DISTANCE_BONUS = 256.0;

    private TaskHandler() {}

    private static GolemTasks data(Level level) {
        return level.getData(TCAttachments.GOLEM_TASKS);
    }

    public static void addTask(Level level, Task task) {
        GolemTasks data = data(level);
        task.setId(data.nextTaskId());
        Map<Integer, Task> tasks = data.tasks();
        if (tasks.size() > TASK_LIMIT) {
            Iterator<Task> iterator = tasks.values().iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
        tasks.put(task.getId(), task);
    }

    public static @Nullable Task getTask(Level level, int id) {
        return getTasks(level).get(id);
    }

    public static Map<Integer, Task> getTasks(Level level) {
        return data(level).tasks();
    }

    public static List<Task> getBlockTasksSorted(Level level, @Nullable UUID uuid, Entity golem) {
        return getTasksSorted(level, uuid, golem, Task.TYPE_BLOCK);
    }

    public static List<Task> getEntityTasksSorted(Level level, @Nullable UUID uuid, Entity golem) {
        return getTasksSorted(level, uuid, golem, Task.TYPE_ENTITY);
    }

    private static List<Task> getTasksSorted(Level level, @Nullable UUID uuid, Entity golem, byte type) {
        List<Task> out = new ArrayList<>();
        for (Task task : getTasks(level).values()) {
            if (task.isReserved() || task.getType() != type || (uuid != null && task.getGolemUUID() != null && !uuid.equals(task.getGolemUUID()))) {
                continue;
            }
            if (type == Task.TYPE_ENTITY && (task.getEntity() == null || !task.getEntity().isAlive())) {
                task.setSuspended(true);
                continue;
            }
            insertByDistance(out, task, golem);
        }
        return out;
    }

    private static void insertByDistance(List<Task> out, Task task, Entity golem) {
        double score = weightedDistance(task, golem);
        for (int i = 0; i < out.size(); i++) {
            if (score < weightedDistance(out.get(i), golem)) {
                out.add(i, task);
                return;
            }
        }
        out.add(task);
    }

    private static double weightedDistance(Task task, Entity golem) {
        return task.getPos().distToCenterSqr(golem.getX(), golem.getY(), golem.getZ()) - task.getPriority() * PRIORITY_DISTANCE_BONUS;
    }

    public static void completeTask(Task task, EntityThaumaturgeGolem golem) {
        if (task.isCompleted() || task.isSuspended()) {
            return;
        }
        ISealEntity seal = SealHandler.getSealEntity(golem.level(), task.getSealPos());
        if (seal != null) {
            task.setCompletion(seal.getSeal().onTaskCompletion(golem.level(), golem, task));
        } else {
            task.setCompletion(true);
        }
    }

    public static void clearSuspendedOrExpiredTasks(Level level) {
        GolemTasks data = data(level);
        Map<Integer, Task> kept = new ConcurrentHashMap<>();
        for (Task task : data.tasks().values()) {
            if (!task.isSuspended() && task.getLifespan() > 0L) {
                task.setLifespan((short) (task.getLifespan() - 1L));
                kept.put(task.getId(), task);
            } else {
                ISealEntity seal = SealHandler.getSealEntity(level, task.getSealPos());
                if (seal != null) {
                    seal.getSeal().onTaskSuspension(level, task);
                }
            }
        }
        data.replaceTasks(kept);
    }
}
