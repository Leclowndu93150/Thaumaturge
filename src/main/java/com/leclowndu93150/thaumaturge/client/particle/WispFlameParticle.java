package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class WispFlameParticle extends TCParticle {
    private static final int FRAME_COUNT = 8;
    private static final int BASE_LIFETIME = 10;
    private static final float DRIFT = 0.0025F;

    private final float startSize;
    private final float endSize;

    private WispFlameParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, WispFlameParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.alpha = options.alpha();
        this.lifetime = BASE_LIFETIME + this.random.nextInt(5);
        this.startSize = options.scale() * 0.1F;
        this.endSize = options.endScale() * 0.1F;
        this.quadSize = this.startSize;
        setDelay(options.delay());
    }

    @Override
    protected void update() {
        drift(DRIFT, 0.0F, DRIFT);
        frame(this.age % FRAME_COUNT);
        this.quadSize = Keyframes.sample(progress(), this.startSize, this.endSize);
    }

    public static final class Provider implements ParticleProvider<WispFlameParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("wisp_flame");

        @Override
        public Particle createParticle(WispFlameParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new WispFlameParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
