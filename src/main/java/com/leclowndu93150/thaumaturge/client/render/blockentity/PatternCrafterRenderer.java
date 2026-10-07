package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.BlockEntityPatternCrafter;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.BlockPatternCrafter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class PatternCrafterRenderer implements BlockEntityRenderer<BlockEntityPatternCrafter> {
    private static final ResourceLocation MODES_TEXTURE = TTIds.rl("textures/block/pattern_crafter_modes.png");
    private static final ResourceLocation GEAR_TEXTURE = TTIds.rl("textures/misc/gear_brass.png");

    private static final int MODE_FRAMES = 10;
    private static final float MODE_SIZE = 0.5F;
    private static final float MODE_FORWARD = 0.501F;
    private static final float MODE_HEIGHT = 0.75F;
    private static final float GEAR_OFFSET_X = 0.2F;
    private static final float GEAR_Y = 0.34375F;
    private static final float GEAR_FORWARD = 0.505F;
    private static final float GEAR_SIZE = 0.5F;

    public PatternCrafterRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityPatternCrafter crafter,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        Direction facing = crafter.getBlockState().getValue(BlockPatternCrafter.FACING);
        int frame = crafter.patternType();
        float rotation = (crafter.rot + crafter.rotSpeed * partialTick) % 360.0F;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        float yRot =
                switch (facing) {
                    case SOUTH -> 180.0F;
                    case WEST -> 90.0F;
                    case EAST -> 270.0F;
                    default -> 0.0F;
                };
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        {
            VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(MODES_TEXTURE));
            PoseStack.Pose pose = poseStack.last();
            float u0 = frame / (float) MODE_FRAMES;
            float u1 = (frame + 1) / (float) MODE_FRAMES;
            float half = MODE_SIZE / 2.0F;
            quad(
                    pose,
                    buffer,
                    -half,
                    MODE_HEIGHT - half,
                    -MODE_FORWARD,
                    half,
                    MODE_HEIGHT + half,
                    -MODE_FORWARD,
                    u0,
                    u1,
                    light);
        }

        for (int side = 0; side < 2; side++) {
            float offsetX = side == 0 ? -GEAR_OFFSET_X : GEAR_OFFSET_X;
            float spin = side == 0 ? -rotation : rotation;
            poseStack.pushPose();
            poseStack.translate(offsetX, GEAR_Y, -GEAR_FORWARD);
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
            VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(GEAR_TEXTURE));
            PoseStack.Pose pose = poseStack.last();
            float half = GEAR_SIZE / 2.0F;
            quad(pose, buffer, -half, -half, 0.0F, half, half, 0.0F, 0.0F, 1.0F, light);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void quad(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float x0,
            float y0,
            float z,
            float x1,
            float y1,
            float z1,
            float u0,
            float u1,
            int light) {
        buffer.addVertex(pose, x0, y0, z)
                .setColor(-1)
                .setUv(u0, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
        buffer.addVertex(pose, x0, y1, z1)
                .setColor(-1)
                .setUv(u0, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
        buffer.addVertex(pose, x1, y1, z1)
                .setColor(-1)
                .setUv(u1, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
        buffer.addVertex(pose, x1, y0, z)
                .setColor(-1)
                .setUv(u1, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
