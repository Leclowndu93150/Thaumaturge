package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.TCBannerModel;
import com.leclowndu93150.thaumaturge.content.decor.banner.AbstractBannerBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerStandingBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerWallBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BlockEntityBanner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class BannerRenderer implements BlockEntityRenderer<BlockEntityBanner, BannerRenderState> {
    private static final Identifier TEX_BLANK = TCIds.rl("textures/entity/banner_blank.png");
    private static final Identifier TEX_CULTIST = TCIds.rl("textures/entity/banner_cultist.png");
    private static final float SWAY_BASE = 0.02F;
    private static final float SWAY_PERIOD = 11.0F;
    private static final float WALL_FORWARD = -0.4125F;
    private static final float ASPECT_HALF_WIDTH = 0.3F;
    private static final float ASPECT_TOP = 0.35F;
    private static final float ASPECT_BOTTOM = 0.95F;
    private static final float ASPECT_Z = -0.052F;

    private final TCBannerModel model;

    public BannerRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TCBannerModel(context.bakeLayer(TCModelLayers.TC_BANNER));
    }

    @Override
    public BannerRenderState createRenderState() {
        return new BannerRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityBanner banner, BannerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(banner, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = banner.getBlockState();
        state.onWall = blockState.getBlock() instanceof BannerWallBlock;
        if (state.onWall) {
            Direction facing = blockState.getValue(BannerWallBlock.FACING);
            state.yawDegrees = switch (facing) {
                case WEST -> 90.0F;
                case NORTH -> 180.0F;
                case EAST -> 270.0F;
                default -> 0.0F;
            };
        } else {
            state.yawDegrees = blockState.getValue(BannerStandingBlock.ROTATION) * 22.5F;
        }
        DyeColor dye = blockState.getBlock() instanceof AbstractBannerBlock bannerBlock ? bannerBlock.dye() : null;
        state.color = dye == null ? -1 : 0xFF000000 | dye.getTextureDiffuseColor();
        state.aspectTexture = null;
        ResourceKey<IAspect> aspect = banner.aspect();
        if (aspect != null && banner.getLevel() != null) {
            state.aspectTexture = banner.getLevel().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(aspect).map(Holder::value).map(IAspect::texture).orElse(null);
        }
        BlockPos pos = banner.getBlockPos();
        float time = (pos.getX() * 7 + pos.getY() * 9 + pos.getZ() * 13) + (Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount) + partialTicks;
        state.sway = SWAY_BASE - Mth.sin(time / SWAY_PERIOD) * SWAY_BASE;
    }

    @Override
    public void submit(BannerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Identifier texture = state.color == -1 ? TEX_CULTIST : TEX_BLANK;
        int tint = state.color == -1 ? -1 : state.color;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F + state.yawDegrees));
        if (state.onWall) {
            poseStack.translate(0.0F, 1.0F, WALL_FORWARD);
        } else {
            submitPart(collector, poseStack, model.pole, texture, -1, state);
        }
        submitPart(collector, poseStack, model.beam, texture, -1, state);
        submitPart(collector, poseStack, model.tabLeft, texture, tint, state);
        submitPart(collector, poseStack, model.tabRight, texture, tint, state);
        model.cloth.xRot = state.sway;
        submitPart(collector, poseStack, model.cloth, texture, tint, state);
        if (state.aspectTexture != null) {
            submitAspect(collector, poseStack, state);
        }
        poseStack.popPose();
    }

    private void submitPart(SubmitNodeCollector collector, PoseStack poseStack, ModelPart part, Identifier texture, int color, BannerRenderState state) {
        collector.submitModelPart(part, poseStack, RenderTypes.entityCutout(texture), state.lightCoords, OverlayTexture.NO_OVERLAY, null, color, null);
    }

    private void submitAspect(SubmitNodeCollector collector, PoseStack poseStack, BannerRenderState state) {
        poseStack.pushPose();
        poseStack.translate(0.0F, -5.0F / 16.0F, 0.0F);
        poseStack.mulPose(Axis.XP.rotation(state.sway));
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(state.aspectTexture), (pose, buffer) -> {
            Matrix4fc mat = pose.pose();
            int color = ARGB.colorFromFloat(0.75F, 1.0F, 1.0F, 1.0F);
            addVertex(buffer, mat, -ASPECT_HALF_WIDTH, ASPECT_TOP, 0.0F, 1.0F, color, light);
            addVertex(buffer, mat, ASPECT_HALF_WIDTH, ASPECT_TOP, 1.0F, 1.0F, color, light);
            addVertex(buffer, mat, ASPECT_HALF_WIDTH, ASPECT_BOTTOM, 1.0F, 0.0F, color, light);
            addVertex(buffer, mat, -ASPECT_HALF_WIDTH, ASPECT_BOTTOM, 0.0F, 0.0F, color, light);
        });
        poseStack.popPose();
    }

    private static void addVertex(VertexConsumer buffer, Matrix4fc mat, float x, float y, float u, float v, int color, int light) {
        buffer.addVertex(mat, x, y, ASPECT_Z).setUv(u, v).setColor(color).setLight(light).setNormal(0.0F, 0.0F, -1.0F).setOverlay(OverlayTexture.NO_OVERLAY);
    }
}
