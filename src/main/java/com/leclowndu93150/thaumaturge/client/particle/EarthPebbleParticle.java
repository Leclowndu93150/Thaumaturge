package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.EarthPebbleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class EarthPebbleParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final int BASE_LIFETIME = 20;
    private static final float FRICTION = 0.9F;
    private static final float GRAVITY = 0.4F;

    private final float startSize;

    private EarthPebbleParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, EarthPebbleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.lifetime = BASE_LIFETIME + this.random.nextInt(10);
        this.friction = FRICTION;
        this.gravity = GRAVITY;
        this.startSize = options.scale() * 0.1F;
        this.quadSize = this.startSize;
        frame(this.random.nextInt(FRAME_COUNT));
        setSpin(this.random.nextFloat(), (float) this.random.nextGaussian());
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = 1.0F - t;
        this.quadSize = Keyframes.sample(t, this.startSize, this.startSize / 2.0F);
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<EarthPebbleParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("earth_pebble");

        @Override
        public Particle createParticle(EarthPebbleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new EarthPebbleParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
