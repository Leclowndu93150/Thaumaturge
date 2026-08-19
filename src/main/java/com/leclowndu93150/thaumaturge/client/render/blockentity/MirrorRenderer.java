package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirrorBase;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockMirror;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public final class MirrorRenderer implements BlockEntityRenderer<BlockEntityMirrorBase, MirrorRenderState> {
    public static final Identifier FRAME_MODEL_ID = TCIds.rl("block/mirror");
    public static final Identifier FRAME_ESSENTIA_MODEL_ID = TCIds.rl("block/mirror_essentia");
    public static final StandaloneModelKey<BlockStateModel> FRAME_MODEL = new StandaloneModelKey<>(FRAME_MODEL_ID::toString);
    public static final StandaloneModelKey<BlockStateModel> FRAME_ESSENTIA_MODEL = new StandaloneModelKey<>(FRAME_ESSENTIA_MODEL_ID::toString);

    private static final Identifier PANE_TEXTURE = TCIds.rl("textures/block/mirrorpane.png");
    private static final Identifier PANE_TRANS_TEXTURE = TCIds.rl("textures/block/mirrorpanetrans.png");

    private static final int[][] WINDOW_ROWS = {{2, 5, 11}, {3, 5, 11}, {4, 4, 12}, {5, 3, 13}, {6, 3, 13}, {7, 3, 13}, {8, 3, 13}, {9, 3, 13}, {10, 3, 13}, {11, 4, 12}, {12, 5, 11}, {13, 5, 11}};
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float PORTAL_PLANE = -0.5F + 0.018F;
    private static final float PANE_PLANE = -0.5F + 0.02F;
    private static final float FRAME_CENTER_LIFT = 0.46875F;
    private static final double PORTAL_RANGE_SQ = 1024.0;

    public MirrorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public MirrorRenderState createRenderState() {
        return new MirrorRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityMirrorBase mirror, MirrorRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(mirror, state, partialTicks, cameraPosition, breakProgress);
        state.facing = mirror.getBlockState().getValue(BlockMirror.FACING);
        state.linked = mirror.linked;
        var player = Minecraft.getInstance().player;
        state.inRange = player != null && player.distanceToSqr(Vec3.atCenterOf(mirror.getBlockPos())) < PORTAL_RANGE_SQ;
        state.frameParts.clear();
        boolean essentia = mirror.getBlockState().getBlock() instanceof BlockMirror mirrorBlock && mirrorBlock.isEssentia();
        BlockStateModel frameModel = Minecraft.getInstance().getModelManager().getStandaloneModel(essentia ? FRAME_ESSENTIA_MODEL : FRAME_MODEL);
        if (frameModel != null && mirror.getLevel() instanceof ClientLevel clientLevel) {
            frameModel.collectParts(clientLevel, mirror.getBlockPos(), mirror.getBlockState(), clientLevel.getRandom(), state.frameParts);
        }
    }

    @Override
    public void submit(MirrorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        float xRot = state.facing == Direction.UP ? 0.0F : state.facing == Direction.DOWN ? 180.0F : 90.0F;
        float yRot = switch (state.facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-xRot));
        if (!state.frameParts.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0F, -FRAME_CENTER_LIFT, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            collector.submitMultiLayerBlockModel(poseStack, state.frameParts, true, new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (state.linked && state.inRange) {
            collector.submitCustomGeometry(poseStack, EldritchPortalSurface.SURFACE, (pose, buffer) -> portalWindow(pose, buffer, state.blockPos));
        }
        RenderType paneType = RenderTypes.entityTranslucent(state.linked && state.inRange ? PANE_TRANS_TEXTURE : PANE_TEXTURE);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, paneType, (pose, buffer) -> paneQuad(pose, buffer, light));
        poseStack.popPose();
    }

    private static void portalWindow(PoseStack.Pose pose, VertexConsumer buffer, BlockPos worldPos) {
        for (int[] row : WINDOW_ROWS) {
            float z0 = row[0] * PIXEL - 0.5F;
            float z1 = z0 + PIXEL;
            float x0 = row[1] * PIXEL - 0.5F;
            float x1 = row[2] * PIXEL - 0.5F;
            EldritchPortalSurface.quad(pose, buffer, worldPos, x0, PORTAL_PLANE, z0, x0, PORTAL_PLANE, z1, x1, PORTAL_PLANE, z1, x1, PORTAL_PLANE, z0);
        }
    }

    private static void paneQuad(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        paneVertex(buffer, pose, -0.5F, -0.5F, 0.0F, 0.0F, light);
        paneVertex(buffer, pose, -0.5F, 0.5F, 0.0F, 1.0F, light);
        paneVertex(buffer, pose, 0.5F, 0.5F, 1.0F, 1.0F, light);
        paneVertex(buffer, pose, 0.5F, -0.5F, 1.0F, 0.0F, light);
    }

    private static void paneVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float z, float u, float v, int light) {
        buffer.addVertex(pose, x, PANE_PLANE, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
