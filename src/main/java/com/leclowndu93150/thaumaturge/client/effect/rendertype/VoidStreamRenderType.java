package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.client.render.TTShaders;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class VoidStreamRenderType {
    public static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/end_portal.png");

    private static final int BUFFER = 1536;
    private static final RenderStateShard.ShaderStateShard SHADER =
            new RenderStateShard.ShaderStateShard(TTShaders::voidStream);
    private static final RenderStateShard.TextureStateShard TEXTURE_STATE =
            new RenderStateShard.TextureStateShard(TEXTURE, false, false);

    public static final RenderType ADDITIVE = RenderType.create(
            "thaumaturge_void_stream_add",
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormat.Mode.QUADS,
            BUFFER,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(SHADER)
                    .setTextureState(TEXTURE_STATE)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    public static final RenderType TRANSLUCENT = RenderType.create(
            "thaumaturge_void_stream_tr",
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormat.Mode.QUADS,
            BUFFER,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(SHADER)
                    .setTextureState(TEXTURE_STATE)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    private VoidStreamRenderType() {}
}
