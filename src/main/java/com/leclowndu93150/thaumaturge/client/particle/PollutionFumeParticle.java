package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class PollutionFumeParticle extends TCParticle {
    private static final int BASE_LIFETIME = 100;
    private static final float START_ALPHA = 0.5F;
    private static final double RISE_SPEED = 0.02;
    private static final double SIDE_JITTER = 0.005;
    private static final double WIND_SCALE = 0.001;

    private PollutionFumeParticle(ClientLevel level, double x, double y, double z, ParticleSheet sheet) {
        super(level, x, y, z, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * SIDE_JITTER, RISE_SPEED, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * SIDE_JITTER,
                sheet);
        setColor(1.0F, 0.3F, 0.9F);
        this.lifetime = BASE_LIFETIME + this.random.nextInt(60);
        this.quadSize = 0.2F;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -1.0F : 1.0F);
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = START_ALPHA * (1.0F - t);
        this.quadSize = Keyframes.sample(t, 0.2F, 0.5F);
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("pollution_fume");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new PollutionFumeParticle(level, x, y, z, SHEET);
        }
    }
}
