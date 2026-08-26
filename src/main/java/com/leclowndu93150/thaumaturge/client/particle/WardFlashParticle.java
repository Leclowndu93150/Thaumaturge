package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.WardFlashParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public final class WardFlashParticle extends TCParticle {
    private static final int FRAME_COUNT = 16;
    private static final int BASE_AGE = 12;
    private static final int AGE_JITTER = 5;
    private static final float BASE_SCALE = 1.4F;
    private static final float SCALE_JITTER = 0.3F;
    private static final float FACE_OFFSET = 0.505F;
    private static final float HIT_JITTER = 0.2F;
    private static final float HIT_CLAMP = 0.4F;
    private static final float RAMP_PORTION = 5.0F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;

    private final Quaternionf faceRotation;

    private WardFlashParticle(
            ClientLevel level, double x, double y, double z, WardFlashParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        Direction face = options.face();
        float sx = Mth.clamp(options.hitX() - 0.6F + this.random.nextFloat() * HIT_JITTER, -HIT_CLAMP, HIT_CLAMP);
        float sy = Mth.clamp(options.hitY() - 0.6F + this.random.nextFloat() * HIT_JITTER, -HIT_CLAMP, HIT_CLAMP);
        float sz = Mth.clamp(options.hitZ() - 0.6F + this.random.nextFloat() * HIT_JITTER, -HIT_CLAMP, HIT_CLAMP);
        if (face.getStepX() != 0) {
            sx = 0.0F;
        }
        if (face.getStepY() != 0) {
            sy = 0.0F;
        }
        if (face.getStepZ() != 0) {
            sz = 0.0F;
        }
        setPos(
                x + sx + face.getStepX() * FACE_OFFSET,
                y + sy + face.getStepY() * FACE_OFFSET,
                z + sz + face.getStepZ() * FACE_OFFSET);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.lifetime = BASE_AGE + this.random.nextInt(AGE_JITTER);
        this.quadSize = 0.5F * (float) (BASE_SCALE + this.random.nextGaussian() * SCALE_JITTER);
        this.alpha = 0.0F;
        this.setSize(0.01F, 0.01F);
        this.faceRotation = new Quaternionf()
                .rotationTo(0.0F, 0.0F, 1.0F, face.getStepX(), face.getStepY(), face.getStepZ())
                .rotateZ((float) Math.toRadians(this.random.nextInt(360)));
    }

    @Override
    protected void update() {
        float threshold = this.lifetime / RAMP_PORTION;
        float raw = this.age <= threshold ? this.age / threshold : (float) (this.lifetime - this.age) / this.lifetime;
        this.alpha = raw / 2.0F;
        frame(Math.min(FRAME_COUNT - 1, (int) (FRAME_COUNT * progress())));
    }

    @Override
    protected int getLightColor(float partialTick) {
        return EMISSIVE_LIGHT;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTickTime) {
        renderRotatedQuad(buffer, camera, new Quaternionf(this.faceRotation), partialTickTime);
    }

    public static final class Provider implements ParticleProvider<WardFlashParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("ward_flash");

        @Override
        public Particle createParticle(
                WardFlashParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            return new WardFlashParticle(level, x, y, z, options, SHEET);
        }
    }
}
