package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public final class GolemBuilderRenderer implements BlockEntityRenderer<BlockEntityGolemBuilder> {
    public static final ResourceLocation MODEL = TCIds.rl("models/mesh/golembuilder.tcmesh");
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/golembuilder.png");
    private static final Material LAVA_MATERIAL =
            new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/lava_still"));
    private static final String PRESS_PART = "press";
    private static final float PRESS_DROP = 0.625F;
    private static final float LAVA_OFFSET_X = -0.3125F;
    private static final float LAVA_OFFSET_Y = 0.625F;
    private static final float LAVA_OFFSET_Z = 1.3125F;
    private static final float LAVA_SIZE = 0.625F;
    private static final int LAVA_LIGHT = 200;

    public GolemBuilderRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityGolemBuilder builder,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        Direction facing = builder.getBlockState().getValue(BlockGolemBuilder.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
            default -> {}
        }
        submitParts(builder.press, poseStack, buffers, light);
        submitLava(poseStack, buffers);
        poseStack.popPose();
    }

    public static void submitParts(int press, PoseStack poseStack, MultiBufferSource buffers, int light) {
        TCMesh mesh = GolemMeshes.get(MODEL);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        for (TCMeshPart part : mesh.parts()) {
            if (!PRESS_PART.equals(part.name())) {
                GolemMeshes.renderPart(part, poseStack.last(), buffer, light, -1);
            }
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, (float) (-Math.sin(Math.toRadians(press)) * PRESS_DROP), 0.0F);
        for (TCMeshPart part : mesh.parts()) {
            if (PRESS_PART.equals(part.name())) {
                GolemMeshes.renderPart(part, poseStack.last(), buffer, light, -1);
            }
        }
        poseStack.popPose();
    }

    private static void submitLava(PoseStack poseStack, MultiBufferSource buffers) {
        poseStack.pushPose();
        poseStack.translate(LAVA_OFFSET_X, LAVA_OFFSET_Y, LAVA_OFFSET_Z);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        poseStack.scale(LAVA_SIZE, LAVA_SIZE, LAVA_SIZE);
        TextureAtlasSprite sprite = LAVA_MATERIAL.sprite();
        VertexConsumer wrapped = sprite.wrap(buffers.getBuffer(Sheets.translucentCullBlockSheet()));
        PoseStack.Pose pose = poseStack.last();
        lavaVertex(wrapped, pose, 0.0F, 0.0F, sprite.getU1(), sprite.getV1());
        lavaVertex(wrapped, pose, 1.0F, 0.0F, sprite.getU0(), sprite.getV1());
        lavaVertex(wrapped, pose, 1.0F, 1.0F, sprite.getU0(), sprite.getV0());
        lavaVertex(wrapped, pose, 0.0F, 1.0F, sprite.getU1(), sprite.getV0());
        poseStack.popPose();
    }

    private static void lavaVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v) {
        buffer.addVertex(pose, x, y, 0.0F)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LAVA_LIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityGolemBuilder builder) {
        return builder.getRenderBoundingBox();
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityGolemBuilder builder) {
        return true;
    }
}
