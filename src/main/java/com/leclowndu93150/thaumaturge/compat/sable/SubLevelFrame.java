package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.world.phys.Vec3;

final class SubLevelFrame implements SpaceFrame {
    private final SubLevelAccess subLevel;

    SubLevelFrame(SubLevelAccess subLevel) {
        this.subLevel = subLevel;
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
    public boolean equals(Object other) {
        return other instanceof SubLevelFrame frame
                && frame.subLevel.getUniqueId().equals(subLevel.getUniqueId());
    }

    @Override
    public int hashCode() {
        return subLevel.getUniqueId().hashCode();
    }
}
