package com.leclowndu93150.thaumaturge.content.golem;

import net.minecraft.world.phys.Vec3;

final class GolemFlightSteering {
    private static final double HORIZONTAL_ACCELERATION = 0.0175D;
    private static final double VERTICAL_ACCELERATION = 0.0015D;
    private static final Vec3 ACCELERATION = new Vec3(HORIZONTAL_ACCELERATION, VERTICAL_ACCELERATION, HORIZONTAL_ACCELERATION);

    private GolemFlightSteering() {}

    static Vec3 desiredDelta(Vec3 offset, double speedModifier) {
        return offset.normalize().multiply(ACCELERATION).scale(speedModifier);
    }

    static void steer(EntityThaumaturgeGolem golem, Vec3 offset, double speedModifier) {
        golem.setDeltaMovement(golem.getDeltaMovement().add(desiredDelta(offset, speedModifier)));
    }
}
