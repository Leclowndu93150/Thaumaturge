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
import net.neoforged.fml.ModList;

public final class IrisCompat {
    private static final String ALPHA_TEST_UNIFORM = "AlphaTestValue";
    private static final ResourceLocation NODE_TEXTURE = TCIds.rl("textures/misc/nodes.png");

    private IrisCompat() {}

    public static boolean shadersActive() {
        return ModList.get().isLoaded(TCIds.IRIS) && IrisApi.getInstance().isShaderPackInUse();
    }

    /**
     * Gets Iris's translucent particle program for Thaumaturge's particle-format effects. This is the 1.21.1
     * equivalent of assigning {@code IrisProgram.PARTICLES_TRANSLUCENT} to a modern render pipeline.
     */
    public static ShaderInstance particleTranslucentShader() {
        return ShaderAccess.getParticleTranslucentShader();
    }

    /**
     * Returns true only while Iris is applying its translucent-particle shader to Thaumaturge's node atlas.
     *
     * <p>Using the currently bound texture is intentional. Iris applies the shader after the RenderType has already
     * bound its texture, so this remains correct even when Iris buffers and reorders block-entity draws. It also avoids
     * changing the shader-pack particle rules for vanilla particles or unrelated Thaumaturge effects.</p>
     */
    public static boolean isNodeParticleShaderPass(Object shader) {
        if (!shadersActive() || shader != particleTranslucentShader()) {
            return false;
        }
        int nodeTextureId = Minecraft.getInstance()
                .getTextureManager()
                .getTexture(NODE_TEXTURE)
                .getId();
        return RenderSystem.getShaderTexture(0) == nodeTextureId;
    }

    /** True while the node atlas is bound, used by Iris fallback-shader compatibility. */
    public static boolean isNodeTextureBound() {
        if (!shadersActive()) {
            return false;
        }
        int nodeTextureId = Minecraft.getInstance()
                .getTextureManager()
                .getTexture(NODE_TEXTURE)
                .getId();
        return RenderSystem.getShaderTexture(0) == nodeTextureId;
    }

    /** Sets the Iris particle alpha cutoff for the duration of a Thaumaturge effect render type. */
    public static void setParticleAlphaTest(float threshold) {
        if (!shadersActive()) {
            return;
        }
        ShaderInstance shader = particleTranslucentShader();
        if (shader.getUniform(ALPHA_TEST_UNIFORM) != null) {
            shader.getUniform(ALPHA_TEST_UNIFORM).set(threshold);
        }
    }

    /**
     * Adds Iris's entity render context to deferred visual effects. Effects submitted after the normal entity pass do
     * not pass through Iris's {@code EntityRenderDispatcher} hook, so they need the same wrapper explicitly.
     */
    public static MultiBufferSource entityEffectBuffers(MultiBufferSource buffers) {
        return shadersActive()
                ? new BufferSourceWrapper(
                        buffers,
                        renderType -> OuterWrappedRenderType.wrapExactlyOnce(
                                "iris:thaumaturge_entity_effect", renderType, EntityRenderStateShard.INSTANCE))
                : buffers;
    }

    /**
     * Adds Iris's block-entity render context to deferred visual effects. Effects submitted after the normal block
     * entity pass do not pass through Iris's {@code BlockEntityRenderDispatcher} hook.
     */
    public static MultiBufferSource blockEntityEffectBuffers(MultiBufferSource buffers) {
        return shadersActive()
                ? new BufferSourceWrapper(
                        buffers,
                        renderType -> OuterWrappedRenderType.wrapExactlyOnce(
                                "iris:thaumaturge_block_entity_effect",
                                renderType,
                                BlockEntityRenderStateShard.INSTANCE))
                : buffers;
    }
}
