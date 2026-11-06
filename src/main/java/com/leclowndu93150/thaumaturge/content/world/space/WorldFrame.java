package com.leclowndu93150.thaumaturge.content.world.space;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public enum WorldFrame implements SpaceFrame {
    INSTANCE;

    @Override
    public boolean isWorld() {
        return true;
    }

    @Override
    public Vec3 toWorld(Vec3 local) {
        return local;
    }

    @Override
    public Vec3 toLocal(Vec3 world) {
        return world;
    }

    @Override
    public Vec3 directionToWorld(Vec3 local) {
        return local;
    }

    @Override
    public Vec3 directionToLocal(Vec3 world) {
        return world;
    }

    @Override
    public void push(BlockPos source, Vec3 worldPoint, Vec3 worldDirection, double liftBlocks, double maxSpeed) {}
}
