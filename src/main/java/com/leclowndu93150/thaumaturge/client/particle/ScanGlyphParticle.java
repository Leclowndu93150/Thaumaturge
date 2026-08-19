package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.ScanGlyphParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class ScanGlyphParticle extends TCParticle {
    private static final int FRAME_COUNT = 15;
    private static final int LIFETIME = 44;
    private static final float SIZE = 0.9F;

    private final boolean additive;

    private ScanGlyphParticle(ClientLevel level, double x, double y, double z, ScanGlyphParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(options.color());
        this.additive = options.additive();
        this.lifetime = LIFETIME;
        this.quadSize = SIZE;
        this.alpha = 0.0F;
        setDelay(options.delay());
    }

    @Override
    protected void update() {
        frame(this.age % FRAME_COUNT);
        this.alpha = Keyframes.sample(progress(), 0.0F, 1.0F, 0.8F, 0.0F);
    }

    @Override
    public Layer getLayer() {
        return this.additive ? TCParticleLayers.additiveNoDepth(this.sheet) : TCParticleLayers.translucentNoDepth(this.sheet);
    }

    public static final class Provider implements ParticleProvider<ScanGlyphParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("scan_glyph");

        @Override
        public Particle createParticle(ScanGlyphParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new ScanGlyphParticle(level, x, y, z, options, SHEET);
        }
    }
}
