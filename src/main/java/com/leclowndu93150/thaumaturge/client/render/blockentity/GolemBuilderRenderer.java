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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class GolemBuilderRenderer implements BlockEntityRenderer<BlockEntityGolemBuilder, GolemBuilderRenderState> {
    public static final Identifier MODEL = TCIds.rl("models/mesh/golembuilder.tcmesh");
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/golembuilder.png");
    private static final SpriteId LAVA_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/lava_still"));
    private static final String PRESS_PART = "press";
    private static final float PRESS_DROP = 0.625F;
    private static final float LAVA_OFFSET_X = -0.3125F;
    private static final float LAVA_OFFSET_Y = 0.625F;
    private static final float LAVA_OFFSET_Z = 1.3125F;
    private static final float LAVA_SIZE = 0.625F;
    private static final int LAVA_LIGHT = 200;

    public GolemBuilderRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public GolemBuilderRenderState createRenderState() {
        return new GolemBuilderRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityGolemBuilder builder, GolemBuilderRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(builder, state, partialTicks, cameraPosition, breakProgress);
        state.facing = builder.getBlockState().getValue(BlockGolemBuilder.FACING);
        state.press = builder.press;
    }

    @Override
    public void submit(GolemBuilderRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        switch (state.facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
            default -> {
            }
        }
        submitParts(state.press, poseStack, collector, state.lightCoords);
        submitLava(poseStack, collector);
        poseStack.popPose();
    }

    public static void submitParts(int press, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        TCMesh mesh = GolemMeshes.get(MODEL);
        RenderType type = RenderTypes.entityCutout(TEXTURE);
        for (TCMeshPart part : mesh.parts()) {
            if (!PRESS_PART.equals(part.name())) {
                collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, -1));
            }
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, (float) (-Math.sin(Math.toRadians(press)) * PRESS_DROP), 0.0F);
        for (TCMeshPart part : mesh.parts()) {
            if (PRESS_PART.equals(part.name())) {
                collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, -1));
            }
        }
        poseStack.popPose();
    }

    private static void submitLava(PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.pushPose();
        poseStack.translate(LAVA_OFFSET_X, LAVA_OFFSET_Y, LAVA_OFFSET_Z);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        poseStack.scale(LAVA_SIZE, LAVA_SIZE, LAVA_SIZE);
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(LAVA_SPRITE);
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(), (pose, buffer) -> {
            VertexConsumer wrapped = sprite.wrap(buffer);
            lavaVertex(wrapped, pose, 0.0F, 0.0F, sprite.getU1(), sprite.getV1());
            lavaVertex(wrapped, pose, 1.0F, 0.0F, sprite.getU0(), sprite.getV1());
            lavaVertex(wrapped, pose, 1.0F, 1.0F, sprite.getU0(), sprite.getV0());
            lavaVertex(wrapped, pose, 0.0F, 1.0F, sprite.getU1(), sprite.getV0());
        });
        poseStack.popPose();
    }

    private static void lavaVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v) {
        buffer.addVertex(pose, x, y, 0.0F).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LAVA_LIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityGolemBuilder builder) {
        return builder.getRenderBoundingBox();
    }
}
