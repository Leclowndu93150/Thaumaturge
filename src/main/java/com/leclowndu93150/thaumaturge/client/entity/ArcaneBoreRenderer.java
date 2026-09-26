package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.entity.ArcaneBoreModel;
import com.leclowndu93150.thaumaturge.client.render.BoreDrillFx;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityArcaneBore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class ArcaneBoreRenderer extends MobRenderer<EntityArcaneBore, ArcaneBoreRenderState, ArcaneBoreModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/arcanebore.png");
    private static final float SHADOW = 0.5F;

    public ArcaneBoreRenderer(EntityRendererProvider.Context context) {
        super(context, new ArcaneBoreModel(context.bakeLayer(TCModelLayers.ARCANE_BORE)), SHADOW);
    }

    @Override
    public ArcaneBoreRenderState createRenderState() {
        return new ArcaneBoreRenderState();
    }

    @Override
    public void extractRenderState(EntityArcaneBore entity, ArcaneBoreRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = Mth.wrapDegrees(Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot));
        state.bodyRot = 0.0F;
        state.digging = entity.clientDiggingSmoothed() && entity.isActive() && entity.validInventory();
        state.headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.beamUvScroll = BoreDrillFx.beamUvScroll(entity.tickCount + partialTicks);
        state.beamSpin = BoreDrillFx.beamSpin(entity.level().getGameTime(), partialTicks);
        state.tip = BoreDrillFx.tipOffset(state.yRot, state.headPitch, entity.getEyeHeight());
        state.tipFrame = BoreDrillFx.tipFrame(entity.tickCount);
    }

    @Override
    public void submit(ArcaneBoreRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.digging) {
            BoreDrillFx.submit(poseStack, collector, camera, state.tip, state.yRot, state.headPitch, state.beamUvScroll, state.beamSpin, state.tipFrame);
        }
    }

    @Override
    public Identifier getTextureLocation(ArcaneBoreRenderState state) {
        return TEXTURE;
    }
}
