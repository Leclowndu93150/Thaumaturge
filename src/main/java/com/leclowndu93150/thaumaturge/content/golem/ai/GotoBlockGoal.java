package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class GotoBlockGoal extends GotoGoal {
    private static final double EASY_REACH_DIST_SQR = 2.25;
    private static final byte EVENT_EMOTE_TASK = 5;

    public GotoBlockGoal(EntityThaumaturgeGolem golem) {
        super(golem);
    }

    @Override
    public void tick() {
        super.tick();
        if (golem.getTask() != null) {
            BlockPos pos = golem.getTask().getPos();
            golem.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10.0F, golem.getMaxHeadXRot());
        }
    }

    @Override
    protected void moveTo() {
        BlockPos destination = targetBlock != null ? targetBlock : golem.getTask().getPos();
        golem.getNavigation().moveTo(destination.getX() + 0.5, destination.getY() + 0.5, destination.getZ() + 0.5, golem.getGolemMoveSpeed());
    }

    @Override
    protected boolean findDestination() {
        for (Task task : TaskHandler.getBlockTasksSorted(golem.level(), golem.getUUID(), golem)) {
            if (areGolemTagsValidForTask(task) && task.canGolemPerformTask(golem) && golem.isWithinHome(task.getPos()) && isValidDestination(golem.level(), task.getPos())
                    && canEasilyReach(task.getPos())) {
                targetBlock = getAdjacentSpace(task.getPos());
                golem.setTask(task);
                task.setReserved(true);
                if (ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get()) {
                    golem.level().broadcastEntityEvent(golem, EVENT_EMOTE_TASK);
                }
                return true;
            }
        }
        return false;
    }

    private @Nullable BlockPos getAdjacentSpace(BlockPos pos) {
        double best = Double.MAX_VALUE;
        BlockPos closest = null;
        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = pos.relative(face);
            if (golem.level().getBlockState(adjacent).getCollisionShape(golem.level(), adjacent).isEmpty()) {
                double dist = adjacent.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ());
                if (dist < best) {
                    closest = adjacent;
                    best = dist;
                }
            }
        }
        return closest;
    }

    private boolean canEasilyReach(BlockPos pos) {
        if (golem.distanceToSqr(Vec3.atCenterOf(pos)) < minDist) {
            return true;
        }
        Path path = golem.getNavigation().createPath(pos, 0);
        if (path == null) {
            return false;
        }
        Node end = path.getEndNode();
        if (end == null) {
            return false;
        }
        int dx = end.x - Mth.floor(pos.getX());
        int dz = end.z - Mth.floor(pos.getZ());
        int dy = end.y - Mth.floor(pos.getY());
        if (dx == 0 && dz == 0 && dy == 2) {
            dy--;
        }
        return dx * dx + dz * dz + dy * dy < EASY_REACH_DIST_SQR;
    }
}
