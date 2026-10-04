package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class VisRelayBeamRenderTypes {
    public static final ResourceLocation BEAM_TEXTURE = TCIds.rl("textures/misc/beam1.png");

    public static final RenderType BEAM = TCRenderTypes.fxAlphaAdditive(BEAM_TEXTURE);
    public static final RenderType FLARE = TCRenderTypes.fxAlphaAdditive(ParticleTextures.STAR_GLINT);

    private VisRelayBeamRenderTypes() {}
}
