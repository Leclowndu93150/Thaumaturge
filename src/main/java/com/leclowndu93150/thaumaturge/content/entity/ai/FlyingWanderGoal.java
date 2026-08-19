package com.leclowndu93150.thaumaturge.content.entity.ai;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;

public final class FlyingWanderGoal extends Goal {
    private static final int HORIZONTAL_SPREAD = 7;
    private static final int VERTICAL_SPREAD = 6;
    private static final int VERTICAL_BIAS = 2;
    private static final int REPICK_ONE_IN = 30;
    private static final double ARRIVE_DISTANCE_SQ = 4.0;
    private static final int MAX_SURFACE_CLEARANCE = 8;
    private static final int PICK_ATTEMPTS = 10;

    private final Mob mob;
    private final boolean clampToSurface;
    private final BooleanSupplier canWander;

    public FlyingWanderGoal(Mob mob, boolean clampToSurface, BooleanSupplier canWander) {
        this.mob = mob;
        this.clampToSurface = clampToSurface;
        this.canWander = canWander;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!canWander.getAsBoolean()) {
            return false;
        }
        MoveControl moveControl = mob.getMoveControl();
        if (!moveControl.hasWanted()) {
            return true;
        }
        double dx = moveControl.getWantedX() - mob.getX();
        double dy = moveControl.getWantedY() - mob.getY();
        double dz = moveControl.getWantedZ() - mob.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;
        return distSq < ARRIVE_DISTANCE_SQ || mob.getRandom().nextInt(reducedTickDelay(REPICK_ONE_IN)) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        RandomSource random = mob.getRandom();
        for (int attempt = 0; attempt < PICK_ATTEMPTS; attempt++) {
            BlockPos candidate = BlockPos.containing(mob.getX() + random.nextInt(HORIZONTAL_SPREAD) - random.nextInt(HORIZONTAL_SPREAD), mob.getY() + random.nextInt(VERTICAL_SPREAD) - VERTICAL_BIAS,
                    mob.getZ() + random.nextInt(HORIZONTAL_SPREAD) - random.nextInt(HORIZONTAL_SPREAD));
            if (isValidWaypoint(candidate)) {
                mob.getMoveControl().setWantedPosition(candidate.getX() + 0.5, candidate.getY() + 0.1, candidate.getZ() + 0.5, 1.0);
                return;
            }
        }
    }

    private boolean isValidWaypoint(BlockPos pos) {
        if (!mob.level().isEmptyBlock(pos)) {
            return false;
        }
        if (pos.getY() <= mob.level().getMinY()) {
            return false;
        }
        if (!clampToSurface) {
            return true;
        }
        int surface = mob.level().getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        return pos.getY() <= surface + MAX_SURFACE_CLEARANCE;
    }
}
