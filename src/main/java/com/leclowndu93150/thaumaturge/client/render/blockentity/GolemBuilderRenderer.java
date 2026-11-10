package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;

public final class GolemBuilderRenderer implements BlockEntityRenderer<BlockEntityGolemBuilder> {
    public static final ResourceLocation MODEL = TTIds.rl("models/mesh/golembuilder.ttmesh");
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/golembuilder.png");
    private static final Material LAVA_MATERIAL =
            new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.withDefaultNamespace("block/lava_still"));
    private static final String PRESS_PART = "press";
    private static final float PRESS_REST_BOTTOM = 1.6875F;
    private static final float BASE_TOP = 1.0F;
    private static final float PRESS_CLEARANCE = 0.0625F;
    private static final float PRESS_TRAVEL = PRESS_REST_BOTTOM - BASE_TOP - PRESS_CLEARANCE;
    private static final float LAVA_HEIGHT = 0.75F;
    private static final float LAVA_HALF_WIDTH = 0.3125F;
    private static final float LAVA_NEAR = 0.6875F;
    private static final float LAVA_FAR = 1.3125F;
    private static final int LAVA_LIGHT = LightTexture.FULL_BRIGHT;

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
        TTMesh mesh = GolemMeshes.get(MODEL);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        for (TTMeshPart part : mesh.parts()) {
            if (!PRESS_PART.equals(part.name())) {
                GolemMeshes.renderPart(part, poseStack.last(), buffer, light, -1);
            }
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, -PRESS_TRAVEL * Mth.sin(press * Mth.DEG_TO_RAD), 0.0F);
        for (TTMeshPart part : mesh.parts()) {
            if (PRESS_PART.equals(part.name())) {
                GolemMeshes.renderPart(part, poseStack.last(), buffer, light, -1);
            }
        }
        poseStack.popPose();
    }

    private static void submitLava(PoseStack poseStack, MultiBufferSource buffers) {
        TextureAtlasSprite sprite = LAVA_MATERIAL.sprite();
        VertexConsumer wrapped = sprite.wrap(buffers.getBuffer(Sheets.translucentItemSheet()));
        lavaQuad(poseStack.last(), wrapped);
    }

    static void lavaQuad(PoseStack.Pose pose, VertexConsumer buffer) {
        lavaVertex(buffer, pose, -LAVA_HALF_WIDTH, LAVA_FAR, 1.0F, 1.0F);
        lavaVertex(buffer, pose, LAVA_HALF_WIDTH, LAVA_FAR, 0.0F, 1.0F);
        lavaVertex(buffer, pose, LAVA_HALF_WIDTH, LAVA_NEAR, 0.0F, 0.0F);
        lavaVertex(buffer, pose, -LAVA_HALF_WIDTH, LAVA_NEAR, 1.0F, 0.0F);
    }

    private static void lavaVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float z, float u, float v) {
        buffer.addVertex(pose, x, LAVA_HEIGHT, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LAVA_LIGHT)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityGolemBuilder builder) {
        return builder.renderBounds();
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityGolemBuilder builder) {
        return true;
    }
}
