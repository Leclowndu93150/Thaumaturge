package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.WardFlashParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;

public final class WardFlashParticle extends TTParticle {
    private static final ParticleSheet SHEET = TTParticleSheets.sheet("ward_flash");
    private static final int FRAME_COUNT = 16;
    private static final int BASE_AGE = 12;
    private static final int AGE_JITTER = 5;
    private static final float SIZE_BASE = 0.7F;
    private static final float SIZE_SPREAD = 0.15F;
    private static final float SIZE_MIN = 0.2F;
    private static final float SIZE_MAX = 1.2F;
    private static final double EDGE_MARGIN = 0.1;
    private static final double HIT_NUDGE = 0.1;
    private static final double SURFACE_OFFSET = 0.005;
    private static final float RAMP_PORTION = 5.0F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;

    private final Quaternionf faceRotation;

    private WardFlashParticle(
            ClientLevel level, double x, double y, double z, WardFlashParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        Direction face = options.face();
        double px = Mth.floor(x) + placeAlong(face.getStepX(), options.hitX(), this.random);
        double py = Mth.floor(y) + placeAlong(face.getStepY(), options.hitY(), this.random);
        double pz = Mth.floor(z) + placeAlong(face.getStepZ(), options.hitZ(), this.random);
        setPos(px, py, pz);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.lifetime = BASE_AGE + this.random.nextInt(AGE_JITTER);
        this.quadSize = sizeForDeviation(this.random.nextGaussian());
        this.alpha = 0.0F;
        this.setSize(0.01F, 0.01F);
        this.faceRotation = new Quaternionf()
                .rotationTo(0.0F, 0.0F, 1.0F, face.getStepX(), face.getStepY(), face.getStepZ())
                .rotateZ((float) Math.toRadians(this.random.nextInt(360)));
    }

    static float sizeForDeviation(double gaussian) {
        return Mth.clamp(SIZE_BASE + (float) gaussian * SIZE_SPREAD, SIZE_MIN, SIZE_MAX);
    }

    static double placeAcross(double hit, RandomSource random) {
        return Mth.clamp(hit + (random.nextDouble() * 2.0 - 1.0) * HIT_NUDGE, EDGE_MARGIN, 1.0 - EDGE_MARGIN);
    }

    static double placeAlong(int faceStep, double hit, RandomSource random) {
        if (faceStep == 0) {
            return placeAcross(hit, random);
        }
        return faceStep > 0 ? 1.0 + SURFACE_OFFSET : -SURFACE_OFFSET;
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
    public ParticleRenderType getRenderType() {
        return TTParticleLayers.fxTranslucent(SHEET);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTickTime) {
        renderRotatedQuad(buffer, camera, new Quaternionf(this.faceRotation), partialTickTime);
    }

    public static final class Provider implements ParticleProvider<WardFlashParticleOptions> {
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
