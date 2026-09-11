package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.particle.ParticleRenderType;

public final class TCParticleLayers {
    private TCParticleLayers() {}

    public static ParticleRenderType additive(ParticleSheet sheet) {
        return sheet.layer(ParticleSheet.Mode.ADDITIVE);
    }

    public static ParticleRenderType additiveSoft(ParticleSheet sheet) {
        return sheet.layer(ParticleSheet.Mode.ADDITIVE_SOFT);
    }

    public static ParticleRenderType translucent(ParticleSheet sheet) {
        return sheet.layer(ParticleSheet.Mode.TRANSLUCENT);
    }

    public static ParticleRenderType additiveNoDepth(ParticleSheet sheet) {
        return sheet.layer(ParticleSheet.Mode.ADDITIVE_NO_DEPTH);
    }

    public static ParticleRenderType translucentNoDepth(ParticleSheet sheet) {
        return sheet.layer(ParticleSheet.Mode.TRANSLUCENT_NO_DEPTH);
    }
}
