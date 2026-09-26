package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeValve;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public final class TubeValveRenderer implements BlockEntityRenderer<BlockEntityTubeValve, TubeValveRenderState> {
    public static final Identifier MODEL_ID = TCIds.rl("block/tube_valve_head");
    public static final StandaloneModelKey<BlockStateModel> MODEL = new StandaloneModelKey<>(MODEL_ID::toString);

    public TubeValveRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public TubeValveRenderState createRenderState() {
        return new TubeValveRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityTubeValve valve, TubeValveRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(valve, state, partialTicks, cameraPosition, breakProgress);
        state.facing = valve.facing();
        state.rotation = valve.rotation(partialTicks);
        state.seed = valve.getBlockState().getSeed(valve.getBlockPos());
        state.parts.clear();
        state.model = Minecraft.getInstance().getModelManager().getStandaloneModel(MODEL);
        if (state.model != null && valve.getLevel() instanceof ClientLevel clientLevel) {
            state.model.collectParts(clientLevel, valve.getBlockPos(), valve.getBlockState(), clientLevel.getRandom(), state.parts);
        }
    }

    @Override
    public void submit(TubeValveRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.parts.isEmpty())
            return;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        orientTo(state.facing, poseStack);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.rotation * 1.5F));
        poseStack.translate(0.0F, -0.03F - state.rotation / 360.0F * 0.09F, 0.0F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        collector.submitMultiLayerBlockModel(poseStack, state.parts, true, new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        if (state.breakProgress != null && state.model != null) {
            collector.submitBreakingBlockModel(poseStack, state.model, state.seed, state.breakProgress.progress());
        }
        poseStack.popPose();
    }

    private static void orientTo(Direction direction, PoseStack poseStack) {
        switch (direction) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
            case UP -> {
            }
        }
    }
}
