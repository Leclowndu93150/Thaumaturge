package com.leclowndu93150.thaumaturge.content.golem.tasks;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class TaskHandoff {
    private static final byte CLAIM_EMOTE = 5;

    private TaskHandoff() {}

    public static void assign(EntityThaumaturgeGolem golem, Task task) {
        golem.setTask(task);
        task.claim();
        if (ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get()) {
            golem.level().broadcastEntityEvent(golem, CLAIM_EMOTE);
        }
    }

    public static void continueWith(ServerLevel level, IGolemAPI golem, Predicate<Task> follows) {
        if (!(golem.asEntity() instanceof EntityThaumaturgeGolem body)) {
            return;
        }
        TaskBoard.of(level).openEntityTasks(null, body).stream()
                .filter(follows)
                .filter(next -> next.canBePerformedBy(golem))
                .filter(next -> withinReach(body, next.entity()))
                .findFirst()
                .ifPresent(next -> assign(body, next));
    }

    private static boolean withinReach(EntityThaumaturgeGolem body, Entity target) {
        return target != null && body.isWithinRestriction(target.blockPosition());
    }
}
