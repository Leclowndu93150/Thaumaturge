package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class PrimalFlareParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final int LIFETIME = 8;
    private static final float FRICTION = 0.9F;
    private static final float PEAK_ALPHA = 0.8F;

    private PrimalFlareParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SimpleParticleType options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.rCol = this.random.nextFloat();
        this.gCol = this.random.nextFloat();
        this.bCol = this.random.nextFloat();
        this.lifetime = LIFETIME;
        this.friction = FRICTION;
        this.quadSize = (1.5F + this.random.nextFloat() * 2.0F) * 0.1F;
        this.alpha = 0.0F;
        frame(this.random.nextInt(FRAME_COUNT));
    }

    @Override
    protected void update() {
        this.alpha = PEAK_ALPHA * (1.0F - Math.abs(2.0F * progress() - 1.0F));
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("primal_flare");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new PrimalFlareParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
