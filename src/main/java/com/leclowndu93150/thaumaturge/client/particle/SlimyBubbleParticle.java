package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SlimyBubbleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class SlimyBubbleParticle extends TCParticle {
    private static final int GROW_TICKS = 6;
    private static final int RISE_END_GAP = 4;
    private static final int GROW_LIFT_AGE = 5;
    private static final float GROW_LIFT = 0.1F;
    private static final double RISE_SPEED = 0.005;
    private static final int GROW_FRAME = 0;
    private static final int RISE_FRAME = 3;
    private static final int POP_END_FRAME = 6;

    private SlimyBubbleParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            SlimyBubbleParticleOptions options,
            ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.alpha = options.alpha();
        this.lifetime = options.age();
        this.quadSize = options.scale();
        frame(GROW_FRAME);
    }

    @Override
    protected void update() {
        if (this.age - 1 < GROW_TICKS) {
            frame(GROW_FRAME + this.age / 2);
            if (this.age == GROW_LIFT_AGE) {
                this.y += GROW_LIFT;
            }
        } else if (this.age < this.lifetime - RISE_END_GAP) {
            this.yd += RISE_SPEED;
            frame(RISE_FRAME + this.age % 4 / 2);
        } else {
            this.yd /= 2.0;
            frame(POP_END_FRAME - (this.lifetime - this.age) / 2);
        }
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<SlimyBubbleParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("slimy_bubble");

        @Override
        public Particle createParticle(
                SlimyBubbleParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz,
                RandomSource random) {
            return new SlimyBubbleParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
