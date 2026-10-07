package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class EssentiaStreamRenderType {
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/effect/essentia.png");

    public static final RenderType RENDER_TYPE = TTRenderTypes.translucentTextured(TEXTURE);

    private EssentiaStreamRenderType() {}
}
