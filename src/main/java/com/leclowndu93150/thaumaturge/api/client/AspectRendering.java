package com.leclowndu93150.thaumaturge.api.client;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

/**
 * Draws aspect icons the way Thaumaturge does, for addon screens, block entity renderers and entity
 * renderers. Client only: never load this class on a dedicated server.
 *
 * <p>Every method that takes no {@link AspectKnowledge} uses the client player's own knowledge
 * from {@link AspectKnowledgeAccess}, so an aspect the player has not discovered is drawn masked,
 * as the Thaumonomicon and thaumometer draw it. Pass a knowledge value explicitly for previews
 * that should ignore the player. The {@code missing} methods draw the unknown-aspect placeholder
 * for an aspect id that is not in the registry.
 *
 * <p>World drawing writes to a caller-owned buffer source. Billboards face the main camera.
 * The implementation is bound at client setup by Thaumaturge via {@link #bind(Bindings)}.
 *
 * @since 1.0.0
 */
public final class AspectRendering {
    /** The size of a GUI aspect icon in GUI pixels. */
    public static final int GUI_ICON_SIZE = 16;

    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("AspectRendering");

    private AspectRendering() {}

    /**
     * Draws a GUI aspect icon, with its amount in the corner when {@code amount} is positive.
     *
     * @param graphics the GUI graphics
     * @param font     the font for the amount
     * @param x        the left edge
     * @param y        the top edge
     * @param aspect   the aspect
     * @param amount   the amount to print, or zero for none
     */
    public static void renderGui(GuiGraphics graphics, Font font, int x, int y, Holder<IAspect> aspect, float amount) {
        renderGui(graphics, font, x, y, aspect, amount, AspectKnowledgeAccess.of(aspect));
    }

    /**
     * Draws a GUI aspect icon as it appears with the given knowledge: the real icon and amount
     * when known, a masked chip otherwise.
     *
     * @param graphics  the GUI graphics
     * @param font      the font for the amount
     * @param x         the left edge
     * @param y         the top edge
     * @param aspect    the aspect
     * @param amount    the amount to print, or zero for none
     * @param knowledge how much of the aspect to reveal
     */
    public static void renderGui(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            Holder<IAspect> aspect,
            float amount,
            AspectKnowledge knowledge) {
        BINDING.get().renderGui(graphics, font, x, y, aspect, amount, knowledge);
    }

    /**
     * Draws the unknown-aspect placeholder in a GUI.
     *
     * @param graphics the GUI graphics
     * @param x        the left edge
     * @param y        the top edge
     */
    public static void renderMissingGui(GuiGraphics graphics, int x, int y) {
        BINDING.get().renderGui(graphics, Minecraft.getInstance().font, x, y, null, 0.0F, AspectKnowledge.UNKNOWN);
    }

    /**
     * The render type an aspect quad uses.
     *
     * @param aspect    the aspect
     * @param knowledge how much of the aspect to reveal
     * @param blend     how the quad blends
     * @return the render type
     */
    public static RenderType renderType(Holder<IAspect> aspect, AspectKnowledge knowledge, BlendMode blend) {
        return BINDING.get().renderType(aspect, knowledge, blend);
    }

    /**
     * The render type the unknown-aspect placeholder quad uses.
     *
     * @param blend how the quad blends
     * @return the render type
     */
    public static RenderType missingRenderType(BlendMode blend) {
        return BINDING.get().renderType(null, AspectKnowledge.UNKNOWN, blend);
    }

    /**
     * Writes one aspect quad, one unit wide and centred on the pose origin, into a buffer of
     * {@link #renderType}.
     *
     * @param pose        the pose to draw with
     * @param buffer      the buffer
     * @param aspect      the aspect
     * @param knowledge   how much of the aspect to reveal
     * @param alpha       the opacity, zero to one
     * @param monochrome  whether to draw it grey instead of in the aspect's colour
     * @param packedLight the packed light
     */
    public static void renderQuad(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            Holder<IAspect> aspect,
            AspectKnowledge knowledge,
            float alpha,
            boolean monochrome,
            int packedLight) {
        BINDING.get().renderQuad(pose, buffer, aspect, knowledge, alpha, monochrome, packedLight);
    }

    /**
     * Writes the unknown-aspect placeholder quad into a buffer of {@link #missingRenderType}.
     *
     * @param pose        the pose to draw with
     * @param buffer      the buffer
     * @param alpha       the opacity, zero to one
     * @param packedLight the packed light
     */
    public static void renderMissingQuad(PoseStack.Pose pose, VertexConsumer buffer, float alpha, int packedLight) {
        BINDING.get().renderQuad(pose, buffer, null, AspectKnowledge.UNKNOWN, alpha, false, packedLight);
    }

    /**
     * Draws a camera-facing aspect icon at the pose origin into a buffer source, using the client
     * player's knowledge.
     *
     * @param poseStack   the pose stack, positioned at the icon's centre
     * @param buffers     the buffer source
     * @param aspect      the aspect
     * @param scale       the icon's width in blocks
     * @param alpha       the opacity, zero to one
     * @param monochrome  whether to draw it grey
     * @param packedLight the packed light
     * @param blend       how the icon blends
     */
    public static void renderBillboard(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Holder<IAspect> aspect,
            float scale,
            float alpha,
            boolean monochrome,
            int packedLight,
            BlendMode blend) {
        renderBillboard(
                poseStack,
                buffers,
                aspect,
                scale,
                alpha,
                monochrome,
                packedLight,
                blend,
                AspectKnowledgeAccess.of(aspect));
    }

    /**
     * Draws a camera-facing aspect icon at the pose origin into a buffer source.
     *
     * @param poseStack   the pose stack, positioned at the icon's centre
     * @param buffers     the buffer source
     * @param aspect      the aspect
     * @param scale       the icon's width in blocks
     * @param alpha       the opacity, zero to one
     * @param monochrome  whether to draw it grey
     * @param packedLight the packed light
     * @param blend       how the icon blends
     * @param knowledge   how much of the aspect to reveal
     */
    public static void renderBillboard(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Holder<IAspect> aspect,
            float scale,
            float alpha,
            boolean monochrome,
            int packedLight,
            BlendMode blend,
            AspectKnowledge knowledge) {
        renderFacingCamera(
                poseStack,
                buffers,
                scale,
                renderType(aspect, knowledge, blend),
                (pose, buffer) -> renderQuad(pose, buffer, aspect, knowledge, alpha, monochrome, packedLight));
    }

    /**
     * Draws a camera-facing unknown-aspect placeholder at the pose origin into a buffer source.
     *
     * @param poseStack   the pose stack, positioned at the icon's centre
     * @param buffers     the buffer source
     * @param scale       the icon's width in blocks
     * @param alpha       the opacity, zero to one
     * @param packedLight the packed light
     * @param blend       how the icon blends
     */
    public static void renderMissingBillboard(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float scale,
            float alpha,
            int packedLight,
            BlendMode blend) {
        renderFacingCamera(
                poseStack,
                buffers,
                scale,
                missingRenderType(blend),
                (pose, buffer) -> renderMissingQuad(pose, buffer, alpha, packedLight));
    }

    /**
     * Binds the implementation. Called once at client setup by Thaumaturge; addons must not call
     * this.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    public static void renderQuad(
            PoseStack pose,
            VertexConsumer buffer,
            Holder<IAspect> aspect,
            float alpha,
            boolean monochrome,
            int packedLight) {
        renderQuad(pose.last(), buffer, aspect, AspectKnowledgeAccess.of(aspect), alpha, monochrome, packedLight);
    }

    public static void renderMissingQuad(PoseStack pose, VertexConsumer buffer, float alpha, int packedLight) {
        renderMissingQuad(pose.last(), buffer, alpha, packedLight);
    }

    private static void renderFacingCamera(
            PoseStack poseStack,
            MultiBufferSource buffers,
            float scale,
            RenderType renderType,
            BiConsumer<PoseStack.Pose, VertexConsumer> quad) {
        poseStack.pushPose();
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.scale(scale, scale, scale);
        quad.accept(poseStack.last(), buffers.getBuffer(renderType));
        poseStack.popPose();
    }

    /**
     * How an aspect icon blends with what is behind it.
     *
     * @since 1.0.0
     */
    public enum BlendMode {
        /** Normal translucency. */
        ALPHA,
        /** Additive, for glowing icons. */
        ADDITIVE
    }

    /**
     * Implementation hook supplied by Thaumaturge. A null aspect means the unknown-aspect
     * placeholder. Addons must not implement this interface.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * Draws a GUI icon.
         *
         * @param graphics  the GUI graphics
         * @param font      the font for the amount
         * @param x         the left edge
         * @param y         the top edge
         * @param aspect    the aspect, or null for the placeholder
         * @param amount    the amount to print, or zero for none
         * @param knowledge how much of the aspect to reveal
         */
        void renderGui(
                GuiGraphics graphics,
                Font font,
                int x,
                int y,
                @Nullable Holder<IAspect> aspect,
                float amount,
                AspectKnowledge knowledge);

        /**
         * The render type of an aspect quad.
         *
         * @param aspect    the aspect, or null for the placeholder
         * @param knowledge how much of the aspect to reveal
         * @param blend     how the quad blends
         * @return the render type
         */
        RenderType renderType(@Nullable Holder<IAspect> aspect, AspectKnowledge knowledge, BlendMode blend);

        /**
         * Writes one aspect quad.
         *
         * @param pose        the pose to draw with
         * @param buffer      the buffer
         * @param aspect      the aspect, or null for the placeholder
         * @param knowledge   how much of the aspect to reveal
         * @param alpha       the opacity
         * @param monochrome  whether to draw it grey
         * @param packedLight the packed light
         */
        void renderQuad(
                PoseStack.Pose pose,
                VertexConsumer buffer,
                @Nullable Holder<IAspect> aspect,
                AspectKnowledge knowledge,
                float alpha,
                boolean monochrome,
                int packedLight);
    }
}
