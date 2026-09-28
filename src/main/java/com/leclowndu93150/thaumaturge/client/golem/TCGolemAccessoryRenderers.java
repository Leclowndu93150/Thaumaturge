package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryAnchor;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryRenderContext;
import com.leclowndu93150.thaumaturge.api.client.golems.RegisterGolemAccessoryRenderersEvent;
import com.leclowndu93150.thaumaturge.client.render.BoxGeometry;
import com.leclowndu93150.thaumaturge.registry.TCGolemAccessories;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCGolemAccessoryRenderers {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/models/golem_decoration.png");
    private static final int TEX_SIZE = 128;
    private static final int WHITE = 0xFFFFFFFF;

    private static final float SX = 0.09375F / 4.0F;
    private static final float SY = 0.1875F / 9.0F;
    private static final float SZ = 0.09375F / 4.0F;
    private static final float HEAD_TOP = 0.1875F;
    private static final float HEAD_FRONT = -0.09375F;

    private static final float BX = 0.15625F / 8.0F;
    private static final float BY = 0.25F / 12.0F;
    private static final float BZ = 0.125F / 5.5F;
    private static final float CHEST_TOP = 0.25F;

    private TCGolemAccessoryRenderers() {}

    @SubscribeEvent
    public static void onRegisterRenderers(RegisterGolemAccessoryRenderersEvent event) {
        event.register(TCGolemAccessories.FEZ, GolemAccessoryAnchor.HEAD, TCGolemAccessoryRenderers::fez);
        event.register(TCGolemAccessories.TOP_HAT, GolemAccessoryAnchor.HEAD, TCGolemAccessoryRenderers::topHat);
        event.register(TCGolemAccessories.GLASSES, GolemAccessoryAnchor.HEAD, TCGolemAccessoryRenderers::glasses);
        event.register(TCGolemAccessories.VISOR, GolemAccessoryAnchor.HEAD, TCGolemAccessoryRenderers::visor);
        event.register(TCGolemAccessories.BOWTIE, GolemAccessoryAnchor.BODY, TCGolemAccessoryRenderers::bowtie);
    }

    private static void fez(PoseStack poseStack, MultiBufferSource buffers, GolemAccessoryRenderContext context) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        BoxGeometry.box(
                poseStack.last(),
                buffer,
                -4.5F * SX,
                HEAD_TOP - 3.0F * SY,
                -4.5F * SZ,
                4.5F * SX,
                HEAD_TOP + 4.0F * SY,
                4.5F * SZ,
                0,
                94,
                9,
                7,
                9,
                TEX_SIZE,
                TEX_SIZE,
                WHITE,
                context.lightCoords(),
                false);
    }

    private static void topHat(PoseStack poseStack, MultiBufferSource buffers, GolemAccessoryRenderContext context) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        int light = context.lightCoords();
        BoxGeometry.box(
                pose,
                buffer,
                -4.5F * SX,
                HEAD_TOP - 3.0F * SY,
                -4.5F * SZ,
                4.5F * SX,
                HEAD_TOP + 6.0F * SY,
                4.5F * SZ,
                0,
                110,
                9,
                9,
                9,
                TEX_SIZE,
                TEX_SIZE,
                WHITE,
                light,
                false);
        BoxGeometry.box(
                pose,
                buffer,
                -6.5F * SX,
                HEAD_TOP - 3.0F * SY,
                -6.5F * SZ,
                6.5F * SX,
                HEAD_TOP - 2.0F * SY,
                6.5F * SZ,
                36,
                114,
                13,
                1,
                13,
                TEX_SIZE,
                TEX_SIZE,
                WHITE,
                light,
                false);
    }

    private static void glasses(PoseStack poseStack, MultiBufferSource buffers, GolemAccessoryRenderContext context) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        BoxGeometry.box(
                poseStack.last(),
                buffer,
                -4.5F * SX,
                HEAD_TOP - 7.0F * SY,
                -4.5F * SZ,
                4.5F * SX,
                HEAD_TOP - 3.0F * SY,
                4.5F * SZ,
                0,
                80,
                9,
                4,
                9,
                TEX_SIZE,
                TEX_SIZE,
                WHITE,
                context.lightCoords(),
                false);
    }

    private static void visor(PoseStack poseStack, MultiBufferSource buffers, GolemAccessoryRenderContext context) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        BoxGeometry.box(
                poseStack.last(),
                buffer,
                -5.0F * SX,
                HEAD_TOP - 8.0F * SY,
                HEAD_FRONT - 0.5F * SZ,
                5.0F * SX,
                HEAD_TOP - 3.0F * SY,
                HEAD_FRONT + 4.5F * SZ,
                0,
                70,
                10,
                5,
                5,
                TEX_SIZE,
                TEX_SIZE,
                WHITE,
                context.lightCoords(),
                false);
    }

    private static void bowtie(PoseStack poseStack, MultiBufferSource buffers, GolemAccessoryRenderContext context) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
        BoxGeometry.box(
                poseStack.last(),
                buffer,
                -8.5F * BX,
                CHEST_TOP - 4.0F * BY,
                -6.0F * BZ,
                8.5F * BX,
                CHEST_TOP,
                6.0F * BZ,
                0,
                0,
                17,
                4,
                12,
                TEX_SIZE,
                TEX_SIZE,
                WHITE,
                context.lightCoords(),
                false);
    }
}
