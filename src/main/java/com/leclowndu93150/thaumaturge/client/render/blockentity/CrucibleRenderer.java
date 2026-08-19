package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class CrucibleRenderer implements BlockEntityRenderer<BlockEntityCrucible, CrucibleRenderState> {

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public CrucibleRenderState createRenderState() {
        return new CrucibleRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityCrucible blockEntity, CrucibleRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.fluid = blockEntity.getTank().getResource(0).toStack(blockEntity.getTank().getAmountAsInt(0));
        state.fluidHeight = blockEntity.getFluidHeight();
        var fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state.fluid.getFluid().defaultFluidState());
        state.sprite = fluidModel.stillMaterial().sprite();
        var tintSource = fluidModel.fluidTintSource();
        if (tintSource != null) {
            state.color = tintSource.colorAsStack(state.fluid);
        } else {
            state.color = -1;
        }

        float recolor = (float) blockEntity.getAspects().totalAmount() / BlockEntityCrucible.MAX_ASPECT;
        if (recolor > 0.0F) {
            recolor = 0.5F + recolor / 2F;
        }

        if (recolor > 1F)
            recolor = 1F;

        float r = Mth.lerp(recolor, ARGB.redFloat(state.color), 0.25F);
        float g = Mth.lerp(recolor, ARGB.greenFloat(state.color), 0.0F);
        float b = Mth.lerp(recolor, ARGB.blueFloat(state.color), 0.75F);
        state.color = ARGB.colorFromFloat(1F, r, g, b);
    }

    @Override
    public void submit(CrucibleRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.fluid.isEmpty())
            return;
        float height = state.fluidHeight;
        float min = 0 / 16F, max = 16 / 16F;
        submitNodeCollector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(), (pose, buffer) -> {
            VertexConsumer wrapped = state.sprite.wrap(buffer);
            JarRenderer.addVertex(wrapped, pose, min, height, min, min, min, state.color, state.lightCoords, 0, 1, 0);
            JarRenderer.addVertex(wrapped, pose, min, height, max, min, max, state.color, state.lightCoords, 0, 1, 0);
            JarRenderer.addVertex(wrapped, pose, max, height, max, max, max, state.color, state.lightCoords, 0, 1, 0);
            JarRenderer.addVertex(wrapped, pose, max, height, min, max, min, state.color, state.lightCoords, 0, 1, 0);
        });
    }
}
