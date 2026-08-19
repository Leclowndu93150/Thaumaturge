package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.RiftShardParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class RiftShardParticle extends TCParticle {
    private static final int FRAME_COUNT = 16;
    private static final int BASE_LIFETIME = 16;
    private static final float FRICTION = 0.75F;
    private static final float DRIFT = 0.01F;

    private RiftShardParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RiftShardParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(0.25F, 0.25F, 1.0F);
        this.lifetime = BASE_LIFETIME + this.random.nextInt(16);
        this.friction = FRICTION;
        this.quadSize = options.scale() * 0.1F;
        frame(this.random.nextInt(FRAME_COUNT));
    }

    @Override
    protected void update() {
        drift(DRIFT, DRIFT, DRIFT);
        this.alpha = 1.0F - progress();
    }

    public static final class Provider implements ParticleProvider<RiftShardParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("rift_shard");

        @Override
        public Particle createParticle(RiftShardParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new RiftShardParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
