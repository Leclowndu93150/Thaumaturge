package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEntityCentrifuge;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public final class CentrifugeRenderer implements BlockEntityRenderer<BlockEntityCentrifuge, CentrifugeRenderState> {
    public static final Identifier SPINNER_MODEL_ID = TTIds.rl("block/centrifuge_spinner");
    public static final StandaloneModelKey<BlockStateModel> SPINNER_MODEL = new StandaloneModelKey<>(SPINNER_MODEL_ID::toString);
    private static final float BLOCK_CENTER = 0.5F;
    private static final int[] NO_TINTS = new int[0];

    public CentrifugeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public CentrifugeRenderState createRenderState() {
        return new CentrifugeRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityCentrifuge centrifuge, CentrifugeRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(centrifuge, state, partialTicks, cameraPosition, breakProgress);
        state.rotation = centrifuge.rotation + centrifuge.rotationSpeed * partialTicks;
        state.spinnerParts.clear();
        BlockStateModel spinner = Minecraft.getInstance().getModelManager().getStandaloneModel(SPINNER_MODEL);
        if (spinner != null && centrifuge.getLevel() instanceof ClientLevel clientLevel) {
            spinner.collectParts(clientLevel, centrifuge.getBlockPos(), centrifuge.getBlockState(), clientLevel.getRandom(), state.spinnerParts);
        }
    }

    @Override
    public void submit(CentrifugeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.spinnerParts.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(BLOCK_CENTER, BLOCK_CENTER, BLOCK_CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        poseStack.translate(-BLOCK_CENTER, -BLOCK_CENTER, -BLOCK_CENTER);
        collector.submitMultiLayerBlockModel(poseStack, state.spinnerParts, true, NO_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
