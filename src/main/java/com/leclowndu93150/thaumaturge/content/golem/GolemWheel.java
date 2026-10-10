package com.leclowndu93150.thaumaturge.content.golem;

import net.minecraft.util.Mth;

final class GolemWheel {
    private static final double WHEEL_DIAMETER = 0.5D;
    private static final double DEGREES_PER_BLOCK = 360.0D / (Math.PI * WHEEL_DIAMETER);

    private GolemWheel() {}

    static float spun(float rotation, double movedX, double movedZ, float facingDegrees) {
        float facing = facingDegrees * Mth.DEG_TO_RAD;
        double forwardX = -Mth.sin(facing);
        double forwardZ = Mth.cos(facing);
        double rolled = movedX * forwardX + movedZ * forwardZ;
        return Mth.wrapDegrees(rotation + (float) (rolled * DEGREES_PER_BLOCK));
    }
}
