package com.leclowndu93150.thaumaturge.content.world.space;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface SpaceFrame {
    boolean isWorld();

    Vec3 toWorld(Vec3 local);

    Vec3 toLocal(Vec3 world);

    Vec3 directionToWorld(Vec3 local);

    Vec3 directionToLocal(Vec3 world);

    void push(BlockPos source, Vec3 worldPoint, Vec3 worldDirection, double liftBlocks, double maxSpeed);
}
