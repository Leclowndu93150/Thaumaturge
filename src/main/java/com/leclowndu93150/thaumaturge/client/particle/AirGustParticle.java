package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.AirGustParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class AirGustParticle extends TCParticle {
    private static final int FRAME_COUNT = 5;
    private static final int BASE_LIFETIME = 20;
    private static final float START_ALPHA = 0.5F;
    private static final float FRICTION = 0.75F;
    private static final float LIFT = -0.1F;

    private final float startSize;
    private final float endSize;

    private AirGustParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, AirGustParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.lifetime = BASE_LIFETIME + this.random.nextInt(10);
        this.friction = FRICTION;
        this.gravity = LIFT;
        this.startSize = options.scale() * 0.1F;
        this.endSize = this.startSize * 2.0F;
        this.quadSize = this.startSize;
        setSpin(this.random.nextFloat(), (float) this.random.nextGaussian() / 2.0F);
        frameByProgress();
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = START_ALPHA * (1.0F - t);
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
        frame((int) (t * FRAME_COUNT));
    }

    public static final class Provider implements ParticleProvider<AirGustParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("air_gust");

        @Override
        public Particle createParticle(AirGustParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new AirGustParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
