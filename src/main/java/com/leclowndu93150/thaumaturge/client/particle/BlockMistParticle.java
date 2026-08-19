package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

public final class BlockMistParticle extends TCParticle {
    private static final int BASE_LIFETIME = 50;
    private static final float GRAVITY = 0.1F;
    private static final double WIND_SCALE = 0.001;

    private BlockMistParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ColorParticleOption options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.getRed(), options.getGreen(), options.getBlue());
        this.lifetime = BASE_LIFETIME + this.random.nextInt(25);
        this.gravity = GRAVITY;
        this.quadSize = 0.5F;
        this.alpha = 0.0F;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -1.0F : 1.0F);
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = Keyframes.sample(t, 0.0F, 0.5F, 0.4F, 0.3F, 0.2F, 0.1F, 0.0F);
        this.quadSize = Keyframes.sample(t, 0.5F, 0.1F);
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("block_mist");

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new BlockMistParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
