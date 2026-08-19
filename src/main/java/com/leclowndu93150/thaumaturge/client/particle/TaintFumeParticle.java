package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class TaintFumeParticle extends TCParticle {
    private static final int FRAME_COUNT = 3;
    private static final int BASE_LIFETIME = 80;
    private static final float START_ALPHA = 0.75F;
    private static final float FRICTION = 0.975F;
    private static final float GRAVITY = 0.2F;

    private final float startSize;
    private final float endSize;

    private TaintFumeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, TaintFumeParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        if (options.color() == TaintFumeParticleOptions.RANDOM_COLOR) {
            this.rCol = 0.4F + this.random.nextFloat() * 0.2F;
            this.gCol = 0.1F + this.random.nextFloat() * 0.3F;
            this.bCol = 0.5F + this.random.nextFloat() * 0.2F;
            this.lifetime = BASE_LIFETIME + this.random.nextInt(20);
            this.friction = FRICTION;
            this.gravity = GRAVITY;
        } else {
            setColor(options.color());
            this.lifetime = 10 + this.random.nextInt(10);
            this.friction = 0.9F;
        }
        this.startSize = options.scale() * 0.1F;
        this.endSize = this.startSize / 4.0F;
        this.quadSize = this.startSize;
        frame(this.random.nextInt(FRAME_COUNT));
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -1.0F : 1.0F);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = START_ALPHA * (1.0F - t);
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<TaintFumeParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("taint_fume");

        @Override
        public Particle createParticle(TaintFumeParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new TaintFumeParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
