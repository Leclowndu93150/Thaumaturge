package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class SparkleParticle extends TCParticle {
    private static final int FRAMES_PER_ROW = 16;
    private static final int TOTAL_FRAMES = 32;
    private static final float SMALL_CHANCE = 0.2F;
    private static final int FLICKER_STEP = 4;
    private static final float DRIFT_X = 5.0E-4F;
    private static final float DRIFT_Y = 0.001F;
    private static final double WIND_SCALE = 5.0E-4;

    private final int rowOffset;
    private final boolean flicker;
    private final float baseSize;
    private final float peakSize;
    private float flickerFrom = 1.0F;
    private float flickerTo;

    private SparkleParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SparkleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        boolean small = this.random.nextFloat() < SMALL_CHANCE;
        this.rowOffset = small ? FRAMES_PER_ROW : 0;
        this.flicker = options.flicker();
        this.lifetime = options.baseAge() * 4 + this.random.nextInt(Math.max(1, options.baseAge()));
        this.friction = options.decay();
        this.gravity = options.gravity();
        this.baseSize = options.scale() * 0.1F;
        this.peakSize = this.baseSize * 2.0F;
        this.quadSize = this.baseSize;
        this.flickerTo = this.random.nextFloat();
        setDelay(options.delay());
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        drift(DRIFT_X, DRIFT_Y, DRIFT_X);
        frame(this.rowOffset + this.age % FRAMES_PER_ROW);
        float t = progress();
        if (this.flicker) {
            if (this.age % FLICKER_STEP == 0) {
                this.flickerFrom = this.flickerTo;
                this.flickerTo = t > 0.85F ? 0.0F : this.random.nextFloat();
            }
            this.alpha = Mth.lerp((float) (this.age % FLICKER_STEP) / FLICKER_STEP, this.flickerFrom, this.flickerTo);
            this.quadSize = Keyframes.sample(t, this.baseSize, this.peakSize);
        } else {
            this.alpha = 1.0F - Math.abs(2.0F * t - 1.0F);
            this.quadSize = Keyframes.sample(t, this.baseSize, this.peakSize, this.baseSize);
        }
    }

    public static final class Provider implements ParticleProvider<SparkleParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("sparkle");

        @Override
        public Particle createParticle(SparkleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SparkleParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
