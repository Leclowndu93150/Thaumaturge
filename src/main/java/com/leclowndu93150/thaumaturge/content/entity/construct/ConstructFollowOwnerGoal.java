package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;

public final class ConstructFollowOwnerGoal extends Goal {
    private static final int RECALC_INTERVAL = 10;
    private static final double TELEPORT_DIST_SQR = 144.0;

    private final EntityOwnedConstruct construct;
    private LivingEntity owner;
    private final double followSpeed;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float startDistance;
    private final float stopDistance;
    private float oldWaterCost;

    public ConstructFollowOwnerGoal(EntityOwnedConstruct construct, double followSpeed, float startDistance, float stopDistance) {
        this.construct = construct;
        this.followSpeed = followSpeed;
        this.navigation = construct.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        LivingEntity currentOwner = construct.getOwner();
        if (currentOwner == null) {
            return false;
        }
        if (construct.distanceToSqr(currentOwner) < startDistance * startDistance) {
            return false;
        }
        owner = currentOwner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !navigation.isDone() && construct.distanceToSqr(owner) > stopDistance * stopDistance;
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
        oldWaterCost = construct.getPathfindingMalus(PathType.WATER);
        construct.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        owner = null;
        navigation.stop();
        construct.setPathfindingMalus(PathType.WATER, oldWaterCost);
    }

    @Override
    public void tick() {
        construct.getLookControl().setLookAt(owner, 10.0F, construct.getMaxHeadXRot());
        if (--timeToRecalcPath > 0) {
            return;
        }
        timeToRecalcPath = RECALC_INTERVAL;
        if (navigation.moveTo(owner, followSpeed) || construct.isLeashed() || construct.distanceToSqr(owner) < TELEPORT_DIST_SQR) {
            return;
        }
        int baseX = Mth.floor(owner.getX()) - 2;
        int baseZ = Mth.floor(owner.getZ()) - 2;
        int baseY = Mth.floor(owner.getBoundingBox().minY);
        for (int dx = 0; dx <= 4; dx++) {
            for (int dz = 0; dz <= 4; dz++) {
                if ((dx < 1 || dz < 1 || dx > 3 || dz > 3) && isSolidGround(new BlockPos(baseX + dx, baseY - 1, baseZ + dz)) && isPassable(new BlockPos(baseX + dx, baseY, baseZ + dz))
                        && isPassable(new BlockPos(baseX + dx, baseY + 1, baseZ + dz))) {
                    construct.snapTo(baseX + dx + 0.5, baseY, baseZ + dz + 0.5, construct.getYRot(), construct.getXRot());
                    navigation.stop();
                    return;
                }
            }
        }
    }

    private boolean isPassable(BlockPos pos) {
        return construct.level().getBlockState(pos).isAir() || !construct.level().getBlockState(pos).isCollisionShapeFullBlock(construct.level(), pos);
    }

    private boolean isSolidGround(BlockPos pos) {
        return construct.level().getBlockState(pos).isCollisionShapeFullBlock(construct.level(), pos);
    }
}
