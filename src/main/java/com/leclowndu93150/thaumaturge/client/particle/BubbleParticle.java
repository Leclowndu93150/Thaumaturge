package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class BubbleParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final int BUBBLE_FRAME = 0;
    private static final int POP_FRAME_1 = 1;
    private static final int POP_FRAME_2 = 2;
    private static final int DROPLET_FRAME = 3;
    private static final int POP_TICKS = 2;
    private static final float DRIFT = 0.001F;

    private final int holdFrame;
    private final float startSize;
    private final float endSize;

    private BubbleParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, BubbleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.alpha = options.alpha();
        this.lifetime = Math.max(POP_TICKS + 1, options.age());
        this.gravity = options.buoyancy();
        this.startSize = options.scale() * 0.1F;
        this.quadSize = this.startSize;
        this.endSize = this.startSize / 2.0F;
        this.holdFrame = options.droplet() ? DROPLET_FRAME : BUBBLE_FRAME;
        frame(this.holdFrame);
    }

    @Override
    protected void update() {
        drift(DRIFT, DRIFT, DRIFT);
        int remaining = this.lifetime - this.age;
        if (remaining <= 0) {
            frame(POP_FRAME_2);
        } else if (remaining == 1) {
            frame(POP_FRAME_1);
        } else {
            frame(this.holdFrame);
        }
        this.quadSize = Keyframes.sample(progress(), this.startSize, this.endSize);
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<BubbleParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("bubble");

        @Override
        public Particle createParticle(BubbleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new BubbleParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
