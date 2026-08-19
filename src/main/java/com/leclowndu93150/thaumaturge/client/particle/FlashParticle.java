package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

public final class FlashParticle extends TCParticle {
    private static final int BASE_LIFETIME = 10;
    private static final float BASE_SIZE = 1.0F;

    private final float startSize;

    private FlashParticle(ClientLevel level, double x, double y, double z, ColorParticleOption options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(options.getRed(), options.getGreen(), options.getBlue());
        this.lifetime = BASE_LIFETIME + this.random.nextInt(5);
        this.startSize = BASE_SIZE + this.random.nextFloat() * 0.2F;
        this.quadSize = this.startSize;
        setSpin(this.random.nextFloat(), (float) this.random.nextGaussian());
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = 1.0F - t;
        this.quadSize = this.startSize * (1.0F - t);
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("flash");

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new FlashParticle(level, x, y, z, options, SHEET);
        }
    }
}
