package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.content.aura.BlockEntityRechargePedestal;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class RechargePedestalRenderer implements BlockEntityRenderer<BlockEntityRechargePedestal> {
    private static final float LINE_SPEED = -0.02F;
    private static final float LINE_WIDTH = 0.15F;
    private static final float PEDESTAL_TOP = 1.0F;

    private final PedestalRenderer<BlockEntityRechargePedestal> delegate;

    public RechargePedestalRenderer(BlockEntityRendererProvider.Context context, float itemScale) {
        this.delegate = new PedestalRenderer<>(context, itemScale);
    }

    @Override
    public void render(
            BlockEntityRechargePedestal pedestal,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        delegate.render(pedestal, partialTick, poseStack, buffers, light, overlay);
        BlockPos drainPos = pedestal.getDrainPos();
        if (drainPos == null || pedestal.getLevel() == null) {
            return;
        }
        Vec3 nodeCenter = Vec3.atCenterOf(drainPos);
        Vec3 top = Vec3.atBottomCenterOf(pedestal.getBlockPos()).add(0.0, PEDESTAL_TOP, 0.0);
        Vec3 from = nodeCenter.subtract(top);
        float time = FloatyLineRenderer.time(pedestal.getLevel().getGameTime(), partialTick);
        int color = pedestal.getDrainColor();
        Vec3 origin = Vec3.atLowerCornerOf(pedestal.getBlockPos()).add(0.5, PEDESTAL_TOP, 0.5);
        LateWorldRenderQueue.enqueueBlockEntity(
                origin,
                (latePose, lateBuffers) -> FloatyLineRenderer.draw(
                        latePose, lateBuffers, from, time, color, LINE_SPEED, 1.0F, LINE_WIDTH));
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityRechargePedestal be) {
        return true;
    }
}
