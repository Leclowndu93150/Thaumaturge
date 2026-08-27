package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.ArcaneBoreModel;
import com.leclowndu93150.thaumaturge.client.render.BoreDrillFx;
import com.leclowndu93150.thaumaturge.content.device.bore.BlockEntityArcaneBore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ArcaneBoreBlockRenderer implements BlockEntityRenderer<BlockEntityArcaneBore, ArcaneBoreBlockRenderState> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/arcanebore.png");
    private static final double BEAM_REACH = 6.0;

    private final ArcaneBoreModel model;

    public ArcaneBoreBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ArcaneBoreModel(context.bakeLayer(TCModelLayers.ARCANE_BORE));
    }

    @Override
    public ArcaneBoreBlockRenderState createRenderState() {
        return new ArcaneBoreBlockRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityArcaneBore bore, ArcaneBoreBlockRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(bore, state, partialTicks, cameraPosition, breakProgress);
        state.yaw = bore.renderYaw(partialTicks);
        state.pitch = bore.renderPitch(partialTicks);
        state.digging = bore.digging() && bore.boreActive();
        int ticks = (int) (bore.boreLevel().getGameTime() % Integer.MAX_VALUE);
        state.beamUvScroll = BoreDrillFx.beamUvScroll(ticks + partialTicks);
        state.beamSpin = BoreDrillFx.beamSpin(bore.boreLevel().getGameTime(), partialTicks);
        state.tip = BoreDrillFx.tipOffset(state.yaw, state.pitch, BlockEntityArcaneBore.EYE_HEIGHT).add(0.5, 0.0, 0.5);
        state.tipFrame = BoreDrillFx.tipFrame(ticks);
    }

    @Override
    public void submit(ArcaneBoreBlockRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, EntityModel.MODEL_Y_OFFSET, 0.0F);
        model.setAim(state.yaw, state.pitch);
        collector.submitModelPart(model.root(), poseStack, RenderTypes.entityTranslucent(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, null);
        poseStack.popPose();
        if (state.digging) {
            BoreDrillFx.submit(poseStack, collector, camera, state.tip, state.yaw, state.pitch, state.beamUvScroll, state.beamSpin, state.tipFrame);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityArcaneBore bore) {
        BlockPos pos = bore.getBlockPos();
        return new AABB(pos).inflate(BEAM_REACH);
    }
}
