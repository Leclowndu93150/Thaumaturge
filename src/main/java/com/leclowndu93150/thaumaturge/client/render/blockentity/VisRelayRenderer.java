package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class VisRelayRenderer implements BlockEntityRenderer<BlockEntityVisRelay> {
    private static final int BEAM_COLOR = 0x8469BF;
    private static final float BEAM_SPEED = -0.01F;
    private static final float BEAM_WIDTH = 0.08F;
    private static final float CRYSTAL_HEIGHT = 0.55F;

    public VisRelayRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityVisRelay relay,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BlockPos parent = relay.parentPos();
        if (parent == null || !relay.isLinked() || relay.getLevel() == null) {
            return;
        }
        Vec3 own = Vec3.atCenterOf(relay.getBlockPos()).add(0.0, CRYSTAL_HEIGHT - 0.5, 0.0);
        Vec3 from = Vec3.atCenterOf(parent).subtract(own);
        float time = FloatyLineRenderer.time(relay.getLevel().getGameTime(), partialTick);
        LateWorldRenderQueue.enqueue(
                own,
                (latePose, lateBuffers) -> FloatyLineRenderer.draw(
                        latePose, lateBuffers, from, time, BEAM_COLOR, BEAM_SPEED, 1.0F, BEAM_WIDTH));
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityVisRelay be) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityVisRelay relay) {
        AABB self = new AABB(relay.getBlockPos());
        BlockPos parent = relay.parentPos();
        return parent == null ? self : self.minmax(new AABB(parent));
    }
}
