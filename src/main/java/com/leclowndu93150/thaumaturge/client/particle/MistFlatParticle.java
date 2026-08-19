package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.RandomSource;

public final class MistFlatParticle extends TCParticle {
    private static final int BASE_LIFETIME = 400;
    private static final double WIND_SCALE = 0.001;

    private MistFlatParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ColorParticleOption options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.getRed(), options.getGreen(), options.getBlue());
        this.lifetime = BASE_LIFETIME + this.random.nextInt(100);
        this.quadSize = 0.2F;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -1.0F : 1.0F);
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = 1.0F - t;
        this.quadSize = Keyframes.sample(t, 0.2F, 0.5F);
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("mist_flat");

        @Override
        public Particle createParticle(ColorParticleOption options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new MistFlatParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
