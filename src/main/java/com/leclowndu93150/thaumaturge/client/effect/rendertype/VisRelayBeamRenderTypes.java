package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class VisRelayBeamRenderTypes {
    public static final Identifier BEAM_TEXTURE = TTIds.rl("textures/misc/beam1.png");

    public static final RenderType BEAM = RenderType.create("thaumaturge_vis_relay_beam",
            RenderSetup.builder(TTRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", BEAM_TEXTURE).useLightmap().createRenderSetup());

    public static final RenderType FLARE = RenderType.create("thaumaturge_vis_relay_flare",
            RenderSetup.builder(TTRenderPipelines.FX_ADDITIVE).withTexture("Sampler0", ParticleTextures.STAR_GLINT).useLightmap().createRenderSetup());

    private VisRelayBeamRenderTypes() {}
}
