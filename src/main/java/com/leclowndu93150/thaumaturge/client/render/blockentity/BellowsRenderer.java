package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockBellows;
import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockEntityBellows;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public class BellowsRenderer implements BlockEntityRenderer<BlockEntityBellows, BellowsRenderState> {

    public static final String[] parts = new String[]{"bottom_plank", "top_plank", "bag"};

    public static final StandaloneModelKey<BlockStateModel>[] MODEL_KEYS = new StandaloneModelKey[3];

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public BellowsRenderState createRenderState() {
        return new BellowsRenderState();
    }

    @Override
    public void submit(BellowsRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (state.models != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            BlockFacingPose.northBased(poseStack, state.facing);
            poseStack.translate(-0.5, -0.5, -0.5);

            {
                poseStack.pushPose();
                poseStack.translate(0, 0.5F, 0F);
                poseStack.translate(0F, (state.scale + 0.1F) * -0.5f, 0F);
                poseStack.scale(1F, (state.scale + 0.1F), 1F);
                submitNodeCollector.submitMultiLayerBlockModel(poseStack, state.parts[2], true, new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

                if (state.breakProgress != null)
                    submitNodeCollector.submitBreakingBlockModel(poseStack, state.models[2], state.seed, state.breakProgress.progress());
                poseStack.popPose();
            }

            {
                poseStack.pushPose();
                poseStack.translate(0F, (1 - state.scale) * -0.25F, 0F);
                submitNodeCollector.submitMultiLayerBlockModel(poseStack, state.parts[1], true, new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

                if (state.breakProgress != null)
                    submitNodeCollector.submitBreakingBlockModel(poseStack, state.models[1], state.seed, state.breakProgress.progress());
                poseStack.popPose();
            }

            {
                poseStack.pushPose();
                poseStack.translate(0F, (1 - state.scale) * 0.25F, 0F);
                submitNodeCollector.submitMultiLayerBlockModel(poseStack, state.parts[0], true, new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

                if (state.breakProgress != null)
                    submitNodeCollector.submitBreakingBlockModel(poseStack, state.models[0], state.seed, state.breakProgress.progress());
                poseStack.popPose();
            }

            poseStack.popPose();
        }
    }

    @Override
    public void extractRenderState(BlockEntityBellows blockEntity, BellowsRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = blockEntity.getBlockState().getValue(BlockBellows.FACING);
        state.scale = blockEntity.inflation(partialTicks);
        state.seed = blockEntity.getBlockState().getSeed(blockEntity.getBlockPos());
        BlockEntity be = blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos().relative(state.facing));
        if (be != null && be instanceof BlockEntityTubeBuffer)
            state.extension = true;
        for (int i = 0; i < parts.length; i++) {
            state.parts[i] = new ArrayList<>();
            state.models[i] = Minecraft.getInstance().getModelManager().getStandaloneModel(MODEL_KEYS[i]);
            if (state.models[i] != null) {
                state.models[i].collectParts((ClientLevel) blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getLevel().getRandom(), state.parts[i]);
            }
        }
    }
}
