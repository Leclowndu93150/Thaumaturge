package com.leclowndu93150.thaumaturge.content.entity.eldritch;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class CastingArms {
    private static final byte LEFT_CAST = 15;
    private static final byte RIGHT_CAST = 16;
    private static final byte BOTH_RAISED = 17;
    private static final float CAST_LIFT = 0.5F;
    private static final float RAISED_LIFT = 0.9F;
    private static final float RELAX_RATE = 0.05F;
    private static final float HAND_REACH = 0.5F;
    private static final float RIGHT_HAND_BEARING = 90.0F;
    private static final float LEFT_HAND_BEARING = 180.0F;

    private final Mob caster;
    private boolean rightHandNext;
    private float leftLift;
    private float rightLift;

    public CastingArms(Mob caster) {
        this.caster = caster;
    }

    public Vec3 castFromNextHand() {
        rightHandNext = !rightHandNext;
        caster.level().broadcastEntityEvent(caster, rightHandNext ? RIGHT_CAST : LEFT_CAST);
        float bearing = (caster.getYRot() + (rightHandNext ? RIGHT_HAND_BEARING : LEFT_HAND_BEARING)) % 360.0F * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.cos(bearing) * HAND_REACH, 0.0, -Mth.sin(bearing) * HAND_REACH);
    }

    public void raiseBoth() {
        caster.level().broadcastEntityEvent(caster, BOTH_RAISED);
    }

    public boolean receive(byte event) {
        switch (event) {
            case LEFT_CAST -> leftLift = CAST_LIFT;
            case RIGHT_CAST -> rightLift = CAST_LIFT;
            case BOTH_RAISED -> {
                leftLift = RAISED_LIFT;
                rightLift = RAISED_LIFT;
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    public void relax() {
        leftLift = Math.max(0.0F, leftLift - RELAX_RATE);
        rightLift = Math.max(0.0F, rightLift - RELAX_RATE);
    }

    public float leftLift() {
        return leftLift;
    }

    public float rightLift() {
        return rightLift;
    }
}
