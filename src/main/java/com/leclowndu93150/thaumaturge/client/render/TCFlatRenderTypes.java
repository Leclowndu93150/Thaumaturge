package com.leclowndu93150.thaumaturge.client.render;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import java.util.function.Function;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class TCFlatRenderTypes {
    private static final Function<Identifier, RenderType> CUTOUT_FLAT = Util
            .memoize(texture -> RenderType.create("tc_cutout_flat", RenderSetup.builder(TCRenderPipelines.ENTITY_CUTOUT_FLAT).withTexture("Sampler0", texture).useLightmap().createRenderSetup()));

    private static final Function<Identifier, RenderType> TRANSLUCENT_FLAT = Util.memoize(texture -> RenderType.create("tc_translucent_flat",
            RenderSetup.builder(TCRenderPipelines.ENTITY_TRANSLUCENT_FLAT).withTexture("Sampler0", texture).useLightmap().sortOnUpload().createRenderSetup()));

    private static final Function<Identifier, RenderType> ADDITIVE_FLAT = Util.memoize(texture -> RenderType.create("tc_additive_flat",
            RenderSetup.builder(TCRenderPipelines.ENTITY_ADDITIVE_EMISSIVE).withTexture("Sampler0", texture).useLightmap().sortOnUpload().createRenderSetup()));

    private static final Function<Identifier, RenderType> TRANSLUCENT_FLAT_NO_DEPTH = Util.memoize(texture -> RenderType.create("tc_translucent_flat_no_depth",
            RenderSetup.builder(TCRenderPipelines.ENTITY_TRANSLUCENT_NO_DEPTH).withTexture("Sampler0", texture).useLightmap().sortOnUpload().createRenderSetup()));

    private TCFlatRenderTypes() {}

    public static RenderType entityCutoutFlat(Identifier texture) {
        return CUTOUT_FLAT.apply(texture);
    }

    public static RenderType entityTranslucentFlat(Identifier texture) {
        return TRANSLUCENT_FLAT.apply(texture);
    }

    public static RenderType entityAdditiveFlat(Identifier texture) {
        return ADDITIVE_FLAT.apply(texture);
    }

    public static RenderType entityTranslucentFlatNoDepth(Identifier texture) {
        return TRANSLUCENT_FLAT_NO_DEPTH.apply(texture);
    }
}
