package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.StabilizerRuneParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class StabilizerRuneParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final float START_ALPHA = 0.3F;
    private static final float FRICTION = 1.01F;

    private StabilizerRuneParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, StabilizerRuneParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(0.5F, 0.2F, 0.5F);
        this.lifetime = options.life() + this.random.nextInt(Math.max(1, options.life()));
        this.friction = FRICTION;
        this.quadSize = 0.1F;
        frame(this.random.nextInt(FRAME_COUNT));
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -1.0F : 1.0F);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = START_ALPHA * (1.0F - t);
        this.quadSize = Keyframes.sample(t, 0.1F, 1.0F);
    }

    public static final class Provider implements ParticleProvider<StabilizerRuneParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("stabilizer_rune");

        @Override
        public Particle createParticle(StabilizerRuneParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new StabilizerRuneParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
