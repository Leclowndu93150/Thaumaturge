package com.leclowndu93150.thaumaturge.api.client;

import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.TCFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

/**
 * Supported client-only facade for rendering aspect icons.
 *
 * <p>Addons should use this class instead of importing Thaumaturge's internal renderers. Knowledge
 * may be supplied explicitly for previews or resolved through {@link AspectKnowledgeAccess} for
 * the current client player.
 *
 * @since 1.0.0
 */
public final class AspectRendering {
    public static final int GUI_ICON_SIZE = AspectTagRenderer.TAG_SIZE;

    public enum BlendMode {
        ALPHA,
        ADDITIVE
    }

    private AspectRendering() {}

    /** Renders a GUI icon using the current client's discovery state. */
    public static void renderGui(GuiGraphics graphics, Font font, int x, int y, Holder<IAspect> aspect, float amount) {
        renderGui(graphics, font, x, y, aspect, amount, AspectKnowledgeAccess.of(aspect));
    }

    /** Renders a GUI icon, masking its identity unless the supplied knowledge is known. */
    public static void renderGui(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            Holder<IAspect> aspect,
            float amount,
            AspectKnowledge knowledge) {
        if (knowledge.isKnown()) {
            AspectTagRenderer.render(graphics, font, x, y, aspect, amount);
        } else {
            AspectTagRenderer.renderMaskedChip(graphics, x, y, aspect, knowledge);
        }
    }

    /** Returns the render type used for an aspect quad with the requested discovery state. */
    public static RenderType renderType(Holder<IAspect> aspect, AspectKnowledge knowledge, BlendMode blendMode) {
        ResourceLocation texture =
                knowledge.isKnown() ? aspect.value().texture() : AspectTagWorldRenderer.UNKNOWN_TEXTURE;
        return blendMode == BlendMode.ADDITIVE
                ? TCFlatRenderTypes.entityAdditiveFlat(texture)
                : TCFlatRenderTypes.entityTranslucentFlat(texture);
    }

    /** Renders a camera-facing world icon using a caller-owned buffer source. */
    public static void renderBillboard(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Holder<IAspect> aspect,
            float scale,
            float alpha,
            boolean monochrome,
            int packedLight,
            BlendMode blendMode) {
        renderBillboard(
                poseStack,
                buffers,
                aspect,
                scale,
                alpha,
                monochrome,
                packedLight,
                blendMode,
                AspectKnowledgeAccess.of(aspect));
    }

    /** Renders a camera-facing world icon with an explicit discovery state. */
    public static void renderBillboard(
            PoseStack poseStack,
            MultiBufferSource buffers,
            Holder<IAspect> aspect,
            float scale,
            float alpha,
            boolean monochrome,
            int packedLight,
            BlendMode blendMode,
            AspectKnowledge knowledge) {
        if (!knowledge.isKnown()) {
            poseStack.pushPose();
            poseStack.mulPose(
                    Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
            poseStack.scale(scale, scale, scale);
            AspectTagWorldRenderer.renderQuad(
                    poseStack,
                    buffers.getBuffer(renderType(aspect, knowledge, blendMode)),
                    aspect,
                    alpha,
                    monochrome,
                    packedLight);
            poseStack.popPose();
            return;
        }
        if (blendMode == BlendMode.ADDITIVE) {
            AspectTagWorldRenderer.renderBillboardAdditive(
                    poseStack, buffers, aspect, scale, alpha, monochrome, packedLight);
        } else {
            AspectTagWorldRenderer.renderBillboard(poseStack, buffers, aspect, scale, alpha, monochrome, packedLight);
        }
    }

    /** Writes one aspect quad to a caller-provided vertex consumer. */
    public static void renderQuad(
            PoseStack poseStack,
            VertexConsumer buffer,
            Holder<IAspect> aspect,
            float alpha,
            boolean monochrome,
            int packedLight) {
        AspectTagWorldRenderer.renderQuad(poseStack, buffer, aspect, alpha, monochrome, packedLight);
    }
}
