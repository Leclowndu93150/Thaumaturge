package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class EssentiaStreamRenderType {
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/effect/essentia.png");

    public static final RenderType RENDER_TYPE = TTRenderTypes.translucentTextured(TEXTURE);

    // Fabulous composites clouds and particles from separate buffers using their depth.
    public static final RenderType FABULOUS = RenderType.create(
            "tt_essentia_stream",
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setOutputState(RenderStateShard.PARTICLES_TARGET)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    private EssentiaStreamRenderType() {}
}
