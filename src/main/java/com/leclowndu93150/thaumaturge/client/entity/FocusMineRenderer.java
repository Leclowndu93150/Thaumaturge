package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.GrapplerModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityFocusMine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class FocusMineRenderer extends EntityRenderer<EntityFocusMine, FocusMineRenderer.State> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/grappler.png");
    private static final float PULSE_PERIOD = 5.0F;
    private static final float PULSE_AMPLITUDE = 0.25F;
    private static final float PULSE_BASE = 0.75F;
    private static final float UNARMED_BRIGHTNESS = 0.45F;
    private static final float SPIN_DEGREES_PER_TICK = 1.5F;
    private static final float GROUND_LIFT = 0.05F;
    private static final float COLOR_DIVISOR = 255.0F;

    public static final class State extends EntityRenderState {
        float ticks;
        boolean armed;
        int color = 0xFFFFFF;
    }

    private final GrapplerModel model;

    public FocusMineRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new GrapplerModel(context.bakeLayer(TCModelLayers.GRAPPLER));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityFocusMine entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.ticks = entity.tickCount + partialTicks;
        state.armed = entity.isArmed();
        state.color = entity.renderColor();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        float pulse = state.armed ? Mth.sin(state.ticks / PULSE_PERIOD) * PULSE_AMPLITUDE + PULSE_BASE : UNARMED_BRIGHTNESS;
        float r = ((state.color >> 16) & 0xFF) / COLOR_DIVISOR * pulse;
        float g = ((state.color >> 8) & 0xFF) / COLOR_DIVISOR * pulse;
        float b = (state.color & 0xFF) / COLOR_DIVISOR * pulse;
        int tint = ARGB.colorFromFloat(1.0F, Math.min(r, 1.0F), Math.min(g, 1.0F), Math.min(b, 1.0F));
        poseStack.pushPose();
        poseStack.translate(0.0F, GROUND_LIFT, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks * SPIN_DEGREES_PER_TICK));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        collector.submitModelPart(model.root, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null, tint, null);
        poseStack.popPose();
    }
}
