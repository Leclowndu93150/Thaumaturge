package com.leclowndu93150.thaumaturge.compat.iris;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.layer.BlockEntityRenderStateShard;
import net.irisshaders.iris.layer.BufferSourceWrapper;
import net.irisshaders.iris.layer.EntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

/** Iris-only implementation loaded reflectively by {@link IrisCompat}. */
public final class IrisCompatImpl {
    private static final String ALPHA_TEST_UNIFORM = "AlphaTestValue";
    private static final ResourceLocation NODE_TEXTURE = TCIds.rl("textures/misc/nodes.png");

    private IrisCompatImpl() {}

    public static boolean shadersActive() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    public static ShaderInstance particleTranslucentShader() {
        return ShaderAccess.getParticleTranslucentShader();
    }

    public static boolean isNodeParticleShaderPass(Object shader) {
        if (!shadersActive() || shader != particleTranslucentShader()) {
            return false;
        }
        return RenderSystem.getShaderTexture(0) == nodeTextureId();
    }

    public static boolean isNodeTextureBound() {
        return shadersActive() && RenderSystem.getShaderTexture(0) == nodeTextureId();
    }

    public static void setParticleAlphaTest(float threshold) {
        if (!shadersActive()) {
            return;
        }
        ShaderInstance shader = particleTranslucentShader();
        if (shader.getUniform(ALPHA_TEST_UNIFORM) != null) {
            shader.getUniform(ALPHA_TEST_UNIFORM).set(threshold);
        }
    }

    public static MultiBufferSource entityEffectBuffers(MultiBufferSource buffers) {
        return new BufferSourceWrapper(
                buffers,
                renderType -> OuterWrappedRenderType.wrapExactlyOnce(
                        "iris:thaumaturge_entity_effect", renderType, EntityRenderStateShard.INSTANCE));
    }

    public static MultiBufferSource blockEntityEffectBuffers(MultiBufferSource buffers) {
        return new BufferSourceWrapper(
                buffers,
                renderType -> OuterWrappedRenderType.wrapExactlyOnce(
                        "iris:thaumaturge_block_entity_effect", renderType, BlockEntityRenderStateShard.INSTANCE));
    }

    private static int nodeTextureId() {
        return Minecraft.getInstance()
                .getTextureManager()
                .getTexture(NODE_TEXTURE)
                .getId();
    }
}
