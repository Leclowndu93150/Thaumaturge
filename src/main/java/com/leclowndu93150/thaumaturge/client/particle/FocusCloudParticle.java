package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

public final class FocusCloudParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final int BASE_LIFETIME = 20;
    private static final float PEAK_ALPHA = 0.66F;
    private static final float FRICTION = 0.99F;
    private static final double WIND_SCALE = 0.001;

    private final float startSize;
    private final float endSize;

    private FocusCloudParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ColorParticleOption options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.getRed(), options.getGreen(), options.getBlue());
        this.lifetime = BASE_LIFETIME + this.random.nextInt(10);
        this.friction = FRICTION;
        this.startSize = (5.0F + this.random.nextFloat()) * 0.1F;
        this.endSize = (10.0F + this.random.nextFloat()) * 0.1F;
        this.quadSize = this.startSize;
        this.alpha = 0.0F;
        frame(this.random.nextInt(FRAME_COUNT));
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -0.25F : 0.25F);
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = PEAK_ALPHA * (1.0F - Math.abs(2.0F * t - 1.0F));
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("focus_cloud");

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new FocusCloudParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
