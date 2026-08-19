package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class BoltInstance implements IFXInstance {
    static final int MAX_AGE = 3;
    private static final float WAVE_PERIOD_X = 4.0F;
    private static final float WAVE_PERIOD_Y = 3.0F;
    private static final float WAVE_PERIOD_Z = 2.0F;
    private static final float AMPLITUDE_PER_TICK = 0.1F;
    private static final float JITTER = 0.1F;
    private static final float THIN_DECAY = 0.25F;
    private static final int THIN_CHANCE = 4;

    public final double startX;
    public final double startY;
    public final double startZ;
    public final float colorR;
    public final float colorG;
    public final float colorB;
    public final float width;
    public final float length;
    private final double spanX;
    private final double spanY;
    private final double spanZ;
    private final float phaseOffset;
    private final long seed;
    private int age;
    private boolean expired;

    public BoltInstance(double sx, double sy, double sz, double tx, double ty, double tz, int color, float width) {
        this.startX = sx;
        this.startY = sy;
        this.startZ = sz;
        this.spanX = tx - sx;
        this.spanY = ty - sy;
        this.spanZ = tz - sz;
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        this.width = width;
        double span = Math.sqrt(this.spanX * this.spanX + this.spanY * this.spanY + this.spanZ * this.spanZ);
        this.length = (float) (span * Math.PI);
        ClientLevel level = Minecraft.getInstance().level;
        RandomSource random = level != null ? level.getRandom() : RandomSource.create();
        this.phaseOffset = (float) (random.nextInt(50) * Math.PI);
        this.seed = random.nextInt(1000);
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

    public List<PathStep> computePath(float partialTick) {
        int steps = Math.max(3, (int) this.length);
        float amplitude = (this.age + partialTick) * AMPLITUDE_PER_TICK;
        float stride = this.length / steps;
        Random noise = new Random(this.seed);
        List<PathStep> path = new ArrayList<>(steps);
        path.add(new PathStep(0.0, 0.0, 0.0, this.width));
        for (int i = 1; i < steps - 1; i++) {
            float phase = i * stride + this.phaseOffset;
            double x = this.spanX * i / steps + Mth.sin(phase / WAVE_PERIOD_X) * amplitude + sway(noise);
            double y = this.spanY * i / steps + Mth.sin(phase / WAVE_PERIOD_Y) * amplitude + sway(noise);
            double z = this.spanZ * i / steps + Mth.sin(phase / WAVE_PERIOD_Z) * amplitude + sway(noise);
            float thin = noise.nextInt(THIN_CHANCE) == 0 ? 1.0F - this.age * THIN_DECAY : 1.0F;
            path.add(new PathStep(x, y, z, thin * this.width));
        }
        path.add(new PathStep(this.spanX, this.spanY, this.spanZ, this.width));
        return path;
    }

    private static double sway(Random noise) {
        return (noise.nextFloat() - noise.nextFloat()) * JITTER;
    }

    public float alpha(float partialTick) {
        return Mth.clamp(1.0F - (this.age + partialTick) / MAX_AGE, 0.1F, 1.0F);
    }

    public record PathStep(double x, double y, double z, float width) {
    }
}
