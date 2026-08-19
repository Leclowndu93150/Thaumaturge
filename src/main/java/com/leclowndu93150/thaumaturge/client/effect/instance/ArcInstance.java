package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class ArcInstance implements IFXInstance {
    static final int MAX_AGE = 3;
    private static final int MAX_SEGMENTS = 50;
    private static final double GRAVITY = 0.115;
    private static final double STEP_GRAVITY = GRAVITY / 1.9;
    private static final double JITTER = 0.25;
    private static final double MIN_RUN = 1.0E-9;

    public final double startX;
    public final double startY;
    public final double startZ;
    public final float colorR;
    public final float colorG;
    public final float colorB;
    public final float length;
    public final List<Vec3> points;
    private int age;
    private boolean expired;

    public ArcInstance(double sx, double sy, double sz, double tx, double ty, double tz, int color, float apexHint) {
        this.startX = sx;
        this.startY = sy;
        this.startZ = sz;
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        Vec3 span = new Vec3(tx - sx, ty - sy, tz - sz);
        this.length = (float) span.length();
        ClientLevel level = Minecraft.getInstance().level;
        RandomSource random = level != null ? level.getRandom() : RandomSource.create();
        this.points = tracePath(span, random, apexHint);
        if (level != null) {
            for (int i = 1; i < this.points.size() - 1; i++) {
                Vec3 p = this.points.get(i);
                spawnCrackle(level, random, sx + p.x, sy + p.y, sz + p.z);
            }
        }
    }

    private static List<Vec3> tracePath(Vec3 span, RandomSource random, float apexHint) {
        List<Vec3> path = new ArrayList<>();
        path.add(Vec3.ZERO);
        Vec3 step = launchVelocity(span, apexHint, GRAVITY);
        double reach = step.lengthSqr();
        Vec3 cursor = Vec3.ZERO;
        for (int segments = 0; segments < MAX_SEGMENTS && cursor.distanceToSqr(span) > reach; segments++) {
            cursor = cursor.add(step);
            path.add(cursor.add(jitter(random), jitter(random), jitter(random)));
            step = step.subtract(0.0, STEP_GRAVITY, 0.0);
        }
        path.add(span);
        return path;
    }

    private static double jitter(RandomSource random) {
        return (random.nextDouble() - random.nextDouble()) * JITTER;
    }

    private static Vec3 launchVelocity(Vec3 span, double apexHint, double gravity) {
        double rise = span.y;
        double run = Math.sqrt(span.x * span.x + span.z * span.z);
        double apex = Math.max(apexHint, rise + apexHint);
        double vertical = Math.sqrt(apex * gravity);
        if (run < MIN_RUN) {
            return new Vec3(0.0, vertical, 0.0);
        }
        double slope = 2.0 * (apex + Math.sqrt(apex * (apex - rise))) / run;
        double horizontal = vertical / slope;
        return new Vec3(span.x / run * horizontal, vertical, span.z / run * horizontal);
    }

    private void spawnCrackle(ClientLevel level, RandomSource random, double wx, double wy, double wz) {
        ClientEffects.drawSimpleSparkle(level, random, wx, wy, wz, 0.0, 0.0, 0.0, 0.5F, Mth.clamp(this.colorR * 3.0F, 0.0F, 1.0F), Mth.clamp(this.colorG * 3.0F, 0.0F, 1.0F),
                Mth.clamp(this.colorB * 3.0F, 0.0F, 1.0F), 2 + random.nextInt(3), 0.995F, 0.1F, 8);
    }

    @Override
    public void tick() {
        if (this.age++ >= MAX_AGE) {
            this.expired = true;
        }
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    public float alpha(float partialTick) {
        return 1.0F - (this.age + partialTick) / MAX_AGE;
    }
}
