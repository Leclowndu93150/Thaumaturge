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

    /** Renders a placeholder for a syntactically valid aspect id missing from the registry. */
    public static void renderMissingGui(GuiGraphics graphics, int x, int y, ResourceLocation missingId) {
        java.util.Objects.requireNonNull(missingId, "missingId");
        AspectTagRenderer.renderMissingChip(graphics, x, y);
    }

    /** Returns the render type for a holder-free missing-aspect placeholder. */
    public static RenderType missingRenderType(BlendMode blendMode) {
        return blendMode == BlendMode.ADDITIVE
                ? TCFlatRenderTypes.entityAdditiveFlat(AspectTagWorldRenderer.UNKNOWN_TEXTURE)
                : TCFlatRenderTypes.entityTranslucentFlat(AspectTagWorldRenderer.UNKNOWN_TEXTURE);
    }

    /** Renders a camera-facing placeholder for a missing registry entry. */
    public static void renderMissingBillboard(
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation missingId,
            float scale,
            float alpha,
            int packedLight,
            BlendMode blendMode) {
        java.util.Objects.requireNonNull(missingId, "missingId");
        poseStack.pushPose();
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.scale(scale, scale, scale);
        AspectTagWorldRenderer.renderMissingQuad(
                poseStack, buffers.getBuffer(missingRenderType(blendMode)), alpha, packedLight);
        poseStack.popPose();
    }

    /** Writes a holder-free missing-aspect placeholder to a caller-provided vertex consumer. */
    public static void renderMissingQuad(
            PoseStack poseStack, VertexConsumer buffer, ResourceLocation missingId, float alpha, int packedLight) {
        java.util.Objects.requireNonNull(missingId, "missingId");
        AspectTagWorldRenderer.renderMissingQuad(poseStack, buffer, alpha, packedLight);
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
