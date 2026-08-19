package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class CurseSmokeParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final int LIFETIME = 8;
    private static final float FRICTION = 0.9F;

    private float flickerFrom;
    private float flickerTo;

    private CurseSmokeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SimpleParticleType options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.rCol = 0.41F + this.random.nextFloat() * 0.2F;
        this.gCol = 0.0F;
        this.bCol = 0.019F + this.random.nextFloat() * 0.2F;
        this.lifetime = LIFETIME;
        this.friction = FRICTION;
        this.quadSize = (2.0F + this.random.nextFloat() * 4.0F) * 0.1F;
        this.alpha = 0.0F;
        this.flickerTo = this.random.nextFloat();
        frame(this.random.nextInt(FRAME_COUNT));
        setSpin(this.random.nextFloat(), 0.0F);
        setDelay(this.random.nextInt(4));
    }

    @Override
    protected void update() {
        if (this.age % 2 == 0) {
            this.flickerFrom = this.flickerTo;
            this.flickerTo = this.age >= this.lifetime - 2 ? 0.0F : this.random.nextFloat();
        }
        this.alpha = net.minecraft.util.Mth.lerp((this.age % 2) / 2.0F, this.flickerFrom, this.flickerTo);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("curse_smoke");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new CurseSmokeParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
