package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public abstract class TCParticle extends SingleQuadParticle {
    private static final float SPIN_UNIT = (float) (2.0 * Math.PI * Math.PI / 180.0);
    private static final float WIND_STRENGTH = 0.1F;
    private static final float WIND_ANGLE_JITTER = 0.17F;

    protected final @Nullable ParticleSheet sheet;
    protected final @Nullable TextureAtlasSprite sprite;
    protected int frame;
    private int delay;
    private float spinPerTick;
    private double windX;
    private double windZ;

    protected TCParticle(
            ClientLevel level, double x, double y, double z, double vx, double vy, double vz, ParticleSheet sheet) {
        this(level, x, y, z, vx, vy, vz, sheet, null);
    }

    protected TCParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            TextureAtlasSprite sprite) {
        this(level, x, y, z, vx, vy, vz, null, sprite);
    }

    private TCParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            @Nullable ParticleSheet sheet,
            @Nullable TextureAtlasSprite sprite) {
        super(level, x, y, z, vx, vy, vz);
        this.sheet = sheet;
        this.sprite = sprite;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.gravity = 0.0F;
        this.quadSize = 0.1F;
        this.setSize(0.1F, 0.1F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.delay > 0) {
            this.delay--;
            return;
        }
        this.oRoll = this.roll;
        this.roll += this.spinPerTick;
        this.xd += this.windX;
        this.zd += this.windZ;
        super.tick();
        if (!this.removed) {
            update();
        }
    }

    protected abstract void update();

    protected float progress() {
        return Mth.clamp((float) this.age / this.lifetime, 0.0F, 1.0F);
    }

    protected void setDelay(int delay) {
        this.delay = Math.max(0, delay);
        if (this.delay > 0) {
            this.alpha = 0.0F;
        }
    }

    protected boolean delayed() {
        return this.delay > 0;
    }

    protected void setSpin(float startTurns, float speed) {
        this.roll = (float) (startTurns * Math.PI * 2.0);
        this.oRoll = this.roll;
        this.spinPerTick = speed * SPIN_UNIT;
    }

    protected void setMoonWind(double scale) {
        int phase = this.level.getMoonPhase();
        double angle = phase * (Math.PI / 4.0) + this.random.nextFloat() * WIND_ANGLE_JITTER;
        this.windX = Math.cos(angle) * WIND_STRENGTH * scale;
        this.windZ = Math.sin(angle) * WIND_STRENGTH * scale;
    }

    protected void drift(float strengthX, float strengthY, float strengthZ) {
        if (strengthX != 0.0F) this.xd += this.random.nextGaussian() * strengthX;
        if (strengthY != 0.0F) this.yd += this.random.nextGaussian() * strengthY;
        if (strengthZ != 0.0F) this.zd += this.random.nextGaussian() * strengthZ;
    }

    protected void frame(int index) {
        this.frame = Mth.clamp(index, 0, this.sheet == null ? 0 : this.sheet.frames() - 1);
    }

    protected void frameByProgress() {
        if (this.sheet != null) {
            frame((int) (progress() * this.sheet.frames()));
        }
    }

    protected int sheetFrames() {
        return this.sheet == null ? 1 : this.sheet.frames();
    }

    protected void setColor(int color) {
        this.rCol = ARGB32.red(color) / 255.0F;
        this.gCol = ARGB32.green(color) / 255.0F;
        this.bCol = ARGB32.blue(color) / 255.0F;
    }

    protected void lerpColor(float t, float r0, float g0, float b0, float r1, float g1, float b1) {
        this.rCol = Mth.lerp(t, r0, r1);
        this.gCol = Mth.lerp(t, g0, g1);
        this.bCol = Mth.lerp(t, b0, b1);
    }

    @Override
    protected float getU0() {
        return this.sheet != null ? this.sheet.u0(this.frame) : this.sprite != null ? this.sprite.getU0() : 0.0F;
    }

    @Override
    protected float getU1() {
        return this.sheet != null ? this.sheet.u1(this.frame) : this.sprite != null ? this.sprite.getU1() : 1.0F;
    }

    @Override
    protected float getV0() {
        return this.sheet != null ? 0.0F : this.sprite != null ? this.sprite.getV0() : 0.0F;
    }

    @Override
    protected float getV1() {
        return this.sheet != null ? 1.0F : this.sprite != null ? this.sprite.getV1() : 1.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return this.sheet != null ? TCParticleLayers.additiveNoDepth(this.sheet) : ParticleRenderType.TERRAIN_SHEET;
    }
}
