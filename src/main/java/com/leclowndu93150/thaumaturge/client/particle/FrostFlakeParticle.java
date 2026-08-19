package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.FrostFlakeParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class FrostFlakeParticle extends TCParticle {
    private static final int BASE_LIFETIME = 40;
    private static final float FRICTION = 0.8F;
    private static final float GRAVITY = 0.033F;
    private static final float DRIFT_XZ = 0.0025F;
    private static final float DRIFT_Y = 1.0E-4F;

    private FrostFlakeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, FrostFlakeParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.lifetime = BASE_LIFETIME + this.random.nextInt(40);
        this.friction = FRICTION;
        this.gravity = GRAVITY;
        this.quadSize = options.scale() * 0.1F;
        setSpin(this.random.nextFloat() * 3.0F, (float) this.random.nextGaussian() / 4.0F);
    }

    @Override
    protected void update() {
        drift(DRIFT_XZ, DRIFT_Y, DRIFT_XZ);
        this.alpha = 1.0F - progress();
    }

    public static final class Provider implements ParticleProvider<FrostFlakeParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("frost_flake");

        @Override
        public Particle createParticle(FrostFlakeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new FrostFlakeParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
