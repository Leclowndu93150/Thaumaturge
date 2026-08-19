package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class GolemTrailParticle extends TCParticle {
    private static final int BASE_LIFETIME = 20;
    private static final float START_ALPHA = 0.3F;
    private static final double WIND_SCALE = 0.001;

    private GolemTrailParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.lifetime = BASE_LIFETIME + this.random.nextInt(5);
        this.quadSize = 0.15F;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -1.0F : 1.0F);
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = START_ALPHA * (1.0F - t);
        this.quadSize = Keyframes.sample(t, 0.15F, 0.3F, 0.8F);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("golem_trail");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new GolemTrailParticle(level, x, y, z, vx, vy, vz, SHEET);
        }
    }
}
