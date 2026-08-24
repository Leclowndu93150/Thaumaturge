package com.leclowndu93150.thaumaturge.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class TCRenderTypes {
    private static final int BUFFER = 1536;

    private static final RenderStateShard.ShaderStateShard PARTICLE_SHADER =
            new RenderStateShard.ShaderStateShard(GameRenderer::getParticleShader);
    private static final RenderStateShard.ShaderStateShard POSITION_TEX_COLOR_SHADER =
            new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader);
    private static final RenderStateShard.OverlayStateShard OVERLAY = new RenderStateShard.OverlayStateShard(true);

    private static final Function<ResourceLocation, RenderType> FX_ADDITIVE = Util.memoize(texture -> particle(
            "tc_fx_additive",
            texture,
            RenderStateShard.ADDITIVE_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            RenderStateShard.COLOR_WRITE,
            false));
    private static final Function<ResourceLocation, RenderType> FX_TRANSLUCENT = Util.memoize(texture -> particle(
            "tc_fx_translucent",
            texture,
            RenderStateShard.TRANSLUCENT_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            RenderStateShard.COLOR_WRITE,
            true));
    private static final Function<ResourceLocation, RenderType> FX_ADDITIVE_NO_DEPTH = Util.memoize(texture -> particle(
            "tc_fx_additive_no_depth",
            texture,
            RenderStateShard.ADDITIVE_TRANSPARENCY,
            RenderStateShard.NO_DEPTH_TEST,
            RenderStateShard.COLOR_WRITE,
            false));
    private static final Function<ResourceLocation, RenderType> FX_TRANSLUCENT_NO_DEPTH =
            Util.memoize(texture -> particle(
                    "tc_fx_translucent_no_depth",
                    texture,
                    RenderStateShard.TRANSLUCENT_TRANSPARENCY,
                    RenderStateShard.NO_DEPTH_TEST,
                    RenderStateShard.COLOR_WRITE,
                    true));
    private static final Function<ResourceLocation, RenderType> FX_TRANSLUCENT_DEPTH = Util.memoize(texture -> particle(
            "tc_fx_translucent_depth",
            texture,
            RenderStateShard.TRANSLUCENT_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            RenderStateShard.COLOR_DEPTH_WRITE,
            false));

    private static final Function<ResourceLocation, RenderType> ADDITIVE_TEXTURED = Util.memoize(texture -> textured(
            "tc_additive_textured",
            texture,
            RenderStateShard.ADDITIVE_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            false));
    private static final Function<ResourceLocation, RenderType> TRANSLUCENT_TEXTURED = Util.memoize(texture -> textured(
            "tc_translucent_textured",
            texture,
            RenderStateShard.TRANSLUCENT_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            true));
    private static final Function<ResourceLocation, RenderType> ADDITIVE_TEXTURED_NO_DEPTH =
            Util.memoize(texture -> textured(
                    "tc_additive_textured_no_depth",
                    texture,
                    RenderStateShard.ADDITIVE_TRANSPARENCY,
                    RenderStateShard.NO_DEPTH_TEST,
                    false));

    private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT_FLAT = Util.memoize(texture -> flat(
            "tc_entity_cutout_flat",
            texture,
            null,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            RenderStateShard.COLOR_DEPTH_WRITE,
            false));
    private static final Function<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_FLAT = Util.memoize(texture -> flat(
            "tc_entity_translucent_flat",
            texture,
            RenderStateShard.TRANSLUCENT_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            RenderStateShard.COLOR_WRITE,
            true));
    private static final Function<ResourceLocation, RenderType> ENTITY_ADDITIVE_EMISSIVE = Util.memoize(texture -> flat(
            "tc_entity_additive_emissive",
            texture,
            RenderStateShard.ADDITIVE_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            RenderStateShard.COLOR_WRITE,
            false));
    private static final Function<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_NO_DEPTH =
            Util.memoize(texture -> flat(
                    "tc_entity_translucent_no_depth",
                    texture,
                    RenderStateShard.TRANSLUCENT_TRANSPARENCY,
                    RenderStateShard.NO_DEPTH_TEST,
                    RenderStateShard.COLOR_WRITE,
                    true));
    private static final Function<ResourceLocation, RenderType> TAINTED_SWIRL_NO_DEPTH = Util.memoize(texture -> flat(
            "tc_tainted_swirl_no_depth",
            texture,
            RenderStateShard.TRANSLUCENT_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            RenderStateShard.COLOR_WRITE,
            false));

    public static final RenderType SPARKLE = RenderType.create(
            "tc_sparkle",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            BUFFER,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    public static final RenderType SPARKLE_CULLED = RenderType.create(
            "tc_sparkle_culled",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            BUFFER,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    private TCRenderTypes() {}

    public static RenderType fxAdditive(ResourceLocation texture) {
        return FX_ADDITIVE.apply(texture);
    }

    public static RenderType fxTranslucent(ResourceLocation texture) {
        return FX_TRANSLUCENT.apply(texture);
    }

    public static RenderType fxAdditiveNoDepth(ResourceLocation texture) {
        return FX_ADDITIVE_NO_DEPTH.apply(texture);
    }

    public static RenderType fxTranslucentNoDepth(ResourceLocation texture) {
        return FX_TRANSLUCENT_NO_DEPTH.apply(texture);
    }

    public static RenderType fxTranslucentDepth(ResourceLocation texture) {
        return FX_TRANSLUCENT_DEPTH.apply(texture);
    }

    public static RenderType additiveTextured(ResourceLocation texture) {
        return ADDITIVE_TEXTURED.apply(texture);
    }

    public static RenderType translucentTextured(ResourceLocation texture) {
        return TRANSLUCENT_TEXTURED.apply(texture);
    }

    public static RenderType additiveTexturedNoDepth(ResourceLocation texture) {
        return ADDITIVE_TEXTURED_NO_DEPTH.apply(texture);
    }

    public static RenderType entityCutoutFlat(ResourceLocation texture) {
        return ENTITY_CUTOUT_FLAT.apply(texture);
    }

    public static RenderType entityTranslucentFlat(ResourceLocation texture) {
        return ENTITY_TRANSLUCENT_FLAT.apply(texture);
    }

    public static RenderType entityAdditiveEmissive(ResourceLocation texture) {
        return ENTITY_ADDITIVE_EMISSIVE.apply(texture);
    }

    public static RenderType entityTranslucentNoDepth(ResourceLocation texture) {
        return ENTITY_TRANSLUCENT_NO_DEPTH.apply(texture);
    }

    public static RenderType taintedSwirlNoDepth(ResourceLocation texture) {
        return TAINTED_SWIRL_NO_DEPTH.apply(texture);
    }

    private static RenderType particle(
            String name,
            ResourceLocation texture,
            RenderStateShard.TransparencyStateShard transparency,
            RenderStateShard.DepthTestStateShard depthTest,
            RenderStateShard.WriteMaskStateShard writeMask,
            boolean sort) {
        return RenderType.create(
                name,
                DefaultVertexFormat.PARTICLE,
                VertexFormat.Mode.QUADS,
                BUFFER,
                false,
                sort,
                RenderType.CompositeState.builder()
                        .setShaderState(PARTICLE_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(transparency)
                        .setDepthTestState(depthTest)
                        .setWriteMaskState(writeMask)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .createCompositeState(false));
    }

    private static RenderType textured(
            String name,
            ResourceLocation texture,
            RenderStateShard.TransparencyStateShard transparency,
            RenderStateShard.DepthTestStateShard depthTest,
            boolean sort) {
        return RenderType.create(
                name,
                DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS,
                BUFFER,
                false,
                sort,
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_TEX_COLOR_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(transparency)
                        .setDepthTestState(depthTest)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .setCullState(RenderStateShard.NO_CULL)
                        .createCompositeState(false));
    }

    private static RenderType flat(
            String name,
            ResourceLocation texture,
            RenderStateShard.TransparencyStateShard transparency,
            RenderStateShard.DepthTestStateShard depthTest,
            RenderStateShard.WriteMaskStateShard writeMask,
            boolean sort) {
        RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setDepthTestState(depthTest)
                .setWriteMaskState(writeMask)
                .setCullState(RenderStateShard.NO_CULL)
                .setOverlayState(OVERLAY);
        if (transparency != null) {
            builder.setTransparencyState(transparency);
        }
        return RenderType.create(
                name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                BUFFER,
                false,
                sort,
                builder.createCompositeState(false));
    }
}
