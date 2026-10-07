package com.leclowndu93150.thaumaturge.content.device.levitator;

import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class LevitatorMist {
    private static final double FACE_INSET = 0.45;
    private static final double FACE_SPREAD = 0.3;
    private static final double STREAM_SPEED = 0.025;
    private static final double STREAM_JITTER = 0.004;
    private static final float CLING_CHANCE = 0.12F;
    private static final double CLING_DRIFT = 0.006;

    private LevitatorMist() {}

    public static void stream(Level level, BlockPos pos, Direction facing, float chance) {
        RandomSource random = level.getRandom();
        if (random.nextFloat() >= chance) {
            return;
        }
        Vec3 push = facing.getUnitVec3();
        Vec3 origin = pos.getCenter().add(push.scale(FACE_INSET)).add(planeJitter(random, facing));
        Vec3 velocity = push.scale(STREAM_SPEED).add(random.triangle(0.0, STREAM_JITTER), random.triangle(0.0, STREAM_JITTER), random.triangle(0.0, STREAM_JITTER));
        level.addParticle(TTParticles.LEVITATOR_MIST.get(), origin.x, origin.y, origin.z, velocity.x, velocity.y, velocity.z);
    }

    public static void cling(Level level, Entity rider) {
        RandomSource random = level.getRandom();
        if (random.nextFloat() >= CLING_CHANCE) {
            return;
        }
        double halfWidth = rider.getBbWidth() * 0.5;
        level.addParticle(TTParticles.LEVITATOR_MIST.get(), rider.getX() + random.triangle(0.0, halfWidth), rider.getY() + random.nextDouble() * rider.getBbHeight(),
                rider.getZ() + random.triangle(0.0, halfWidth), random.triangle(0.0, CLING_DRIFT), random.triangle(0.0, CLING_DRIFT), random.triangle(0.0, CLING_DRIFT));
    }

    private static Vec3 planeJitter(RandomSource random, Direction facing) {
        double a = random.triangle(0.0, FACE_SPREAD);
        double b = random.triangle(0.0, FACE_SPREAD);
        return switch (facing.getAxis()) {
            case X -> new Vec3(0.0, a, b);
            case Y -> new Vec3(a, 0.0, b);
            case Z -> new Vec3(a, b, 0.0);
        };
    }
}
