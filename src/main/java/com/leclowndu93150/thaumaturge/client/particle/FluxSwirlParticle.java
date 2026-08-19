package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class FluxSwirlParticle extends TCParticle {
    private static final int FRAME_COUNT = 14;
    private static final int BASE_LIFETIME = 15;
    private static final float FRICTION = 0.9F;
    private static final float DRIFT = 0.0125F;

    private final float startSize;
    private final float endSize;

    private FluxSwirlParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, FluxSwirlParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.lifetime = BASE_LIFETIME + this.random.nextInt(10);
        this.friction = FRICTION;
        this.gravity = (float) (this.random.nextGaussian() * 0.1);
        this.startSize = options.scale() * 0.1F;
        this.endSize = options.endScale() * 0.1F;
        this.quadSize = this.startSize;
        this.alpha = 0.0F;
        setSpin(this.random.nextFloat(), (float) this.random.nextGaussian());
        setDelay(this.random.nextInt(4));
    }

    @Override
    protected void update() {
        drift(DRIFT, DRIFT, DRIFT);
        float t = progress();
        this.alpha = Keyframes.sample(t, 0.0F, 1.0F, 1.0F, 0.0F);
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
        frame(this.age % FRAME_COUNT);
    }

    public static final class Provider implements ParticleProvider<FluxSwirlParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("flux_swirl");

        @Override
        public Particle createParticle(FluxSwirlParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new FluxSwirlParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
