package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.content.aura.BlockEntityRechargePedestal;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class RechargePedestalRenderer implements BlockEntityRenderer<BlockEntityRechargePedestal, RechargePedestalRenderState> {
    private static final float LINE_SPEED = -0.02F;
    private static final float LINE_WIDTH = 0.15F;
    private static final float PEDESTAL_TOP = 1.0F;

    private final PedestalRenderer<BlockEntityRechargePedestal> delegate;

    public RechargePedestalRenderer(BlockEntityRendererProvider.Context context, float itemScale) {
        this.delegate = new PedestalRenderer<>(context, itemScale);
    }

    @Override
    public RechargePedestalRenderState createRenderState() {
        return new RechargePedestalRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityRechargePedestal pedestal, RechargePedestalRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        delegate.extractRenderState(pedestal, state, partialTicks, cameraPosition, breakProgress);
        state.draining = false;
        BlockPos drainPos = pedestal.getDrainPos();
        if (drainPos != null && pedestal.getLevel() != null) {
            Vec3 nodeCenter = Vec3.atCenterOf(drainPos);
            Vec3 top = Vec3.atBottomCenterOf(pedestal.getBlockPos()).add(0.0, PEDESTAL_TOP, 0.0);
            state.draining = true;
            state.drainFromX = nodeCenter.x - top.x;
            state.drainFromY = nodeCenter.y - top.y;
            state.drainFromZ = nodeCenter.z - top.z;
            state.drainColor = pedestal.getDrainColor();
            state.time = pedestal.getLevel().getGameTime();
            state.partial = partialTicks;
        }
    }

    @Override
    public void submit(RechargePedestalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        delegate.submit(state, poseStack, collector, camera);
        if (state.draining) {
            Vec3 from = new Vec3(state.drainFromX, state.drainFromY, state.drainFromZ);
            float time = FloatyLineRenderer.time(state.time, state.partial);
            int color = state.drainColor;
            Vec3 origin = Vec3.atLowerCornerOf(state.blockPos).add(0.5, PEDESTAL_TOP, 0.5);
            LateWorldRenderQueue.enqueue(origin, (latePose, buffers) -> FloatyLineRenderer.draw(latePose, buffers, from, time, color, LINE_SPEED, 1.0F, LINE_WIDTH));
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
