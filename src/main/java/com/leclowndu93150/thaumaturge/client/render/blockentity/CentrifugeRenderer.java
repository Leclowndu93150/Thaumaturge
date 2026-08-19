package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.CentrifugeModel;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEntityCentrifuge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class CentrifugeRenderer implements BlockEntityRenderer<BlockEntityCentrifuge, CentrifugeRenderState> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/centrifuge.png");

    private final CentrifugeModel model;

    public CentrifugeRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CentrifugeModel(context.bakeLayer(TCModelLayers.CENTRIFUGE));
    }

    @Override
    public CentrifugeRenderState createRenderState() {
        return new CentrifugeRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityCentrifuge centrifuge, CentrifugeRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(centrifuge, state, partialTicks, cameraPosition, breakProgress);
        state.rotation = centrifuge.rotation + centrifuge.rotationSpeed * partialTicks;
    }

    @Override
    public void submit(CentrifugeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        collector.submitModelPart(model.boxes, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, null);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        collector.submitModelPart(model.spinner, poseStack, RenderTypes.entityCutout(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null, -1, null);
        poseStack.popPose();
    }
}
