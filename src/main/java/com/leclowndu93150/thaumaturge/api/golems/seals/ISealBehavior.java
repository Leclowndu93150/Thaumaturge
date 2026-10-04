package com.leclowndu93150.thaumaturge.api.golems.seals;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.server.level.ServerLevel;

/**
 * The job of a placed seal: which tasks it posts and what happens when a golem arrives.
 *
 * <p>Each placed seal owns one behaviour instance, created by its {@link SealType}, so a behaviour may keep per-placement state such
 * as task caches. State that must survive a restart goes through the type's behaviour codec. Every callback runs on the server
 * thread and receives the placed seal, through which the behaviour reads its filter and settings.
 *
 * @since 1.0.0
 */
public interface ISealBehavior {
    /**
     * Runs every server tick while the seal's chunk is loaded and no redstone signal stops it.
     *
     * @param level the level
     * @param seal  the placed seal
     */
    void tick(ServerLevel level, ISealEntity seal);

    /**
     * Runs when a golem claims one of this seal's tasks, before it starts walking.
     *
     * @param level the level
     * @param seal  the placed seal
     * @param golem the golem
     * @param task  the claimed task
     */
    default void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {}

    /**
     * Runs when the golem reaches the task target. Return false to have the golem stay and try again a moment later, which is how
     * multi-step work such as block breaking advances.
     *
     * @param level the level
     * @param seal  the placed seal
     * @param golem the golem
     * @param task  the task
     * @return whether the work is finished
     */
    boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task);

    /**
     * Asked before a golem claims a task and while it works on it. Trait and ownership gates are already checked by then.
     *
     * @param seal  the placed seal
     * @param golem the golem asking
     * @param task  the task
     * @return whether this golem may do the task now
     */
    boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task);

    /**
     * Runs when the task board drops one of this seal's tasks because it was suspended or expired.
     *
     * @param level the level
     * @param seal  the placed seal
     * @param task  the dropped task
     */
    default void onTaskSuspended(ServerLevel level, ISealEntity seal, Task task) {}

    /**
     * Runs once when the seal is removed from the world.
     *
     * @param level the level
     * @param seal  the seal being removed
     */
    default void onRemoved(ServerLevel level, ISealEntity seal) {}
}
