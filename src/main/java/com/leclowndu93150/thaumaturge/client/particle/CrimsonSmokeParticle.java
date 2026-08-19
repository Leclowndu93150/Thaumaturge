package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class CrimsonSmokeParticle extends TCParticle {
    private static final int BASE_LIFETIME = 10;
    private static final float ALPHA = 0.8F;
    private static final float END_R = 0.6F;

    private CrimsonSmokeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.lifetime = BASE_LIFETIME + this.random.nextInt(10);
        this.alpha = ALPHA;
        this.quadSize = (3.0F + this.random.nextFloat() * 2.0F) * 0.1F;
        frameByProgress();
    }

    @Override
    protected void update() {
        float t = progress();
        lerpColor(t, 1.0F, 1.0F, 1.0F, END_R, 0.0F, 0.0F);
        frameByProgress();
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("crimson_smoke");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new CrimsonSmokeParticle(level, x, y, z, vx, vy, vz, SHEET);
        }
    }
}
