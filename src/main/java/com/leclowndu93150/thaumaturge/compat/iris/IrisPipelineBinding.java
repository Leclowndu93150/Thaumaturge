package com.leclowndu93150.thaumaturge.compat.iris;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.minecraft.client.renderer.RenderPipelines;
import org.jspecify.annotations.Nullable;

public final class IrisPipelineBinding {
    private static final String EMISSIVE_DEFINE = "EMISSIVE";

    private IrisPipelineBinding() {}

    public static void bindModPipelines() {
        IrisApi iris = IrisApi.getInstance();
        for (RenderPipeline pipeline : RenderPipelines.getStaticPipelines()) {
            if (!TCIds.MODID.equals(pipeline.getLocation().getNamespace())) {
                continue;
            }
            IrisProgram program = programFor(pipeline);
            if (program != null) {
                iris.assignPipeline(pipeline, program);
            }
        }
    }

    private static @Nullable IrisProgram programFor(RenderPipeline pipeline) {
        VertexFormat format = pipeline.getVertexFormat();
        if (format == DefaultVertexFormat.PARTICLE) {
            return IrisProgram.PARTICLES_TRANSLUCENT;
        }
        if (format == DefaultVertexFormat.ENTITY) {
            return pipeline.getShaderDefines().values().containsKey(EMISSIVE_DEFINE) ? IrisProgram.EMISSIVE_ENTITIES : IrisProgram.ENTITIES_TRANSLUCENT;
        }
        if (format == DefaultVertexFormat.POSITION_TEX_COLOR || format == DefaultVertexFormat.POSITION_TEX) {
            return IrisProgram.TEXTURED;
        }
        if (format == DefaultVertexFormat.POSITION_COLOR) {
            return IrisProgram.BASIC;
        }
        if (format == DefaultVertexFormat.POSITION) {
            return IrisProgram.BLOCK;
        }
        return null;
    }
}
