package com.leclowndu93150.thaumaturge.client.effect.pipeline;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCRenderPipelines {
    private static final BlendFunction TC_ADDITIVE = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE);
    private static final Identifier FX_FRAGMENT = TCIds.rl("core/tc_fx");
    private static final float FX_ALPHA_TEST_THRESHOLD = 1.0F / 255.0F;
    private static final DepthStencilState TEST_NO_WRITE = new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false);
    private static final DepthStencilState ALWAYS_NO_WRITE = new DepthStencilState(CompareOp.ALWAYS_PASS, false);

    private static final RenderPipeline.Snippet RIFT_SNIPPET = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET, RenderPipelines.GLOBALS_SNIPPET).withSampler("Sampler0")
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS).withDepthStencilState(DepthStencilState.DEFAULT).buildSnippet();

    public static final RenderPipeline FX_ADDITIVE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET).withLocation(TCIds.rl("pipeline/fx_additive"))
            .withFragmentShader(FX_FRAGMENT).withColorTargetState(new ColorTargetState(TC_ADDITIVE)).withDepthStencilState(TEST_NO_WRITE).withCull(false).build();

    public static final RenderPipeline FX_ADDITIVE_ALPHA_TEST = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(TCIds.rl("pipeline/fx_additive_alpha_test")).withFragmentShader(FX_FRAGMENT).withShaderDefine("ALPHA_CUTOUT", FX_ALPHA_TEST_THRESHOLD)
            .withColorTargetState(new ColorTargetState(TC_ADDITIVE)).withDepthStencilState(TEST_NO_WRITE).withCull(false).build();

    public static final RenderPipeline FX_TRANSLUCENT = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET).withLocation(TCIds.rl("pipeline/fx_translucent"))
            .withFragmentShader(FX_FRAGMENT).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withDepthStencilState(TEST_NO_WRITE).withCull(false).build();

    public static final RenderPipeline FX_ADDITIVE_NO_DEPTH = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(TCIds.rl("pipeline/fx_additive_no_depth")).withFragmentShader(FX_FRAGMENT).withColorTargetState(new ColorTargetState(TC_ADDITIVE))
            .withDepthStencilState(ALWAYS_NO_WRITE).withCull(false).build();

    public static final RenderPipeline FX_TRANSLUCENT_NO_DEPTH = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(TCIds.rl("pipeline/fx_translucent_no_depth")).withFragmentShader(FX_FRAGMENT)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withDepthStencilState(ALWAYS_NO_WRITE).withCull(false).build();

    public static final RenderPipeline SPARKLE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET).withLocation(TCIds.rl("pipeline/sparkle"))
            .withVertexShader("core/rendertype_lightning").withFragmentShader("core/rendertype_lightning").withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES).withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)).withCull(false)
            .build();

    public static final RenderPipeline RIFT_GLOW = RenderPipeline.builder(RIFT_SNIPPET).withLocation(TCIds.rl("pipeline/rift_glow"))
            .withVertexShader(TCIds.rl("core/tc_ender")).withFragmentShader(TCIds.rl("core/tc_ender"))
            .withColorTargetState(new ColorTargetState(TC_ADDITIVE)).withDepthStencilState(TEST_NO_WRITE).withCull(false).build();

    public static final RenderPipeline RIFT_GLOW_NO_DEPTH = RenderPipeline.builder(RIFT_SNIPPET).withLocation(TCIds.rl("pipeline/rift_glow_no_depth"))
            .withVertexShader(TCIds.rl("core/tc_ender")).withFragmentShader(TCIds.rl("core/tc_ender"))
            .withColorTargetState(new ColorTargetState(TC_ADDITIVE)).withDepthStencilState(ALWAYS_NO_WRITE).withCull(false).build();

    public static final RenderPipeline RIFT_SOLID = RenderPipeline.builder(RIFT_SNIPPET).withLocation(TCIds.rl("pipeline/rift_solid"))
            .withVertexShader(TCIds.rl("core/tc_ender")).withFragmentShader(TCIds.rl("core/tc_ender"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withCull(false).build();

    public static final RenderPipeline HOLE_SURFACE = RenderPipeline.builder(RIFT_SNIPPET).withLocation(TCIds.rl("pipeline/hole_surface"))
            .withVertexShader(TCIds.rl("core/tc_ender")).withFragmentShader(TCIds.rl("core/tc_ender")).withCull(false).build();

    public static final RenderPipeline PORTAL_SURFACE = RenderPipeline.builder(RenderPipelines.END_PORTAL_SNIPPET).withLocation(TCIds.rl("pipeline/portal_surface"))
            .withVertexShader(TCIds.rl("core/tc_portal")).withFragmentShader(TCIds.rl("core/tc_portal"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS).withCull(false).build();

    public static final RenderPipeline SPARKLE_CULLED = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(TCIds.rl("pipeline/sparkle_culled")).withVertexShader("core/rendertype_lightning").withFragmentShader("core/rendertype_lightning")
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING)).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES).withDepthStencilState(TEST_NO_WRITE)
            .build();

    public static final RenderPipeline ENTITY_CUTOUT_FLAT = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(TCIds.rl("pipeline/entity_cutout_flat")).withVertexShader("core/entity").withFragmentShader("core/entity")
            .withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING").withSampler("Sampler0").withSampler("Sampler2").withCull(false)
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS).withDepthStencilState(DepthStencilState.DEFAULT).build();

    public static final RenderPipeline ENTITY_TRANSLUCENT_FLAT = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(TCIds.rl("pipeline/entity_translucent_flat")).withVertexShader("core/entity").withFragmentShader("core/entity")
            .withShaderDefine("ALPHA_CUTOUT", 0.1F).withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING").withSampler("Sampler0").withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withCull(false).withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT).build();

    public static final RenderPipeline ENTITY_ADDITIVE_EMISSIVE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(TCIds.rl("pipeline/entity_additive_emissive")).withVertexShader("core/entity").withFragmentShader("core/entity")
            .withShaderDefine("EMISSIVE").withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING").withSampler("Sampler0").withColorTargetState(new ColorTargetState(TC_ADDITIVE))
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS).withDepthStencilState(TEST_NO_WRITE).withCull(false).build();

    public static final RenderPipeline ENTITY_TRANSLUCENT_NO_DEPTH = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(TCIds.rl("pipeline/entity_translucent_no_depth")).withVertexShader("core/entity").withFragmentShader("core/entity")
            .withShaderDefine("NO_OVERLAY").withShaderDefine("NO_CARDINAL_LIGHTING").withSampler("Sampler0").withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS).withDepthStencilState(ALWAYS_NO_WRITE).withCull(false).build();

    public static final RenderPipeline GUI_TEXTURED_ADDITIVE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(TCIds.rl("pipeline/gui_textured_additive")).withColorTargetState(new ColorTargetState(TC_ADDITIVE)).build();

    @SubscribeEvent
    static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(FX_ADDITIVE);
        event.registerPipeline(FX_ADDITIVE_ALPHA_TEST);
        event.registerPipeline(RIFT_GLOW);
        event.registerPipeline(RIFT_GLOW_NO_DEPTH);
        event.registerPipeline(RIFT_SOLID);
        event.registerPipeline(HOLE_SURFACE);
        event.registerPipeline(PORTAL_SURFACE);
        event.registerPipeline(FX_TRANSLUCENT);
        event.registerPipeline(FX_ADDITIVE_NO_DEPTH);
        event.registerPipeline(FX_TRANSLUCENT_NO_DEPTH);
        event.registerPipeline(GUI_TEXTURED_ADDITIVE);
        event.registerPipeline(SPARKLE);
        event.registerPipeline(SPARKLE_CULLED);
        event.registerPipeline(ENTITY_CUTOUT_FLAT);
        event.registerPipeline(ENTITY_TRANSLUCENT_FLAT);
        event.registerPipeline(ENTITY_ADDITIVE_EMISSIVE);
        event.registerPipeline(ENTITY_TRANSLUCENT_NO_DEPTH);
    }

    private TCRenderPipelines() {}
}
