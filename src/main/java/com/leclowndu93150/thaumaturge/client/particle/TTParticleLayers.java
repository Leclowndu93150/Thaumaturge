package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import net.minecraft.client.particle.SingleQuadParticle;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;

public final class TTParticleLayers {
    public static final SingleQuadParticle.Layer ORB_GLOW_TRANSLUCENT = new SingleQuadParticle.Layer(true, ParticleTextures.ORB_GLOW, TTRenderPipelines.FX_TRANSLUCENT);
    private TTParticleLayers() {}

    public static SingleQuadParticle.Layer additive(ParticleSheet sheet) {
        return sheet.layer(true, TTRenderPipelines.FX_ADDITIVE);
    }

    public static SingleQuadParticle.Layer translucent(ParticleSheet sheet) {
        return sheet.layer(true, TTRenderPipelines.FX_TRANSLUCENT);
    }

    public static SingleQuadParticle.Layer additiveNoDepth(ParticleSheet sheet) {
        return sheet.layer(true, TTRenderPipelines.FX_ADDITIVE_NO_DEPTH);
    }

    public static SingleQuadParticle.Layer translucentNoDepth(ParticleSheet sheet) {
        return sheet.layer(true, TTRenderPipelines.FX_TRANSLUCENT_NO_DEPTH);
    }
}
