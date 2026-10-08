package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

public final class ConstructFollowOwnerGoal extends Goal {
    private static final int REPATH_TICKS = 10;
    private static final float LOOK_YAW_STEP = 10.0F;
    private static final double TELEPORT_MIN_DISTANCE = 12.0;
    private static final int RING_RADIUS = 2;
    private static final double COLUMN_CENTRE = 0.5;

    private final EntityOwnedConstruct construct;
    private final double speed;
    private final float startDistance;
    private final float stopDistance;
    private @Nullable LivingEntity owner;
    private int repathIn;
    private float waterMalus;

    public ConstructFollowOwnerGoal(
            EntityOwnedConstruct construct, double speed, float startDistance, float stopDistance) {
        this.construct = construct;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity candidate = construct.getOwner();
        if (candidate == null || construct.distanceTo(candidate) < startDistance) {
            return false;
        }
        owner = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null && !construct.getNavigation().isDone() && construct.distanceTo(owner) > stopDistance;
    }

    @Override
    public void start() {
        repathIn = 0;
        waterMalus = construct.getPathfindingMalus(PathType.WATER);
        construct.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        owner = null;
        construct.getNavigation().stop();
        construct.setPathfindingMalus(PathType.WATER, waterMalus);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (owner == null) {
            return;
        }
        construct.getLookControl().setLookAt(owner, LOOK_YAW_STEP, construct.getMaxHeadXRot());
        if (--repathIn > 0) {
            return;
        }
        repathIn = adjustedTickDelay(REPATH_TICKS);
        if (!construct.getNavigation().moveTo(owner, speed)
                && !construct.isLeashed()
                && construct.distanceTo(owner) >= TELEPORT_MIN_DISTANCE) {
            teleportNear(owner);
        }
    }

    private void teleportNear(LivingEntity target) {
        BlockPos feet = target.blockPosition();
        for (int dx = -RING_RADIUS; dx <= RING_RADIUS; dx++) {
            for (int dz = -RING_RADIUS; dz <= RING_RADIUS; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != RING_RADIUS) {
                    continue;
                }
                BlockPos spot = feet.offset(dx, 0, dz);
                if (isSafe(construct.level(), spot)) {
                    construct.moveTo(
                            spot.getX() + COLUMN_CENTRE,
                            spot.getY(),
                            spot.getZ() + COLUMN_CENTRE,
                            construct.getYRot(),
                            construct.getXRot());
                    construct.getNavigation().stop();
                    return;
                }
            }
        }
    }

    private static boolean isSafe(Level level, BlockPos feet) {
        BlockPos floor = feet.below();
        BlockPos head = feet.above();
        return level.getBlockState(floor).isCollisionShapeFullBlock(level, floor)
                && !level.getBlockState(feet).isCollisionShapeFullBlock(level, feet)
                && !level.getBlockState(head).isCollisionShapeFullBlock(level, head);
    }
}
