package com.leclowndu93150.thaumaturge.content.spell.world;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class Cones {
    private static final Vec3 WORLD_UP = new Vec3(0.0, 1.0, 0.0);
    private static final Vec3 WORLD_EAST = new Vec3(1.0, 0.0, 0.0);
    private static final double NEAR_VERTICAL = 0.99;

    private Cones() {}

    public static Vec3 jitter(Vec3 direction, float halfAngle, RandomSource random) {
        Vec3 axis = direction.normalize();
        Vec3 side = sideOf(axis);
        Vec3 up = side.cross(axis).normalize();
        double angle = halfAngle * Math.sqrt(random.nextDouble());
        double spin = random.nextDouble() * Math.PI * 2.0;
        Vec3 offset = side.scale(Math.cos(spin)).add(up.scale(Math.sin(spin))).scale(Math.sin(angle));
        return axis.scale(Math.cos(angle)).add(offset).normalize();
    }

    public static Vec3 yaw(Vec3 direction, float radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(direction.x * cos - direction.z * sin, direction.y, direction.x * sin + direction.z * cos)
                .normalize();
    }

    private static Vec3 sideOf(Vec3 axis) {
        return Math.abs(axis.y) < NEAR_VERTICAL
                ? axis.cross(WORLD_UP).normalize()
                : axis.cross(WORLD_EAST).normalize();
    }
}
