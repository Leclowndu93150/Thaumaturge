package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class ResearchTableTop {
    private static final ResourceLocation PARCHMENT_TEXTURE = TTIds.rl("textures/misc/parchment.png");
    private static final int WHITE = 0xFFFFFFFF;
    private static final float CENTER = 0.5F;
    private static final float FRAME_HEIGHT = 1.0F;
    private static final float HALF_TURN = 180.0F;
    private static final float EXTENSION_SIDE_YAW = 90.0F;
    private static final float SHEET_X = 0.12F;
    private static final float SHEET_Z = 0.32F;
    private static final float SHEET_WIDTH = 0.48F;
    private static final float SHEET_LENGTH = 0.56F;
    private static final float SHEET_BASE_HEIGHT = 1.008F;
    private static final float SHEET_STEP = 0.013F;
    private static final float[] SHEET_TILTS = {-6.0F, 8.0F, 14.0F, -3.0F, 11.0F};
    private static final float LAY_FLAT = -90.0F;

    private ResearchTableTop() {}

    public static void pose(PoseStack poseStack, Direction facing) {
        poseStack.translate(CENTER, FRAME_HEIGHT, CENTER);
        BlockFacingPose.northBased(poseStack, facing);
        poseStack.mulPose(Axis.YP.rotationDegrees(EXTENSION_SIDE_YAW));
        poseStack.mulPose(Axis.XP.rotationDegrees(HALF_TURN));
    }

    public static void renderSheets(PoseStack poseStack, MultiBufferSource buffers, int light) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(PARCHMENT_TEXTURE));
        for (int sheet = 0; sheet < SHEET_TILTS.length; sheet++) {
            poseStack.pushPose();
            applySheetPose(poseStack, sheet);
            sheet(poseStack.last(), buffer, light);
            poseStack.popPose();
        }
    }

    static void applySheetPose(PoseStack poseStack, int sheet) {
        poseStack.translate(SHEET_X, FRAME_HEIGHT - (SHEET_BASE_HEIGHT + sheet * SHEET_STEP), SHEET_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(SHEET_TILTS[sheet]));
        poseStack.mulPose(Axis.XP.rotationDegrees(LAY_FLAT));
        poseStack.scale(SHEET_WIDTH, SHEET_LENGTH, 1.0F);
    }

    static void vertex(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float x,
            float y,
            float z,
            float u,
            float v,
            float nx,
            float ny,
            float nz,
            int light) {
        buffer.addVertex(pose, x, y, z)
                .setColor(WHITE)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static void sheet(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        vertex(pose, buffer, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        vertex(pose, buffer, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, -1.0F, light);
        vertex(pose, buffer, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
        vertex(pose, buffer, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F, light);
    }
}
