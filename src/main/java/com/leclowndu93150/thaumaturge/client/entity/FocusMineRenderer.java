package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.FocusMineModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityFocusMine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;

public final class FocusMineRenderer extends EntityRenderer<EntityFocusMine> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/focus_mine.png");
    private static final float PULSE_PERIOD = 5.0F;
    private static final float PULSE_AMPLITUDE = 0.25F;
    private static final float PULSE_BASE = 0.75F;
    private static final float UNARMED_BRIGHTNESS = 0.45F;
    private static final float SPIN_DEGREES_PER_TICK = 1.5F;
    private static final float GROUND_LIFT = 0.05F;
    private static final float COLOR_DIVISOR = 255.0F;

    private final FocusMineModel model;

    public FocusMineRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new FocusMineModel(context.bakeLayer(TTModelLayers.FOCUS_MINE));
    }

    @Override
    public void render(
            EntityFocusMine entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        float ticks = entity.tickCount + partialTicks;
        int color = entity.renderColor();
        float pulse =
                entity.isArmed() ? Mth.sin(ticks / PULSE_PERIOD) * PULSE_AMPLITUDE + PULSE_BASE : UNARMED_BRIGHTNESS;
        float r = ((color >> 16) & 0xFF) / COLOR_DIVISOR * pulse;
        float g = ((color >> 8) & 0xFF) / COLOR_DIVISOR * pulse;
        float b = (color & 0xFF) / COLOR_DIVISOR * pulse;
        int tint = ARGB32.colorFromFloat(1.0F, Math.min(r, 1.0F), Math.min(g, 1.0F), Math.min(b, 1.0F));
        poseStack.pushPose();
        poseStack.translate(0.0F, GROUND_LIFT, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(ticks * SPIN_DEGREES_PER_TICK));
        model.root.render(
                poseStack,
                buffers.getBuffer(RenderType.entityCutout(TEXTURE)),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                tint);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFocusMine entity) {
        return TEXTURE;
    }
}
