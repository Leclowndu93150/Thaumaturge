package com.leclowndu93150.thaumaturge.client.space;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public interface ClientSpaceProvider {
    Vec3 renderPosition(Level level, Vec3 position);

    Quaternionfc renderOrientation(Level level, Vec3 position);
}
