package com.leclowndu93150.thaumaturge.client.particle;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.particle.ParticleRenderType;

public final class TTParticleLayers {
    private static final ParticleSheet NITOR_CORE = TTParticleSheets.sheet("nitor_core");
    private static final ParticleSheet WISP_FLAME = TTParticleSheets.sheet("wisp_flame");

    private TTParticleLayers() {}

    public static boolean usesNitorAdditiveBlend(ParticleRenderType layer) {
        return layer == additive(NITOR_CORE) || layer == additiveSoft(WISP_FLAME);
    }

    public static Set<ParticleRenderType> orderNitorLayers(Set<ParticleRenderType> layers) {
        ParticleRenderType core = additive(NITOR_CORE);
        if (!layers.contains(core)) return layers;

        ParticleRenderType flame = additiveSoft(WISP_FLAME);
        for (ParticleRenderType layer : layers) {
            if (layer == core) return layers;
            if (layer == flame) break;
        }

        // Draw the core first so its visible texels occlude flames behind it.
        Set<ParticleRenderType> ordered = LinkedHashSet.newLinkedHashSet(layers.size());
        for (ParticleRenderType layer : layers) {
            if (layer == flame) ordered.add(core);
            ordered.add(layer);
        }
        return ordered;
    }

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
