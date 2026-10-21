package com.leclowndu93150.thaumaturge.api.rifts;

import net.minecraft.world.phys.Vec3;

public record RiftSpec(Vec3 position, int size, int seed, float stability, Source source) {
    public enum Source {
        /** Consumes one unit of local flux per size point, following natural rift creation. */
        FLUX,
        /** Requires a present creative player; unavailable to machine grants. */
        CREATIVE
    }
}
