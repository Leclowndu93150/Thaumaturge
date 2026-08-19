package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class HealFlashParticle extends TCParticle {
    private static final int BASE_LIFETIME = 10;
    private static final float FRICTION = 0.8F;
    private static final float PEAK_ALPHA = 0.7F;
    private static final float DRIFT = 0.0125F;

    private final float startSize;
    private final float endSize;

    private HealFlashParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SimpleParticleType options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.lifetime = BASE_LIFETIME + this.random.nextInt(10);
        this.friction = FRICTION;
        this.gravity = (float) (this.random.nextGaussian() * 0.1);
        this.startSize = this.random.nextFloat() * 0.2F;
        this.endSize = this.random.nextFloat() * 0.1F;
        this.quadSize = this.startSize;
        this.alpha = 0.0F;
        setSpin(this.random.nextFloat(), (float) this.random.nextGaussian());
        setDelay(this.random.nextInt(4));
    }

    @Override
    protected void update() {
        drift(DRIFT, DRIFT, DRIFT);
        float t = progress();
        this.alpha = PEAK_ALPHA * Keyframes.sample(t, 0.0F, 1.0F, 1.0F, 0.0F);
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("heal_flash");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new HealFlashParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
