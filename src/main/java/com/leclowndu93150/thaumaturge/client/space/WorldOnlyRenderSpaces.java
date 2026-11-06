package com.leclowndu93150.thaumaturge.client.space;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

enum WorldOnlyRenderSpaces implements ClientSpaceProvider {
    INSTANCE;

    @Override
    public Vec3 renderPosition(Level level, Vec3 position) {
        return position;
    }

    @Override
    public Quaternionfc renderOrientation(Level level, Vec3 position) {
        return ClientSpaces.IDENTITY;
    }
}
