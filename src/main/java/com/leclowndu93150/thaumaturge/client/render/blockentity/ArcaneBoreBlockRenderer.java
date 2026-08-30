package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.ArcaneBoreModel;
import com.leclowndu93150.thaumaturge.client.render.BoreDrillFx;
import com.leclowndu93150.thaumaturge.content.device.bore.BlockEntityArcaneBore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ArcaneBoreBlockRenderer implements BlockEntityRenderer<BlockEntityArcaneBore> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/arcanebore.png");
    private static final double BEAM_REACH = 6.0;

    private final ArcaneBoreModel model;

    public ArcaneBoreBlockRenderer(BlockEntityRendererProvider.Context context) {
        model = new ArcaneBoreModel(context.bakeLayer(TCModelLayers.ARCANE_BORE));
    }

    @Override
    public void render(
            BlockEntityArcaneBore bore,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay) {
        float yaw = bore.renderYaw(partialTicks);
        float pitch = bore.renderPitch(partialTicks);
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        model.setAim(yaw, pitch);
        model.renderToBuffer(
                poseStack,
                buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                -1);
        poseStack.popPose();

        if (bore.digging() && bore.boreActive()) {
            float ticks = bore.getLevel().getGameTime() + partialTicks;
            Vec3 tip = BoreDrillFx.tipOffset(yaw, pitch, BlockEntityArcaneBore.EYE_HEIGHT)
                    .add(0.5, 0.0, 0.5);
            BoreDrillFx.render(
                    poseStack,
                    buffers,
                    Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation(),
                    tip,
                    yaw,
                    pitch,
                    BoreDrillFx.beamUvScroll(ticks),
                    BoreDrillFx.beamSpin(bore.getLevel().getGameTime(), partialTicks),
                    BoreDrillFx.tipFrame(Mth.floor(ticks)));
        }
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityArcaneBore bore) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityArcaneBore bore) {
        BlockPos pos = bore.getBlockPos();
        return new AABB(pos).inflate(BEAM_REACH);
    }
}
