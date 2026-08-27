package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeTransducer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class NodeTransducerRenderer implements BlockEntityRenderer<BlockEntityNodeTransducer> {
    public NodeTransducerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityNodeTransducer transducer,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        LocalPlayer player = Minecraft.getInstance().player;
        float ticks = player == null ? 0.0F : player.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        NodeStabilizerRenderer.submitTransducerParts(
                transducer.getCount(), transducer.getStatus(), ticks, poseStack, buffers, light);
        poseStack.popPose();
    }
}
