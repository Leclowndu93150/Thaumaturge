package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class VentParticle extends TCParticle {
    private static final int FRAME_COUNT = 6;
    private static final float BASE_ALPHA = 0.4F;
    private static final float FRICTION = 0.85F;
    private static final float GROW_RATE = 1.15F;
    private static final float GROW_RATE_VARIANT = 1.2F;
    private static final float HEADING_SPEED = 0.125F;
    private static final float HEADING_JITTER = 0.0375F;
    private static final float RISE_ACCEL = 0.0025F;
    private static final int NEAR_RANGE = 25;
    private static final int FAR_RANGE = 50;

    private final boolean variant;
    private final float fullScale;
    private final float riseAccel;
    private float growth;

    private VentParticle(ClientLevel level, double x, double y, double z, VentParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, options.vx(), options.vy(), options.vz(), sheet);
        this.variant = options.variant();
        this.fullScale = options.scale();
        this.growth = (this.random.nextFloat() * 0.1F + 0.05F) * this.fullScale;
        this.lifetime = Integer.MAX_VALUE;
        this.hasPhysics = true;
        this.friction = FRICTION;
        this.setSize(0.02F, 0.02F);
        setColor(options.color());
        if (this.variant) {
            this.rCol = Mth.clamp(this.rCol + (float) this.random.nextGaussian() * 0.05F, 0.0F, 1.0F);
            this.gCol = Mth.clamp(this.gCol + (float) this.random.nextGaussian() * 0.05F, 0.0F, 1.0F);
            this.bCol = Mth.clamp(this.bCol + (float) this.random.nextGaussian() * 0.05F, 0.0F, 1.0F);
            this.riseAccel = (float) (this.random.nextGaussian() * 0.0075);
        } else {
            this.riseAccel = RISE_ACCEL;
            aimAlong(options.vx(), options.vy(), options.vz());
        }
        removeIfFarFromCamera(x, y, z);
    }

    private void aimAlong(double vx, double vy, double vz) {
        Vec3 direction = new Vec3(vx, vy, vz);
        if (direction.lengthSqr() < 1.0E-12) {
            return;
        }
        direction = direction.normalize();
        this.xd = (direction.x + this.random.nextGaussian() * signedJitter()) * HEADING_SPEED;
        this.yd = (direction.y + this.random.nextGaussian() * signedJitter()) * HEADING_SPEED;
        this.zd = (direction.z + this.random.nextGaussian() * signedJitter()) * HEADING_SPEED;
    }

    private double signedJitter() {
        return this.random.nextBoolean() ? -HEADING_JITTER : HEADING_JITTER;
    }

    private void removeIfFarFromCamera(double x, double y, double z) {
        int range = Minecraft.getInstance().options.graphicsPreset().get() == GraphicsPreset.FAST ? NEAR_RANGE : FAR_RANGE;
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        if (camera.distanceToSqr(x, y, z) > (double) range * range) {
            remove();
        }
    }

    @Override
    public void tick() {
        this.yd += this.riseAccel;
        super.tick();
        if (this.removed) {
            return;
        }
        this.growth = Math.min(this.growth * (this.variant ? GROW_RATE_VARIANT : GROW_RATE), this.fullScale);
        if (this.growth >= this.fullScale) {
            remove();
        }
    }

    @Override
    protected void update() {
        float grown = this.growth / this.fullScale;
        this.alpha = BASE_ALPHA * (1.0F - grown);
        this.quadSize = 0.3F * this.growth;
        frame((int) (1.0F + grown * 4.0F));
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<VentParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("vent");

        @Override
        public Particle createParticle(VentParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new VentParticle(level, x, y, z, options, SHEET);
        }
    }
}
