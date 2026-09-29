package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.render.TCShaders;
import com.leclowndu93150.thaumaturge.content.focus.BlockEntityHole;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
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
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class HoleRenderer implements BlockEntityRenderer<BlockEntityHole> {
    private static final double FACE_THICKNESS = 1.0 / 1024.0;
    private static final double SURFACE_INSET = 0.01;
    private static final RenderType SURFACE = RenderType.create(
            "tc_hole_surface",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(TCShaders::ender))
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
        VoxelShape shape = hole.originalBlockState().getShape(level, pos);
        if (shape.isEmpty()) {
            return;
        }
        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        VertexConsumer buffer = buffers.getBuffer(SURFACE);
        PoseStack.Pose pose = poseStack.last();
        for (Direction direction : Direction.values()) {
            neighborPos.setWithOffset(pos, direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            boolean hideBoundary = neighbor.is(TCBlocks.HOLE.get()) || neighbor.isSolidRender(level, neighborPos);
            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                if (!hideBoundary || !onBoundary(direction, minX, minY, minZ, maxX, maxY, maxZ)) {
                    renderFace(shape, pose, buffer, direction, minX, minY, minZ, maxX, maxY, maxZ);
                }
            });
        }
    }

    private static boolean onBoundary(
            Direction direction, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return switch (direction) {
            case DOWN -> minY <= 0.0;
            case UP -> maxY >= 1.0;
            case NORTH -> minZ <= 0.0;
            case SOUTH -> maxZ >= 1.0;
            case WEST -> minX <= 0.0;
            case EAST -> maxX >= 1.0;
        };
    }

    private static void renderFace(
            VoxelShape shape,
            PoseStack.Pose pose,
            VertexConsumer buffer,
            Direction direction,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ) {
        VoxelShape face =
                switch (direction) {
                    case DOWN -> Shapes.box(minX, minY - FACE_THICKNESS, minZ, maxX, minY, maxZ);
                    case UP -> Shapes.box(minX, maxY, minZ, maxX, maxY + FACE_THICKNESS, maxZ);
                    case NORTH -> Shapes.box(minX, minY, minZ - FACE_THICKNESS, maxX, maxY, minZ);
                    case SOUTH -> Shapes.box(minX, minY, maxZ, maxX, maxY, maxZ + FACE_THICKNESS);
                    case WEST -> Shapes.box(minX - FACE_THICKNESS, minY, minZ, minX, maxY, maxZ);
                    case EAST -> Shapes.box(maxX, minY, minZ, maxX + FACE_THICKNESS, maxY, maxZ);
                };
        Shapes.join(face, shape, BooleanOp.ONLY_FIRST).forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            float a = (float) x1;
            float b = (float) y1;
            float c = (float) z1;
            float d = (float) x2;
            float e = (float) y2;
            float f = (float) z2;
            switch (direction) {
                case DOWN, UP -> {
                    float y = (float) ((direction == Direction.UP ? maxY - SURFACE_INSET : minY + SURFACE_INSET));
                    quad(pose, buffer, a, y, c, d, y, c, d, y, f, a, y, f);
                }
                case NORTH, SOUTH -> {
                    float z = (float) ((direction == Direction.SOUTH ? maxZ - SURFACE_INSET : minZ + SURFACE_INSET));
                    quad(pose, buffer, a, b, z, a, e, z, d, e, z, d, b, z);
                }
                case WEST, EAST -> {
                    float x = (float) ((direction == Direction.EAST ? maxX - SURFACE_INSET : minX + SURFACE_INSET));
                    quad(pose, buffer, x, b, c, x, e, c, x, e, f, x, b, f);
                }
            }
        });
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
