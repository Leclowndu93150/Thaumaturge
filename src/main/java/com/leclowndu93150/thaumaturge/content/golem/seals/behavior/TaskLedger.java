package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

final class TaskLedger<T> {
    private final Map<Integer, T> entries = new HashMap<>();

    void record(Task task, T value) {
        entries.put(task.id(), value);
    }

    @Nullable
    T get(Task task) {
        return entries.get(task.id());
    }

    boolean has(Task task) {
        return entries.containsKey(task.id());
    }

    boolean tracks(T value) {
        return entries.containsValue(value);
    }

    void forget(Task task) {
        entries.remove(task.id());
    }

    void dropFinished(Level level) {
        entries.keySet().removeIf(id -> !TaskBoard.of(level).isLive(id));
    }

    void dropValues(Predicate<T> stale) {
        entries.values().removeIf(stale);
    }
}
