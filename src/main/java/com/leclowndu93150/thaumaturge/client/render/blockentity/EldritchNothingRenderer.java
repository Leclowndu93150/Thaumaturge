package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchNothing;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class EldritchNothingRenderer implements BlockEntityRenderer<BlockEntityEldritchNothing> {
    private static final float INSET = 0.01F;

    public EldritchNothingRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityEldritchNothing nothing,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        Level level = nothing.getLevel();
        if (level == null) {
            return;
        }
        VertexConsumer buffer = buffers.getBuffer(EldritchPortalSurface.surface());
        PoseStack.Pose pose = poseStack.last();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            cursor.setWithOffset(nothing.getBlockPos(), dir);
            BlockState neighbor = level.getBlockState(cursor);
            if (!neighbor.isSolidRender(level, cursor) && !neighbor.is(TCBlocks.ELDRITCH_NOTHING.get())) {
                faceQuad(pose, buffer, nothing.getBlockPos(), dir);
            }
        }
    }

    private static void faceQuad(PoseStack.Pose pose, VertexConsumer buffer, BlockPos worldPos, Direction dir) {
        float near = INSET;
        float far = 1.0F - INSET;
        switch (dir) {
            case DOWN ->
                EldritchPortalSurface.quad(
                        pose, buffer, worldPos, 0.0F, near, 0.0F, 1.0F, near, 0.0F, 1.0F, near, 1.0F, 0.0F, near, 1.0F);
            case UP ->
                EldritchPortalSurface.quad(
                        pose, buffer, worldPos, 0.0F, far, 0.0F, 1.0F, far, 0.0F, 1.0F, far, 1.0F, 0.0F, far, 1.0F);
            case NORTH ->
                EldritchPortalSurface.quad(
                        pose, buffer, worldPos, 0.0F, 0.0F, near, 0.0F, 1.0F, near, 1.0F, 1.0F, near, 1.0F, 0.0F, near);
            case SOUTH ->
                EldritchPortalSurface.quad(
                        pose, buffer, worldPos, 0.0F, 0.0F, far, 0.0F, 1.0F, far, 1.0F, 1.0F, far, 1.0F, 0.0F, far);
            case WEST ->
                EldritchPortalSurface.quad(
                        pose, buffer, worldPos, near, 0.0F, 0.0F, near, 1.0F, 0.0F, near, 1.0F, 1.0F, near, 0.0F, 1.0F);
            case EAST ->
                EldritchPortalSurface.quad(
                        pose, buffer, worldPos, far, 0.0F, 0.0F, far, 1.0F, 0.0F, far, 1.0F, 1.0F, far, 0.0F, 1.0F);
        }
    }
}
