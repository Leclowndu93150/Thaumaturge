package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchNothing;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EldritchNothingRenderer implements BlockEntityRenderer<BlockEntityEldritchNothing, EldritchNothingRenderState> {
    private static final float INSET = 0.01F;

    public EldritchNothingRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public EldritchNothingRenderState createRenderState() {
        return new EldritchNothingRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEldritchNothing nothing, EldritchNothingRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(nothing, state, partialTicks, cameraPosition, breakProgress);
        state.anyExposed = false;
        Level level = nothing.getLevel();
        if (level == null) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            cursor.setWithOffset(nothing.getBlockPos(), dir);
            BlockState neighbor = level.getBlockState(cursor);
            boolean exposed = !neighbor.isSolidRender() && !neighbor.is(TCBlocks.ELDRITCH_NOTHING.get());
            state.exposed[dir.ordinal()] = exposed;
            state.anyExposed |= exposed;
        }
    }

    @Override
    public void submit(EldritchNothingRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.anyExposed) {
            return;
        }
        collector.submitCustomGeometry(poseStack, EldritchPortalSurface.SURFACE, (pose, buffer) -> {
            for (Direction dir : Direction.values()) {
                if (state.exposed[dir.ordinal()]) {
                    faceQuad(pose, buffer, state.blockPos, dir);
                }
            }
        });
    }

    private static void faceQuad(PoseStack.Pose pose, VertexConsumer buffer, BlockPos worldPos, Direction dir) {
        float near = INSET;
        float far = 1.0F - INSET;
        switch (dir) {
            case DOWN -> EldritchPortalSurface.quad(pose, buffer, worldPos, 0.0F, near, 0.0F, 1.0F, near, 0.0F, 1.0F, near, 1.0F, 0.0F, near, 1.0F);
            case UP -> EldritchPortalSurface.quad(pose, buffer, worldPos, 0.0F, far, 0.0F, 1.0F, far, 0.0F, 1.0F, far, 1.0F, 0.0F, far, 1.0F);
            case NORTH -> EldritchPortalSurface.quad(pose, buffer, worldPos, 0.0F, 0.0F, near, 0.0F, 1.0F, near, 1.0F, 1.0F, near, 1.0F, 0.0F, near);
            case SOUTH -> EldritchPortalSurface.quad(pose, buffer, worldPos, 0.0F, 0.0F, far, 0.0F, 1.0F, far, 1.0F, 1.0F, far, 1.0F, 0.0F, far);
            case WEST -> EldritchPortalSurface.quad(pose, buffer, worldPos, near, 0.0F, 0.0F, near, 1.0F, 0.0F, near, 1.0F, 1.0F, near, 0.0F, 1.0F);
            case EAST -> EldritchPortalSurface.quad(pose, buffer, worldPos, far, 0.0F, 0.0F, far, 1.0F, 0.0F, far, 1.0F, 1.0F, far, 0.0F, 1.0F);
        }
    }
}
