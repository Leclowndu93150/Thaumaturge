package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.LightningFlashParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class LightningFlashParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final int BASE_LIFETIME = 5;

    private final float startAlpha;

    private LightningFlashParticle(ClientLevel level, double x, double y, double z, LightningFlashParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(options.color());
        this.startAlpha = options.alpha();
        this.alpha = this.startAlpha;
        this.lifetime = BASE_LIFETIME + this.random.nextInt(5);
        this.quadSize = options.scale() * 0.1F;
        frame(this.random.nextInt(FRAME_COUNT));
        setSpin(this.random.nextFloat(), 0.0F);
    }

    @Override
    protected void update() {
        this.alpha = this.startAlpha * (1.0F - progress());
    }

    public static final class Provider implements ParticleProvider<LightningFlashParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("lightning_flash");

        @Override
        public Particle createParticle(LightningFlashParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new LightningFlashParticle(level, x, y, z, options, SHEET);
        }
    }
}
