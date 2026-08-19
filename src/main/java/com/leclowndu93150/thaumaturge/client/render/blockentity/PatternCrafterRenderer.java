package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.BlockEntityPatternCrafter;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.BlockPatternCrafter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class PatternCrafterRenderer implements BlockEntityRenderer<BlockEntityPatternCrafter, PatternCrafterRenderState> {
    private static final Identifier MODES_TEXTURE = TCIds.rl("textures/block/pattern_crafter_modes.png");
    private static final Identifier GEAR_TEXTURE = TCIds.rl("textures/misc/gear_brass.png");

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
    public PatternCrafterRenderState createRenderState() {
        return new PatternCrafterRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityPatternCrafter crafter, PatternCrafterRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(crafter, state, partialTicks, cameraPosition, breakProgress);
        state.facing = crafter.getBlockState().getValue(BlockPatternCrafter.FACING);
        state.patternType = crafter.patternType();
        state.rotation = crafter.rot + crafter.rotSpeed * partialTicks;
    }

    @Override
    public void submit(PatternCrafterRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        float yRot = switch (state.facing) {
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        RenderType modeType = RenderTypes.entityCutout(MODES_TEXTURE);
        int light = state.lightCoords;
        int frame = state.patternType;
        collector.submitCustomGeometry(poseStack, modeType, (pose, buffer) -> {
            float u0 = frame / (float) MODE_FRAMES;
            float u1 = (frame + 1) / (float) MODE_FRAMES;
            float half = MODE_SIZE / 2.0F;
            quad(pose, buffer, -half, MODE_HEIGHT - half, -MODE_FORWARD, half, MODE_HEIGHT + half, -MODE_FORWARD, u0, u1, light);
        });

        RenderType gearType = RenderTypes.entityCutout(GEAR_TEXTURE);
        float rotation = state.rotation % 360.0F;
        for (int side = 0; side < 2; side++) {
            float offsetX = side == 0 ? -GEAR_OFFSET_X : GEAR_OFFSET_X;
            float spin = side == 0 ? -rotation : rotation;
            poseStack.pushPose();
            poseStack.translate(offsetX, GEAR_Y, -GEAR_FORWARD);
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
            collector.submitCustomGeometry(poseStack, gearType, (pose, buffer) -> {
                float half = GEAR_SIZE / 2.0F;
                quad(pose, buffer, -half, -half, 0.0F, half, half, 0.0F, 0.0F, 1.0F, light);
            });
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, float x0, float y0, float z, float x1, float y1, float z1, float u0, float u1, int light) {
        buffer.addVertex(pose, x0, y0, z).setColor(-1).setUv(u0, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        buffer.addVertex(pose, x0, y1, z1).setColor(-1).setUv(u0, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        buffer.addVertex(pose, x1, y1, z1).setColor(-1).setUv(u1, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        buffer.addVertex(pose, x1, y0, z).setColor(-1).setUv(u1, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
