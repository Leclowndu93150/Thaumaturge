package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

public final class PuffParticle extends TCParticle {
    private static final int FRAME_COUNT = 5;
    private static final int BASE_LIFETIME = 20;
    private static final float START_SIZE = 0.3F;
    private static final float ALPHA_END = 0.1F;
    private static final float FRICTION = 0.7F;

    private final float endSize;

    private PuffParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ColorParticleOption options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.getRed(), options.getGreen(), options.getBlue());
        this.lifetime = BASE_LIFETIME + this.random.nextInt(15);
        this.friction = FRICTION;
        this.endSize = 0.4F + this.random.nextFloat() * 0.3F;
        this.quadSize = START_SIZE;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -1.0F : 1.0F);
        frameByProgress();
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = Keyframes.sample(t, 1.0F, ALPHA_END);
        this.quadSize = Keyframes.sample(t, START_SIZE, this.endSize);
        frame((int) (t * FRAME_COUNT));
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("puff");

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new PuffParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
