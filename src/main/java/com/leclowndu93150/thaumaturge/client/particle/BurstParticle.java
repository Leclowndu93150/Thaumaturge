package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.BurstParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class BurstParticle extends TCParticle {
    private static final int FRAME_COUNT = 31;

    private BurstParticle(ClientLevel level, double x, double y, double z, BurstParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.lifetime = FRAME_COUNT;
        this.quadSize = options.scale() * 0.1F;
    }

    @Override
    protected void update() {
        frameByProgress();
    }

    public static final class Provider implements ParticleProvider<BurstParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("burst");

        @Override
        public Particle createParticle(BurstParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new BurstParticle(level, x, y, z, options, SHEET);
        }
    }
}
