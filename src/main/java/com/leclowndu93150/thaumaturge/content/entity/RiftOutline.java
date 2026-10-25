package com.leclowndu93150.thaumaturge.content.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

final class RiftOutline {
    private static final double STEP_LENGTH = 0.2;
    private static final double TIP_LENGTH = 0.1;
    private static final double TURN_DEVIATION = 0.33;
    private static final double MAX_ELEVATION = Math.PI / 2.0;
    private static final float SIZE_PER_STEP = 3.0F;
    private static final float GIRTH_DIVISOR = 300.0F;

    private RiftOutline() {}

    static void build(int size, int seed, List<Vec3> points, List<Float> widths) {
        points.clear();
        widths.clear();
        int steps = Math.max(0, Mth.ceil(size / SIZE_PER_STEP));
        float girth = Math.max(0, size) / GIRTH_DIVISOR;
        float taper = steps > 0 ? girth / steps : 0.0F;
        RandomSource random = RandomSource.create(seed);
        double azimuth = random.nextDouble() * Math.PI * 2.0;
        double elevation = random.nextGaussian() * TURN_DEVIATION;
        Arm right = growArm(random, azimuth, elevation, steps, girth, taper);
        Arm left = growArm(random, azimuth + Math.PI, -elevation, steps, girth, taper);
        for (int i = left.points().size() - 1; i >= 0; i--) {
            points.add(left.points().get(i));
            widths.add(left.widths().get(i));
        }
        points.addAll(right.points());
        widths.addAll(right.widths());
    }

    private static Arm growArm(
            RandomSource random, double startAzimuth, double startElevation, int steps, float girth, float taper) {
        List<Vec3> armPoints = new ArrayList<>();
        List<Float> armWidths = new ArrayList<>();
        double azimuth = startAzimuth;
        double elevation = startElevation;
        Vec3 position = Vec3.ZERO;
        Vec3 direction = Vec3.ZERO;
        for (int step = 0; step < steps; step++) {
            azimuth += random.nextGaussian() * TURN_DEVIATION;
            elevation = Mth.clamp(elevation + random.nextGaussian() * TURN_DEVIATION, -MAX_ELEVATION, MAX_ELEVATION);
            float cosElevation = Mth.cos((float) elevation);
            direction = new Vec3(
                    cosElevation * Mth.cos((float) azimuth),
                    Mth.sin((float) elevation),
                    cosElevation * Mth.sin((float) azimuth));
            position = position.add(direction.scale(STEP_LENGTH));
            armPoints.add(position);
            armWidths.add(Math.max(0.0F, girth - taper * step));
        }
        armPoints.add(position.add(direction.scale(TIP_LENGTH)));
        armWidths.add(0.0F);
        return new Arm(armPoints, armWidths);
    }

    private record Arm(List<Vec3> points, List<Float> widths) {}
}
