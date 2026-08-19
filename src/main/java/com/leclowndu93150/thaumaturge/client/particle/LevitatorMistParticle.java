package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class LevitatorMistParticle extends TCParticle {
    private static final int BASE_LIFETIME = 200;
    private static final float START_ALPHA = 0.3F;
    private static final float START_SIZE = 0.2F;
    private static final float END_SIZE = 0.5F;

    private LevitatorMistParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(0.5F, 0.5F, 0.2F);
        this.lifetime = BASE_LIFETIME + this.random.nextInt(100);
        this.quadSize = START_SIZE;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -1.0F : 1.0F);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = START_ALPHA * (1.0F - t);
        this.quadSize = Keyframes.sample(t, START_SIZE, END_SIZE);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("levitator_mist");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new LevitatorMistParticle(level, x, y, z, vx, vy, vz, SHEET);
        }
    }
}
