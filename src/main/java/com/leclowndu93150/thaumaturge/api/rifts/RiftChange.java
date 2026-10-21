package com.leclowndu93150.thaumaturge.api.rifts;

import net.minecraft.world.phys.Vec3;

/** Bounded replacement of mutable rift properties; a collapse can be started but never reversed. */
public record RiftChange(Vec3 position, int size, int seed, float stability, boolean collapse) {
    public static RiftChange of(RiftSnapshot snapshot) {
        return new RiftChange(snapshot.position(), snapshot.size(), snapshot.seed(), snapshot.stability(), snapshot.collapsing());
    }
}
