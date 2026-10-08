package com.leclowndu93150.thaumaturge.content.entity.boss.hierophant;

import net.minecraft.world.phys.Vec3;

public final class HierophantSockets {
    private static final double UNITS_PER_BLOCK = 16;

    private HierophantSockets() {}

    public static Vec3 release(HierophantAction action, boolean left) {
        return switch (action) {
            case SWIPE_RIGHT ->
                left
                        ? new Vec3(22.13718152, 28.03404333, 21.75649089)
                        : new Vec3(-16.05080421, 66.85192874, -39.67452036);
            case SWIPE_LEFT ->
                left
                        ? new Vec3(17.56509021, 61.76579389, -40.62820153)
                        : new Vec3(-24.46374336, 26.30528608, 16.99554264);
            case CAST ->
                left
                        ? new Vec3(20.34083830, 49.04904261, -32.81947937)
                        : new Vec3(-18.53678154, 53.78049772, -34.61638519);
            case THROW ->
                left
                        ? new Vec3(13.00130760, 49.72314863, -37.04526872)
                        : new Vec3(-15.06998262, 57.88289760, -24.08547205);
            default -> Vec3.ZERO;
        };
    }

    public static Vec3 worldOffset(HierophantAction action, boolean left, float yaw, float scale) {
        return release(action, left)
                .multiply(-1, 1, -1)
                .scale(scale / UNITS_PER_BLOCK)
                .yRot((float) Math.toRadians(-yaw));
    }
}
