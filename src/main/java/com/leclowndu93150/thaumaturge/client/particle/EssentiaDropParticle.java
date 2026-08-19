package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.EssentiaDropParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

public final class EssentiaDropParticle extends SingleQuadParticle {
    private final ParticleSheet sheet;
    private final float baseAlpha;
    private final float baseScale;

    private EssentiaDropParticle(ClientLevel level, double x, double y, double z, EssentiaDropParticleOptions data, ParticleSheet sheet) {
        super(level, x, y, z, level.getRandom().nextGaussian() * 0.005, level.getRandom().nextGaussian() * 0.005, level.getRandom().nextGaussian() * 0.005, null);
        this.sheet = sheet;
        Vector3f rgb = ARGB.vector3fFromRGB24(data.color());
        this.rCol = rgb.x();
        this.gCol = rgb.y();
        this.bCol = rgb.z();
        this.baseAlpha = data.alpha();
        this.alpha = data.alpha();
        this.lifetime = 20 + level.getRandom().nextInt(10);
        this.baseScale = 0.4F + level.getRandom().nextFloat() * 0.2F;
        this.quadSize = this.baseScale;
        this.gravity = 0.01F;
        this.hasPhysics = true;
        this.friction = 0.98F;
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
        this.yd -= 0.04 * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        float fade = 1.0F - (float) this.age / this.lifetime;
        this.alpha = this.baseAlpha * fade;
        this.quadSize = this.baseScale * (0.5F + fade * 0.5F);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
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

    public static final class Provider implements ParticleProvider<EssentiaDropParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("essentia_drop");

        @Override
        public Particle createParticle(EssentiaDropParticleOptions options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new EssentiaDropParticle(level, x, y, z, options, SHEET);
        }
    }
}
