package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public abstract class StreamInstance implements IFXInstance {
    public record TrailPoint(double radius, double x, double y, double z) {
    }

    public record Snapshot(double[][] points, float[][] colours, double[] radii, double originX, double originY, double originZ, float texSlice, float start) {
    }

    protected static final double MIN_RADIUS = 0.001;
    private static final double RISE_PER_TICK = 0.01 * 0.2;
    private static final double FRICTION = 0.985;
    private static final double EPSILON = 1.0E-6;
    private static final float TEX_SLICE = 0.075F;
    private static final float SCALE_SPREAD = 0.15F;
    private static final float PULSE_BASE = 0.75F;
    private static final float PULSE_SWING = 0.25F;
    private static final float RADIUS_VARIANCE = 0.2F;
    private static final float COLOR_DIM = 0.1F;
    private static final int TAPER_SPAN = 10;
    private static final int TAPER_LEAD = 12;

    protected final double originX;
    protected final double originY;
    protected final double originZ;
    protected final float colorR;
    protected final float colorG;
    protected final float colorB;
    protected final int waveSeed;
    protected final List<TrailPoint> trail = new ArrayList<>();
    protected float radiusBase;
    protected int maxAge;
    protected int length;
    protected int age;
    private double posX;
    private double posY;
    private double posZ;
    private double motionX;
    private double motionY;
    private double motionZ;
    private int dissolveStart = -1;
    private boolean expired;

    protected StreamInstance(double sx, double sy, double sz, int color, int waveSeed, float scale) {
        this.originX = sx;
        this.originY = sy;
        this.originZ = sz;
        this.posX = sx;
        this.posY = sy;
        this.posZ = sz;
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        this.waveSeed = waveSeed;
        this.radiusBase = (float) (scale * (1.0 + currentRandom().nextGaussian() * SCALE_SPREAD));
        this.trail.add(new TrailPoint(MIN_RADIUS, 0.0, 0.0, 0.0));
        this.trail.add(new TrailPoint(MIN_RADIUS, 0.0, 0.0, 0.0));
    }

    protected static RandomSource currentRandom() {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null ? level.getRandom() : RandomSource.create();
    }

    protected final void launchMotion(float amplitude, double verticalBoost) {
        this.motionX = Mth.sin(this.waveSeed / 4.0F) * amplitude;
        this.motionY = verticalBoost + Mth.sin(this.waveSeed / 3.0F) * amplitude;
        this.motionZ = Mth.sin(this.waveSeed / 2.0F) * amplitude;
    }

    protected final void launchToward(Vec3 direction, double speed, float wobble) {
        Vec3 heading = direction.normalize().scale(speed);
        this.motionX = heading.x + Mth.sin(this.waveSeed / 4.0F) * wobble;
        this.motionY = heading.y + Mth.sin(this.waveSeed / 3.0F) * wobble;
        this.motionZ = heading.z + Mth.sin(this.waveSeed / 2.0F) * wobble;
    }

    protected final Vec3 position() {
        return new Vec3(this.posX, this.posY, this.posZ);
    }

    protected static int travelTicks(double sx, double sy, double sz, double tx, double ty, double tz) {
        double dx = tx - sx;
        double dy = ty - sy;
        double dz = tz - sz;
        return Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy + dz * dz) * 21.0));
    }

    @Override
    public final boolean isExpired() {
        return this.expired;
    }

    @Override
    public final void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (this.age++ >= this.maxAge || this.length < 1) {
            this.expired = true;
            return;
        }
        Vec3 target = seekTarget(level);
        if (target == null) {
            this.expired = true;
            return;
        }
        this.motionY += RISE_PER_TICK;
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        this.motionX *= FRICTION;
        this.motionY *= FRICTION;
        this.motionZ *= FRICTION;
        double dx = target.x - this.posX;
        double dy = target.y - this.posY;
        double dz = target.z - this.posZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        restrainMotion(distance);
        if (distance > EPSILON) {
            double pull = pullStrength(distance) / Math.min(1.0, distance);
            this.motionX += dx / distance * pull + steerJitter(level);
            this.motionY += dy / distance * pull + steerJitter(level);
            this.motionZ += dz / distance * pull + steerJitter(level);
        }
        double radius = this.radiusBase * (PULSE_BASE + Mth.sin((this.waveSeed + this.age) / 2.0F) * PULSE_SWING);
        if (distance < shrinkRange()) {
            float squeeze = Mth.sin((float) (distance * (Math.PI / 2.0)));
            radius *= squeeze;
            this.radiusBase *= squeeze;
        }
        if (this.radiusBase > MIN_RADIUS) {
            this.trail.add(new TrailPoint(radius, this.posX - this.originX, this.posY - this.originY, this.posZ - this.originZ));
        } else {
            if (this.dissolveStart < 0) {
                this.dissolveStart = this.age;
            }
            this.length--;
            onDissolve(level);
        }
        if (this.trail.size() > this.length) {
            this.trail.remove(0);
        }
        afterStep(level);
    }

    protected final void clampMotion(double limit) {
        this.motionX = Mth.clamp(this.motionX, -limit, limit);
        this.motionY = Mth.clamp(this.motionY, -limit, limit);
        this.motionZ = Mth.clamp(this.motionZ, -limit, limit);
    }

    protected abstract Vec3 seekTarget(ClientLevel level);

    protected abstract void restrainMotion(double distance);

    protected abstract double pullStrength(double distance);

    protected double steerJitter(ClientLevel level) {
        return 0.0;
    }

    protected double shrinkRange() {
        return 1.0;
    }

    protected void onDissolve(ClientLevel level) {}

    protected void afterStep(ClientLevel level) {}

    protected final Snapshot buildSnapshot(float partialTick, float wobble, boolean newestFirst, float radiusScale) {
        int n = this.trail.size();
        if (n < 3) {
            return null;
        }
        double[][] points = new double[n][3];
        float[][] colours = new float[n][4];
        double[] radii = new double[n];
        for (int slot = 0; slot < n; slot++) {
            int source = newestFirst ? n - 1 - slot : slot;
            int phase = newestFirst ? slot : n - 1 - slot;
            TrailPoint point = this.trail.get(source);
            int beat = phase + this.age;
            points[slot][0] = point.x() + Mth.sin(beat / 6.0F) * wobble;
            points[slot][1] = point.y() + Mth.sin(beat / 7.0F) * wobble;
            points[slot][2] = point.z() + Mth.sin(beat / 8.0F) * wobble;
            double radius = point.radius() * (1.0F + Mth.sin(beat / 3.0F) * RADIUS_VARIANCE) * radiusScale;
            if (phase > n - TAPER_SPAN) {
                radius *= Mth.cos((float) ((phase - (n - TAPER_LEAD)) / (float) TAPER_SPAN * (Math.PI / 2.0)));
            }
            radii[slot] = anchorRadius(slot, radius);
            colour(colours[slot], beat);
        }
        float start = this.dissolveStart < 0 ? 0.0F : TEX_SLICE * (this.age - this.dissolveStart + partialTick);
        return new Snapshot(points, colours, radii, this.originX, this.originY, this.originZ, TEX_SLICE, start);
    }

    private double anchorRadius(int slot, double radius) {
        return switch (slot) {
            case 0, 1 -> 0.0;
            case 2 -> (this.radiusBase * 0.5F + radius) / 2.0;
            case 3 -> (this.radiusBase + radius) / 2.0;
            case 4 -> (this.radiusBase + radius * 2.0) / 3.0;
            default -> radius;
        };
    }

    protected void colour(float[] out, int beat) {
        float dim = 1.0F - Mth.sin(beat / 2.0F) * COLOR_DIM;
        out[0] = Math.min(1.0F, this.colorR * dim);
        out[1] = Math.min(1.0F, this.colorG * dim);
        out[2] = Math.min(1.0F, this.colorB * dim);
        out[3] = 1.0F;
    }

    protected final TrailPoint trailPoint(int index) {
        return this.trail.get(index);
    }

    protected final int trailSize() {
        return this.trail.size();
    }
}
