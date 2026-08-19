package com.leclowndu93150.thaumaturge.client.effect.instance;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class BoreStreamInstance extends StreamInstance {
    private static final float LAUNCH_AMPLITUDE = 0.15F;
    private static final int MIN_LENGTH = 5;
    private static final int TICKS_PER_SEGMENT = 10;
    private static final double PULL_DIVISOR = 10.0;
    private static final float WOBBLE = 0.03F;

    private final int targetEntityId;

    public BoreStreamInstance(double sx, double sy, double sz, int targetEntityId, int color, int count, float scale, int extend, double my) {
        super(sx, sy, sz, color, count, scale);
        this.targetEntityId = targetEntityId;
        this.length = Math.max(MIN_LENGTH, extend);
        this.maxAge = this.length * TICKS_PER_SEGMENT;
        launchMotion(LAUNCH_AMPLITUDE, my);
    }

    @Override
    protected Vec3 seekTarget(ClientLevel level) {
        Entity target = level.getEntity(this.targetEntityId);
        if (target == null) {
            return null;
        }
        return new Vec3(target.getX(), target.getY() + target.getEyeHeight(), target.getZ());
    }

    @Override
    protected void restrainMotion(double distance) {
        clampMotion(distance / PULL_DIVISOR);
    }

    @Override
    protected double pullStrength(double distance) {
        return distance / PULL_DIVISOR;
    }

    public Snapshot snapshot(float partialTick) {
        return buildSnapshot(partialTick, WOBBLE, false, 1.0F);
    }
}
