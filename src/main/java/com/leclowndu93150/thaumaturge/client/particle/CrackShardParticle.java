package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.CrackShardParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class CrackShardParticle extends TCParticle {
    private static final int TOTAL_FRAMES = 12;
    private static final int FRAMES_PER_VARIANT = 3;
    private static final float FRICTION = 0.8F;

    private final int frameBase;

    private CrackShardParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, CrackShardParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.lifetime = Math.max(1, options.age());
        this.friction = FRICTION;
        this.quadSize = options.scale() * 0.1F;
        this.frameBase = (options.variant() % 4) * FRAMES_PER_VARIANT;
        frame(this.frameBase);
    }

    @Override
    protected void update() {
        frame(this.frameBase + (int) (progress() * FRAMES_PER_VARIANT));
    }

    public static final class Provider implements ParticleProvider<CrackShardParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("crack_shard");

        @Override
        public Particle createParticle(CrackShardParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new CrackShardParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
