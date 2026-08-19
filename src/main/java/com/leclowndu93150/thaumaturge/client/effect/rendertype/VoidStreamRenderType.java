package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TCIds;
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
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class VoidStreamRenderType {
    public static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/end_portal.png");
    private static final Identifier SHADER = Identifier.fromNamespaceAndPath(TCIds.MODID, "core/void_stream");

    private static final RenderPipeline.Snippet BASE = RenderPipeline.builder().withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER).withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("Globals", UniformType.UNIFORM_BUFFER).withVertexShader(SHADER).withFragmentShader(SHADER).withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).buildSnippet();

    public static final RenderPipeline ADDITIVE_PIPELINE = RenderPipeline.builder(BASE).withLocation(Identifier.fromNamespaceAndPath(TCIds.MODID, "pipeline/void_stream_add"))
            .withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE))).withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withCull(false).build();

    public static final RenderPipeline TRANSLUCENT_PIPELINE = RenderPipeline.builder(BASE).withLocation(Identifier.fromNamespaceAndPath(TCIds.MODID, "pipeline/void_stream_tr"))
            .withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA)))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true)).withCull(false).build();

    public static final RenderType ADDITIVE = RenderType.create("thaumaturge_void_stream_add", RenderSetup.builder(ADDITIVE_PIPELINE).withTexture("Sampler0", TEXTURE).createRenderSetup());

    public static final RenderType TRANSLUCENT = RenderType.create("thaumaturge_void_stream_tr", RenderSetup.builder(TRANSLUCENT_PIPELINE).withTexture("Sampler0", TEXTURE).createRenderSetup());

    @SubscribeEvent
    static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(ADDITIVE_PIPELINE);
        event.registerPipeline(TRANSLUCENT_PIPELINE);
    }

    private VoidStreamRenderType() {}
}
