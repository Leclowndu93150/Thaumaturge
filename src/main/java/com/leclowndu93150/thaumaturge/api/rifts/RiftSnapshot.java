package com.leclowndu93150.thaumaturge.api.rifts;

import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Immutable server observation. A revision is not an authorization or a transferable permission. */
public record RiftSnapshot(UUID identity, ResourceKey<Level> dimension, long revision, Vec3 position, int size, int seed, float stability, boolean collapsing, boolean busy) {
}
