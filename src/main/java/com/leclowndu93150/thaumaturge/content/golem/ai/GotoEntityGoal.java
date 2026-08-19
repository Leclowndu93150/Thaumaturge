package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public final class GotoEntityGoal extends GotoGoal {
    private static final byte EVENT_EMOTE_TASK = 5;

    public GotoEntityGoal(EntityThaumaturgeGolem golem) {
        super(golem);
    }

    @Override
    public void tick() {
        super.tick();
        if (golem.getTask() != null && golem.getTask().getEntity() != null) {
            golem.getLookControl().setLookAt(golem.getTask().getEntity(), 10.0F, golem.getMaxHeadXRot());
        }
    }

    @Override
    protected void moveTo() {
        if (golem.getTask() != null && golem.getTask().getEntity() != null) {
            golem.getNavigation().moveTo(golem.getTask().getEntity(), golem.getGolemMoveSpeed());
        }
    }

    @Override
    protected boolean findDestination() {
        for (Task task : TaskHandler.getEntityTasksSorted(golem.level(), golem.getUUID(), golem)) {
            if (areGolemTagsValidForTask(task) && task.canGolemPerformTask(golem) && golem.isWithinHome(task.getEntity().blockPosition())
                    && isValidDestination(golem.level(), task.getEntity().blockPosition()) && canEasilyReach(task.getEntity())) {
                golem.setTask(task);
                task.setReserved(true);
                float halfWidth = task.getEntity().getBbWidth() / 2.0F;
                minDist = 3.5 + halfWidth * halfWidth;
                if (ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get()) {
                    golem.level().broadcastEntityEvent(golem, EVENT_EMOTE_TASK);
                }
                return true;
            }
        }
        return false;
    }

    private boolean canEasilyReach(Entity entity) {
        if (golem.distanceToSqr(entity) < minDist) {
            return true;
        }
        Path path = golem.getNavigation().createPath(entity, 0);
        if (path == null) {
            return false;
        }
        Node end = path.getEndNode();
        if (end == null) {
            return false;
        }
        int dx = end.x - Mth.floor(entity.getX());
        int dz = end.z - Mth.floor(entity.getZ());
        return dx * dx + dz * dz < minDist;
    }
}
