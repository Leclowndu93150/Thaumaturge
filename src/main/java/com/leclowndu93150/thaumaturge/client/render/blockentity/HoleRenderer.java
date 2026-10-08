package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.render.TTShaders;
import com.leclowndu93150.thaumaturge.content.spell.block.BlockEntityHole;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class HoleRenderer implements BlockEntityRenderer<BlockEntityHole> {
    private static final float SURFACE_INSET = 0.001F;
    private static final RenderType SURFACE = RenderType.create(
            "tt_hole_surface",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(TTShaders::ender))
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            TheEndPortalRenderer.END_PORTAL_LOCATION, false, false))
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    public HoleRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityHole hole,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        Level level = hole.getLevel();
        if (level == null) {
            return;
        }
        BlockPos pos = hole.getBlockPos();
        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        VertexConsumer buffer = null;
        PoseStack.Pose pose = poseStack.last();
        for (Direction direction : Direction.values()) {
            neighborPos.setWithOffset(pos, direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.is(TTBlocks.HOLE.get()) || !neighbor.isSolidRender(level, neighborPos)) {
                continue;
            }
            if (buffer == null) {
                buffer = buffers.getBuffer(SURFACE);
            }
            renderWall(pose, buffer, direction);
        }
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

    private static void quad(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float x4,
            float y4,
            float z4) {
        buffer.addVertex(pose.pose(), x1, y1, z1);
        buffer.addVertex(pose.pose(), x2, y2, z2);
        buffer.addVertex(pose.pose(), x3, y3, z3);
        buffer.addVertex(pose.pose(), x4, y4, z4);
    }
}
