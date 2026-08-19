package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public final class PechCurseParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final int BASE_LIFETIME = 50;
    private static final float START_ALPHA = 0.75F;
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final float endR;
    private final float endB;
    private final float startSize;
    private final float endSize;
    private final FacingCameraMode facing;

    private PechCurseParticle(ClientLevel level, double x, double y, double z, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(0.9F, 0.1F, 0.5F);
        this.endR = 0.1F + this.random.nextFloat() * 0.1F;
        this.endB = 0.5F + this.random.nextFloat() * 0.1F;
        this.lifetime = BASE_LIFETIME + this.random.nextInt(50);
        this.startSize = 0.3F;
        this.endSize = (5.0F + this.random.nextFloat() * 2.0F) * 0.1F;
        this.quadSize = this.startSize;
        frame(this.random.nextInt(FRAME_COUNT));
        float spinSpeed = 3.0F + this.random.nextFloat() * 3.0F;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -spinSpeed : spinSpeed);
        float yaw = 90.0F * (float) this.random.nextGaussian() * DEG_TO_RAD;
        float pitch = 90.0F * (float) this.random.nextGaussian() * DEG_TO_RAD;
        this.facing = (target, camera, partialTick) -> target.rotationYXZ(-yaw, pitch, 0.0F);
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = START_ALPHA * (1.0F - t);
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
        lerpColor(t, 0.9F, 0.1F, 0.5F, this.endR, 0.0F, this.endB);
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return this.facing;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("pech_curse");

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new PechCurseParticle(level, x, y, z, SHEET);
        }
    }
}
