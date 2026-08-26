package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.RandomSource;

public final class ShieldSparkParticle extends TCParticle {
    private static final int FRAME_COUNT = 9;

    private final float startAlpha;
    private final boolean additive;

    private ShieldSparkParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            ShieldSparkParticleOptions options,
            ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.startAlpha = options.alpha();
        this.additive = options.additive();
        this.alpha = this.startAlpha;
        this.lifetime = Math.max(1, options.age());
        this.quadSize = options.scale() * 0.1F;
        setDelay(options.delay());
        frameByProgress();
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = this.startAlpha * (1.0F - t);
        frame((int) (t * FRAME_COUNT));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.additive ? TCParticleLayers.additive(this.sheet) : TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<ShieldSparkParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("shield_spark");

        @Override
        public Particle createParticle(
                ShieldSparkParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            RandomSource random = level.getRandom();
            return new ShieldSparkParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
