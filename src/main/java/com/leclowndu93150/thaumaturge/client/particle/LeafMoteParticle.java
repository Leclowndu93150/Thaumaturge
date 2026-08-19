package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

public final class LeafMoteParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final int BASE_LIFETIME = 4;
    private static final float ALPHA = 0.6F;

    private LeafMoteParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ColorParticleOption options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.getRed(), options.getGreen(), options.getBlue());
        this.alpha = ALPHA;
        this.lifetime = BASE_LIFETIME + this.random.nextInt(4);
        this.quadSize = (0.8F + this.random.nextFloat() * 0.3F) * 0.1F;
    }

    @Override
    protected void update() {
        frame(this.age % FRAME_COUNT);
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("leaf_mote");

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new LeafMoteParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
