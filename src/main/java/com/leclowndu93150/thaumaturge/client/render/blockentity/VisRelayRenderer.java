package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class VisRelayRenderer implements BlockEntityRenderer<BlockEntityVisRelay, VisRelayRenderState> {
    private static final int BEAM_COLOR = 0xB08CFF;
    private static final float BEAM_SPEED = -0.01F;
    private static final float BEAM_WIDTH = 0.08F;
    private static final float CRYSTAL_HEIGHT = 0.55F;

    public VisRelayRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public VisRelayRenderState createRenderState() {
        return new VisRelayRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityVisRelay relay, VisRelayRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(relay, state, partialTicks, cameraPosition, breakProgress);
        state.beamTarget = null;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !relay.isLinked() || relay.parentPos() == null) {
            return;
        }
        Vec3 own = Vec3.atCenterOf(relay.getBlockPos()).add(0.0, CRYSTAL_HEIGHT - 0.5, 0.0);
        Vec3 target = Vec3.atCenterOf(relay.parentPos());
        state.beamTarget = target.subtract(own);
        state.ticks = player.tickCount + partialTicks;
        state.time = player.level().getGameTime();
    }

    @Override
    public void submit(VisRelayRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.beamTarget == null) {
            return;
        }
        Vec3 origin = Vec3.atCenterOf(state.blockPos).add(0.0, CRYSTAL_HEIGHT - 0.5, 0.0);
        Vec3 target = state.beamTarget;
        float time = FloatyLineRenderer.time(state.time, state.ticks % 1.0F);
        LateWorldRenderQueue.enqueue(origin, (latePose, buffers) -> FloatyLineRenderer.draw(latePose, buffers, target, time, BEAM_COLOR, BEAM_SPEED, 1.0F, BEAM_WIDTH));
    }
}
