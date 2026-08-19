package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class SparkParticle extends TCParticle {
    private static final int ROW_COUNT = 3;
    private static final int FRAMES_PER_ROW = 8;
    private static final int TOTAL_FRAMES = ROW_COUNT * FRAMES_PER_ROW;
    private static final int BASE_LIFETIME = 5;

    private final int rowOffset;
    private final boolean mirrored;

    private SparkParticle(ClientLevel level, double x, double y, double z, SparkParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(options.color());
        this.alpha = options.alpha();
        this.lifetime = BASE_LIFETIME + this.random.nextInt(5);
        this.quadSize = options.scale() * 0.1F;
        this.rowOffset = this.random.nextInt(ROW_COUNT) * FRAMES_PER_ROW;
        this.mirrored = this.random.nextBoolean();
        frame(this.rowOffset);
    }

    @Override
    protected void update() {
        frame(this.rowOffset + this.age % FRAMES_PER_ROW);
    }

    @Override
    protected float getU0() {
        return this.mirrored ? super.getU1() : super.getU0();
    }

    @Override
    protected float getU1() {
        return this.mirrored ? super.getU0() : super.getU1();
    }

    public static final class Provider implements ParticleProvider<SparkParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("spark");

        @Override
        public Particle createParticle(SparkParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SparkParticle(level, x, y, z, options, SHEET);
        }
    }
}
