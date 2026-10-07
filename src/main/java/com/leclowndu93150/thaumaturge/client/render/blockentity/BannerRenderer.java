package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.TTBannerModel;
import com.leclowndu93150.thaumaturge.content.decor.banner.AbstractBannerBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerStandingBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerWallBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BlockEntityBanner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public final class BannerRenderer implements BlockEntityRenderer<BlockEntityBanner> {
    private static final ResourceLocation TEX_BLANK = TTIds.rl("textures/entity/banner_blank.png");
    private static final ResourceLocation TEX_CULTIST = TTIds.rl("textures/entity/banner_cultist.png");
    private static final float SWAY_PERIOD = 11.0F;
    private static final float MODEL_FLIP = 180.0F;
    private static final int UPPER_CLOTH_SEGMENT = 0;
    private static final float WALL_FORWARD = -0.21875F;
    private static final float ASPECT_HALF_WIDTH = 0.3F;
    private static final float ASPECT_TOP = 0.35F;
    private static final float ASPECT_BOTTOM = 0.95F;
    private static final float ASPECT_Z = -0.052F;

    private final TTBannerModel frame;
    private final TTBannerModel cloth;

    public BannerRenderer(BlockEntityRendererProvider.Context context) {
        this.frame = new TTBannerModel(context.bakeLayer(TTModelLayers.TT_BANNER), false);
        this.cloth = new TTBannerModel(context.bakeLayer(TTModelLayers.TT_BANNER), true);
    }

    @Override
    public void render(
            BlockEntityBanner banner,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BlockState blockState = banner.getBlockState();
        boolean onWall = blockState.getBlock() instanceof BannerWallBlock;
        float yawDegrees;
        if (onWall) {
            Direction facing = blockState.getValue(BannerWallBlock.FACING);
            yawDegrees = switch (facing) {
                case WEST -> 90.0F;
                case NORTH -> 180.0F;
                case EAST -> 270.0F;
                default -> 0.0F;
            };
        } else {
            yawDegrees = blockState.getValue(BannerStandingBlock.ROTATION) * 22.5F;
        }
        DyeColor dye = blockState.getBlock() instanceof AbstractBannerBlock bannerBlock ? bannerBlock.dye() : null;
        int color = dye == null ? -1 : 0xFF000000 | dye.getTextureDiffuseColor();
        ResourceLocation aspectTexture = null;
        ResourceKey<IAspect> aspect = banner.aspect();
        if (aspect != null && banner.getLevel() != null) {
            aspectTexture = banner.getLevel()
                    .registryAccess()
                    .lookupOrThrow(IAspect.REGISTRY_KEY)
                    .get(aspect)
                    .map(Holder::value)
                    .map(IAspect::texture)
                    .orElse(null);
        }
        BlockPos pos = banner.getBlockPos();
        float time = (pos.getX() * 7 + pos.getY() * 9 + pos.getZ() * 13)
                + (Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount)
                + partialTick;
        float phase = time / SWAY_PERIOD;
        float sway = -TTBannerModel.clothBend(phase, UPPER_CLOTH_SEGMENT);

        ResourceLocation texture = color == -1 ? TEX_CULTIST : TEX_BLANK;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F + yawDegrees));
        VertexConsumer cutout = buffers.getBuffer(RenderType.entityCutout(texture));
        if (onWall) {
            poseStack.translate(0.0F, 1.0F, WALL_FORWARD);
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(MODEL_FLIP));
        frame.setupAnim(onWall, phase);
        cloth.setupAnim(onWall, phase);
        frame.root.render(poseStack, cutout, light, overlay, -1);
        cloth.root.render(poseStack, cutout, light, overlay, color);
        poseStack.popPose();
        if (aspectTexture != null) {
            submitAspect(poseStack, buffers, sway, aspectTexture, light);
        }
        poseStack.popPose();
    }

    private void submitAspect(
            PoseStack poseStack, MultiBufferSource buffers, float sway, ResourceLocation aspectTexture, int light) {
        poseStack.pushPose();
        poseStack.translate(0.0F, -5.0F / 16.0F, 0.0F);
        poseStack.mulPose(Axis.XP.rotation(sway));
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(aspectTexture));
        Matrix4f mat = poseStack.last().pose();
        int color = ARGB32.colorFromFloat(0.75F, 1.0F, 1.0F, 1.0F);
        addVertex(buffer, mat, -ASPECT_HALF_WIDTH, ASPECT_TOP, 0.0F, 1.0F, color, light);
        addVertex(buffer, mat, ASPECT_HALF_WIDTH, ASPECT_TOP, 1.0F, 1.0F, color, light);
        addVertex(buffer, mat, ASPECT_HALF_WIDTH, ASPECT_BOTTOM, 1.0F, 0.0F, color, light);
        addVertex(buffer, mat, -ASPECT_HALF_WIDTH, ASPECT_BOTTOM, 0.0F, 0.0F, color, light);
        poseStack.popPose();
    }

    private static void addVertex(
            VertexConsumer buffer, Matrix4f mat, float x, float y, float u, float v, int color, int light) {
        buffer.addVertex(mat, x, y, ASPECT_Z)
                .setUv(u, v)
                .setColor(color)
                .setLight(light)
                .setNormal(0.0F, 0.0F, -1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY);
    }
}
