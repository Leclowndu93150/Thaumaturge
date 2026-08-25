package com.leclowndu93150.thaumaturge.client.warding;

import com.leclowndu93150.thaumaturge.client.render.TCShaders;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

public final class WardRenderType {
    private static final int BUFFER_SIZE = 1536;
    private static final RenderStateShard.ShaderStateShard SHADER =
            new RenderStateShard.ShaderStateShard(TCShaders::wardAdd);
    private static final RenderStateShard.TextureStateShard BLOCK_ATLAS =
            new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false);

    public static final RenderType ADDITIVE = RenderType.create(
            "thaumaturge_ward_runes",
            DefaultVertexFormat.POSITION_TEX_COLOR,
            VertexFormat.Mode.QUADS,
            BUFFER_SIZE,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(SHADER)
                    .setTextureState(BLOCK_ATLAS)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    private WardRenderType() {}
}
