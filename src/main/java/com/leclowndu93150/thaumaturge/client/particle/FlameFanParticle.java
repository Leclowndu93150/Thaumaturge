package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class FlameFanParticle extends TCParticle {
    private static final int FRAME_COUNT = 10;
    private static final int LIFETIME = 10;
    private static final float FRICTION = 0.75F;

    private FlameFanParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, FlameFanParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.alpha = options.alpha();
        this.lifetime = LIFETIME;
        this.friction = FRICTION;
        this.gravity = options.lift();
        this.quadSize = options.scale() * 0.1F;
        frameByProgress();
    }

    @Override
    protected void update() {
        frame((int) (progress() * FRAME_COUNT));
    }

    public static final class Provider implements ParticleProvider<FlameFanParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("flame_fan");

        @Override
        public Particle createParticle(FlameFanParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new FlameFanParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
