package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

public final class GooDripParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final int BASE_LIFETIME = 4;
    private static final float ALPHA = 0.8F;

    private GooDripParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ColorParticleOption options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.getRed(), options.getGreen(), options.getBlue());
        this.alpha = ALPHA;
        this.lifetime = BASE_LIFETIME + this.random.nextInt(4);
        this.quadSize = (0.5F + this.random.nextFloat() * 0.2F) * 0.1F;
        frameByProgress();
    }

    @Override
    protected void update() {
        frameByProgress();
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("goo_drip");

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new GooDripParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
