package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

final class SubLevelFrame implements SpaceFrame {
    private final SubLevelAccess subLevel;
    private final SablePushes pushes;

    SubLevelFrame(SubLevelAccess subLevel, SablePushes pushes) {
        this.subLevel = subLevel;
        this.pushes = pushes;
    }

    @Override
    public boolean isWorld() {
        return false;
    }

    @Override
    public Vec3 toWorld(Vec3 local) {
        return subLevel.logicalPose().transformPosition(local);
    }

    @Override
    public Vec3 toLocal(Vec3 world) {
        return subLevel.logicalPose().transformPositionInverse(world);
    }

    @Override
    public Vec3 directionToWorld(Vec3 local) {
        return subLevel.logicalPose().transformNormal(local);
    }

    @Override
    public Vec3 directionToLocal(Vec3 world) {
        return subLevel.logicalPose().transformNormalInverse(world);
    }

    @Override
    public void push(BlockPos source, Vec3 worldPoint, Vec3 worldDirection, double liftBlocks, double maxSpeed) {
        if (subLevel instanceof ServerSubLevel ship) {
            pushes.queue(ship, source, worldPoint, worldDirection, liftBlocks, maxSpeed);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SubLevelFrame frame
                && frame.subLevel.getUniqueId().equals(subLevel.getUniqueId());
    }

    @Override
    public int hashCode() {
        return subLevel.getUniqueId().hashCode();
    }
}
