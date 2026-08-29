package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.essentia.reservoir.BlockEntityEssentiaReservoir;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Renders the TC4 reservoir's aspect-cycling mixed essentia volume. */
public final class EssentiaReservoirRenderer implements BlockEntityRenderer<BlockEntityEssentiaReservoir> {
    public EssentiaReservoirRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityEssentiaReservoir reservoir,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        int amount = reservoir.getStoredAmount();
        if (amount <= 0) return;

        // JarRenderer's fluid prism is widened and lifted to the TC4 reservoir's 3/16..13/16 bounds.
        poseStack.pushPose();
        poseStack.translate(0.5F, 2.0F / 16.0F, 0.5F);
        poseStack.scale(1.25F, 1.0F, 1.25F);
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        int color = 0xE6000000 | (reservoir.displayColor() & 0x00FFFFFF);
        JarRenderer.submitFluid(
                amount,
                BlockEntityEssentiaReservoir.CAPACITY,
                color,
                Math.max(light, 200),
                JarRenderer.ANIMATED_GLOW_MATERIAL.sprite(),
                poseStack,
                buffers);
        poseStack.popPose();
    }
}
