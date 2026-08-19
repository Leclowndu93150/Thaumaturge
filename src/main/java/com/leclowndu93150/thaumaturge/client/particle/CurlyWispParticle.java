package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.CurlyWispParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

public final class CurlyWispParticle extends TCParticle {
    private static final int FRAME_COUNT = 4;
    private static final float END_R = 0.1F;
    private static final float END_G = 0.0F;
    private static final float END_B = 0.1F;
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final float startR;
    private final float startG;
    private final float startB;
    private final float startAlpha;
    private final float startSize;
    private final float endSize;
    private final FacingCameraMode facing;

    private CurlyWispParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, CurlyWispParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.startR = ARGB.red(options.color()) / 255.0F;
        this.startG = ARGB.green(options.color()) / 255.0F;
        this.startB = ARGB.blue(options.color()) / 255.0F;
        this.startAlpha = options.alpha();
        this.lifetime = 25 + this.random.nextInt(20 + 20 * options.seed());
        this.startSize = 0.5F * options.scale();
        this.endSize = (1.0F + this.random.nextFloat() * 0.4F) * options.scale();
        this.quadSize = this.startSize;
        frame(this.random.nextInt(FRAME_COUNT));
        float spinSpeed = 2.0F + this.random.nextFloat() * 2.0F;
        setSpin(this.random.nextFloat(), this.random.nextBoolean() ? -spinSpeed : spinSpeed);
        setDelay(options.delay());
        if (options.seed() > 0 && this.random.nextBoolean()) {
            float yaw = 90.0F * (float) this.random.nextGaussian() * DEG_TO_RAD;
            float pitch = 90.0F * (float) this.random.nextGaussian() * DEG_TO_RAD;
            this.facing = (target, camera, partialTick) -> target.rotationYXZ(-yaw, pitch, 0.0F);
        } else {
            this.facing = FacingCameraMode.LOOKAT_XYZ;
        }
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = this.startAlpha * (1.0F - t);
        this.quadSize = Keyframes.sample(t, this.startSize, this.endSize);
        lerpColor(t, this.startR, this.startG, this.startB, END_R, END_G, END_B);
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return this.facing;
    }

    public static final class Provider implements ParticleProvider<CurlyWispParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("curly_wisp");

        @Override
        public Particle createParticle(CurlyWispParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new CurlyWispParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
