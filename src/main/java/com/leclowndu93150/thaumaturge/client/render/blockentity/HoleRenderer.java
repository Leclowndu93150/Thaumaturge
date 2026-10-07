package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.content.spell.block.BlockEntityHole;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class HoleRenderer implements BlockEntityRenderer<BlockEntityHole, HoleRenderState> {
    private static final float SURFACE_INSET = 0.001F;
    private static final RenderType SURFACE = RenderType.create("tc_hole_surface",
            RenderSetup.builder(TTRenderPipelines.HOLE_SURFACE).withTexture("Sampler0", AbstractEndPortalRenderer.END_PORTAL_LOCATION).createRenderSetup());

    public HoleRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public HoleRenderState createRenderState() {
        return new HoleRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityHole hole, HoleRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(hole, state, partialTicks, cameraPosition, breakProgress);
        state.anyWall = false;
        Level level = hole.getLevel();
        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            boolean wall = false;
            if (level != null) {
                neighborPos.setWithOffset(hole.getBlockPos(), direction);
                BlockState neighbor = level.getBlockState(neighborPos);
                wall = !neighbor.is(TTBlocks.HOLE.get()) && neighbor.isSolidRender();
            }
            state.walls[direction.ordinal()] = wall;
            state.anyWall |= wall;
        }
    }

    @Override
    public void submit(HoleRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.anyWall) {
            return;
        }
        collector.submitCustomGeometry(poseStack, SURFACE, (pose, buffer) -> {
            for (Direction direction : Direction.values()) {
                if (state.walls[direction.ordinal()]) {
                    renderWall(pose, buffer, direction);
                }
            }
        });
    }

    private static void renderWall(PoseStack.Pose pose, VertexConsumer buffer, Direction direction) {
        float near = SURFACE_INSET;
        float far = 1.0F - SURFACE_INSET;
        switch (direction) {
            case DOWN -> quad(pose, buffer, 0, near, 0, 1, near, 0, 1, near, 1, 0, near, 1);
            case UP -> quad(pose, buffer, 0, far, 0, 1, far, 0, 1, far, 1, 0, far, 1);
            case NORTH -> quad(pose, buffer, 0, 0, near, 0, 1, near, 1, 1, near, 1, 0, near);
            case SOUTH -> quad(pose, buffer, 0, 0, far, 0, 1, far, 1, 1, far, 1, 0, far);
            case WEST -> quad(pose, buffer, near, 0, 0, near, 1, 0, near, 1, 1, near, 0, 1);
            case EAST -> quad(pose, buffer, far, 0, 0, far, 1, 0, far, 1, 1, far, 0, 1);
        }
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        buffer.addVertex(pose.pose(), x1, y1, z1);
        buffer.addVertex(pose.pose(), x2, y2, z2);
        buffer.addVertex(pose.pose(), x3, y3, z3);
        buffer.addVertex(pose.pose(), x4, y4, z4);
    }
}
