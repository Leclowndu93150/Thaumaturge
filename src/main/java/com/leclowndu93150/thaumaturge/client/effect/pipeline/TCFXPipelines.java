package com.leclowndu93150.thaumaturge.client.effect.pipeline;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

public final class TCFXPipelines {
    private static final RenderPipeline.Snippet BASE = RenderPipeline.builder().withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("Fog", UniformType.UNIFORM_BUFFER).withVertexShader("core/position_tex_color").withFragmentShader("core/position_tex_color").withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).buildSnippet();

    private TCFXPipelines() {}

    public static RenderPipeline additiveTextured(Identifier location, Identifier fragmentShader) {
        return RenderPipeline.builder(BASE).withLocation(location).withFragmentShader(fragmentShader)
                .withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE))).withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withCull(false).build();
    }

    public static RenderPipeline additiveTextured(Identifier location) {
        return RenderPipeline.builder(BASE).withLocation(location).withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE)))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)).withCull(false).build();
    }

    public static RenderPipeline translucentTextured(Identifier location) {
        return RenderPipeline.builder(BASE).withLocation(location).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)).withCull(false).build();
    }

    public static RenderPipeline additiveTexturedNoDepth(Identifier location) {
        return RenderPipeline.builder(BASE).withLocation(location).withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE)))
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).withCull(false).build();
    }
}
