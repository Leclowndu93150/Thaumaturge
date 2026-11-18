package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker.WandSource;
import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BoltInstance implements IFXInstance {
    static final int MAX_AGE = 3;
    private static final float WAVE_PERIOD_X = 4.0F;
    private static final float WAVE_PERIOD_Y = 3.0F;
    private static final float WAVE_PERIOD_Z = 2.0F;
    private static final float AMPLITUDE_PER_TICK = 0.1F;
    private static final float JITTER = 0.1F;
    private static final float THIN_DECAY = 0.25F;
    private static final int THIN_CHANCE = 4;

    public final float colorR;
    public final float colorG;
    public final float colorB;
    public final float width;
    private final Vec3 source;
    private final Vec3 target;
    private final @Nullable WandSource wandSource;
    private final float phaseOffset;
    private final long seed;
    private int age;
    private boolean expired;

    public BoltInstance(double sx, double sy, double sz, double tx, double ty, double tz, int color, float width) {
        this(new Vec3(sx, sy, sz), new Vec3(tx, ty, tz), color, width, null);
    }

    public BoltInstance(Vec3 source, Vec3 target, int color, float width, @Nullable WandSource wandSource) {
        this.source = source;
        this.target = target;
        this.wandSource = wandSource;
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        this.width = width;
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
        return computePath(partialTick, null);
    }

    public Vec3 source() {
        return this.source;
    }

    public @Nullable Vec3 wandTip() {
        return this.wandSource == null ? null : this.wandSource.tip();
    }

    public List<PathStep> computePath(float partialTick, @Nullable Vec3 wandTip) {
        Vec3 span = this.target.subtract(wandTip == null ? this.source : wandTip);
        float length = (float) (span.length() * Math.PI);
        if (!Float.isFinite(length)) {
            return List.of();
        }
        int steps = Mth.clamp((int) length, 3, 512);
        float amplitude = (this.age + partialTick) * AMPLITUDE_PER_TICK;
        float stride = length / steps;
        Random noise = new Random(this.seed);
        List<PathStep> path = new ArrayList<>(steps);
        path.add(new PathStep(0.0, 0.0, 0.0, this.width));
        for (int i = 1; i < steps - 1; i++) {
            float phase = i * stride + this.phaseOffset;
            double x = span.x * i / steps + Mth.sin(phase / WAVE_PERIOD_X) * amplitude + sway(noise);
            double y = span.y * i / steps + Mth.sin(phase / WAVE_PERIOD_Y) * amplitude + sway(noise);
            double z = span.z * i / steps + Mth.sin(phase / WAVE_PERIOD_Z) * amplitude + sway(noise);
            float thin = noise.nextInt(THIN_CHANCE) == 0 ? 1.0F - this.age * THIN_DECAY : 1.0F;
            path.add(new PathStep(x, y, z, thin * this.width));
        }
        PathStep target = new PathStep(span.x, span.y, span.z, this.width);
        path.add(target);
        path.add(extrapolateEnd(path.get(path.size() - 2), target));
        if (wandTip != null) {
            // TubeMesh uses the first and last samples as tangents and draws between the interior samples.
            path.addFirst(extrapolateEnd(path.get(1), path.getFirst()));
        }
        return path;
    }

    static PathStep extrapolateEnd(PathStep previous, PathStep target) {
        return new PathStep(
                target.x() * 2.0 - previous.x(),
                target.y() * 2.0 - previous.y(),
                target.z() * 2.0 - previous.z(),
                target.width());
    }

    private static double sway(Random noise) {
        return (noise.nextFloat() - noise.nextFloat()) * JITTER;
    }

    public float alpha(float partialTick) {
        return Mth.clamp(1.0F - (this.age + partialTick) / MAX_AGE, 0.1F, 1.0F);
    }

    public record PathStep(double x, double y, double z, float width) {}
}
