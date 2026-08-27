package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class BoreBeamRenderType {
    public static final Identifier BEAM = TCIds.rl("textures/misc/beam1.png");

    public static final RenderType DRILL_BEAM = RenderType.create("thaumaturge_bore_beam",
            RenderSetup.builder(TCRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", BEAM).useLightmap().createRenderSetup());

    private BoreBeamRenderType() {}
}
