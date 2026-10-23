package com.leclowndu93150.thaumaturge.content.entity.ranger;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

final class RangerPatrolGoal extends Goal {
    private static final long NAVIGATION_FAILED_COOLDOWN = 200L;
    private static final double COMPANION_RANGE = 16.0;
    private static final double ARRIVAL_DISTANCE = 10.0;
    private static final float SWERVE_DEGREES = 90.0F;
    private static final double SWERVE_SCALE = 0.4;
    private static final double STEP_LENGTH = 10.0;
    private static final int WANDER_RANGE = 8;

    private final AbstractCultistRanger ranger;
    private final double speed;
    private final double leaderSpeed;
    private long cooldownUntil = -1L;

    RangerPatrolGoal(AbstractCultistRanger ranger, double speed, double leaderSpeed) {
        this.ranger = ranger;
        this.speed = speed;
        this.leaderSpeed = leaderSpeed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return ranger.isPatrolling()
                && ranger.getTarget() == null
                && !ranger.hasControllingPassenger()
                && ranger.patrolTarget() != null
                && ranger.level().getGameTime() >= cooldownUntil;
    }

    @Override
    public void tick() {
        PathNavigation navigation = ranger.getNavigation();
        BlockPos target = ranger.patrolTarget();
        if (!navigation.isDone() || target == null) {
            return;
        }
        boolean leader = ranger.isPatrolLeader();
        List<AbstractCultistRanger> companions = ranger.level()
                .getEntitiesOfClass(
                        AbstractCultistRanger.class,
                        ranger.getBoundingBox().inflate(COMPANION_RANGE),
                        other -> other != ranger);
        if (companions.isEmpty()) {
            ranger.setPatrolling(false);
        } else if (leader && target.closerToCenterThan(ranger.position(), ARRIVAL_DISTANCE)) {
            ranger.findPatrolTarget();
        } else {
            BlockPos step = nextStep(target);
            if (!navigation.moveTo(step.getX(), step.getY(), step.getZ(), leader ? leaderSpeed : speed)) {
                wander();
                cooldownUntil = ranger.level().getGameTime() + NAVIGATION_FAILED_COOLDOWN;
            } else if (leader) {
                companions.forEach(companion -> companion.setPatrolTarget(step));
            }
        }
    }

    private BlockPos nextStep(BlockPos target) {
        Vec3 goal = Vec3.atBottomCenterOf(target);
        Vec3 self = ranger.position();
        Vec3 swerved =
                self.subtract(goal).yRot(SWERVE_DEGREES).scale(SWERVE_SCALE).add(goal);
        Vec3 step = swerved.subtract(self).normalize().scale(STEP_LENGTH).add(self);
        return ranger.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(step));
    }

    private void wander() {
        RandomSource random = ranger.getRandom();
        BlockPos spot = ranger.level()
                .getHeightmapPos(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        ranger.blockPosition()
                                .offset(
                                        -WANDER_RANGE + random.nextInt(2 * WANDER_RANGE),
                                        0,
                                        -WANDER_RANGE + random.nextInt(2 * WANDER_RANGE)));
        ranger.getNavigation().moveTo(spot.getX(), spot.getY(), spot.getZ(), speed);
    }
}
