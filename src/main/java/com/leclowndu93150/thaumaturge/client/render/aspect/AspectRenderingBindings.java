package com.leclowndu93150.thaumaturge.client.render.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledge;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.client.AspectRendering;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public final class AspectRenderingBindings implements AspectRendering.Bindings {

    @Override
    public void renderGui(
            GuiGraphics graphics,
            Font font,
            int x,
            int y,
            @Nullable Holder<IAspect> aspect,
            float amount,
            AspectKnowledge knowledge) {
        if (aspect == null) {
            AspectTagRenderer.renderMissingChip(graphics, x, y);
        } else if (knowledge.isKnown()) {
            AspectTagRenderer.render(graphics, font, x, y, aspect, amount);
        } else {
            AspectTagRenderer.renderMaskedChip(graphics, x, y, aspect, knowledge);
        }
    }

    @Override
    public RenderType renderType(
            @Nullable Holder<IAspect> aspect, AspectKnowledge knowledge, AspectRendering.BlendMode blend) {
        ResourceLocation texture = aspect != null && knowledge.isKnown()
                ? aspect.value().texture()
                : AspectTagWorldRenderer.UNKNOWN_TEXTURE;
        return blend == AspectRendering.BlendMode.ADDITIVE
                ? TTFlatRenderTypes.entityAdditiveFlat(texture)
                : TTFlatRenderTypes.entityTranslucentFlat(texture);
    }

    @Override
    public void renderQuad(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            @Nullable Holder<IAspect> aspect,
            AspectKnowledge knowledge,
            float alpha,
            boolean monochrome,
            int packedLight) {
        int color = aspect == null
                ? FastColor.ARGB32.colorFromFloat(Mth.clamp(alpha, 0.0F, 1.0F), 1.0F, 1.0F, 1.0F)
                : AspectTagRenderer.colorOf(aspect.value(), alpha, monochrome || !knowledge.isKnown());
        AspectTagWorldRenderer.renderQuad(pose, buffer, color, packedLight);
    }
}
