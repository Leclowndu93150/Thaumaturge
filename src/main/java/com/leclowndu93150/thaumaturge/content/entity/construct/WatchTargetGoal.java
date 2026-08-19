package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;

public final class WatchTargetGoal extends Goal {
    private final Mob watcher;
    private LivingEntity closestEntity;
    private int lookTime;

    public WatchTargetGoal(Mob watcher) {
        this.watcher = watcher;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (watcher.getTarget() != null) {
            closestEntity = watcher.getTarget();
        }
        return closestEntity != null;
    }

    @Override
    public boolean canContinueToUse() {
        double range = watcher.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (!closestEntity.isAlive()) {
            return false;
        }
        if (watcher.distanceToSqr(closestEntity) > range * range) {
            return false;
        }
        return lookTime > 0;
    }

    @Override
    public void start() {
        lookTime = 40 + watcher.getRandom().nextInt(40);
    }

    @Override
    public void stop() {
        closestEntity = null;
    }

    @Override
    public void tick() {
        watcher.getLookControl().setLookAt(closestEntity.getX(), closestEntity.getY() + closestEntity.getEyeHeight(), closestEntity.getZ(), 10.0F, watcher.getMaxHeadXRot());
        lookTime--;
    }
}
