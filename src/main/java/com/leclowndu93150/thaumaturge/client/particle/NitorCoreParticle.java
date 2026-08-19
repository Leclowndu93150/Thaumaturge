package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.NitorCoreParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;

public final class NitorCoreParticle extends SingleQuadParticle {
    private final ParticleSheet sheet;
    private static final int LIFETIME = 10;
    private static final float QUAD_RADIUS = 0.1F;
    private final float midBoost;

    private NitorCoreParticle(
            ClientLevel level, double x, double y, double z, NitorCoreParticleOptions data, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.sheet = sheet;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.rCol = data.r();
        this.gCol = data.g();
        this.bCol = data.b();
        this.alpha = 1.0F;
        this.lifetime = LIFETIME;
        this.midBoost = 1.0F + (float) level.getRandom().nextGaussian() * 0.1F;
        this.quadSize = 1.0F;
        this.hasPhysics = false;
        this.gravity = 0.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.xd += (this.random.nextDouble() - 0.5) * 2.0E-4;
        this.yd += (this.random.nextDouble() - 0.5) * 2.0E-4;
        this.zd += (this.random.nextDouble() - 0.5) * 2.0E-4;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.98;
        this.yd *= 0.98;
        this.zd *= 0.98;
        float frac = (float) this.age / this.lifetime;
        if (frac < 0.5F) {
            this.quadSize = Mth.lerp(frac * 2.0F, 1.0F, this.midBoost);
        } else {
            this.quadSize = Mth.lerp((frac - 0.5F) * 2.0F, this.midBoost, 1.0F);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return TCParticleLayers.additive(this.sheet);
    }

    @Override
    protected float getU0() {
        return 0.0F;
    }

    @Override
    protected float getU1() {
        return 1.0F;
    }

    @Override
    protected float getV0() {
        return 0.0F;
    }

    @Override
    protected float getV1() {
        return 1.0F;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * QUAD_RADIUS;
    }

    public static final class Provider implements ParticleProvider<NitorCoreParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("nitor_core");

        @Override
        public Particle createParticle(
                NitorCoreParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xAux,
                double yAux,
                double zAux) {
            return new NitorCoreParticle(level, x, y, z, options, SHEET);
        }
    }
}
