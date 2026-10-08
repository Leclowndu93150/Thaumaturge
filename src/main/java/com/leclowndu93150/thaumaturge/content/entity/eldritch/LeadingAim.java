package com.leclowndu93150.thaumaturge.content.entity.eldritch;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class LeadingAim {
    private LeadingAim() {}

    public static Vec3 at(Entity shooter, Entity target) {
        Vec3 drift = target.getDeltaMovement();
        return new Vec3(
                target.getX() + drift.x - shooter.getX(),
                target.getY() - shooter.getY() - target.getBbHeight() / 2.0F,
                target.getZ() + drift.z - shooter.getZ());
    }
}
