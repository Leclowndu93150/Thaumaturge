package com.leclowndu93150.thaumaturge.client.screen.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPreviewRenderer extends PictureInPictureRenderer<BlockPreviewRenderState> {

    public BlockPreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<BlockPreviewRenderState> getRenderStateClass() {
        return BlockPreviewRenderState.class;
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected void renderToTexture(BlockPreviewRenderState state, PoseStack poseStack) {

        float scale = 1 * state.zoom() * state.previewScale();
        poseStack.scale(scale, scale, 1);
        poseStack.translate(-state.centerX(), -state.centerY(), 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.rotX()));
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotY()));

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : state.blocks().keySet()) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        float mbCenterX = (minX + maxX + 1) / 2.0F;
        float mbCenterY = (minY + maxY + 1) / 2.0F;
        float mbCenterZ = (minZ + maxZ + 1) / 2.0F;
        poseStack.translate(-mbCenterX, -mbCenterY, -mbCenterZ);

        PreviewBlockGetter blockGetter = new PreviewBlockGetter(state.blocks());
        RandomSource random = RandomSource.create();
        QuadInstance quadInstance = new QuadInstance();
        BlockStateModelSet modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();

        for (Map.Entry<BlockPos, BlockState> block : state.blocks().entrySet()) {
            BlockPos blockPos = block.getKey();
            BlockState bs = block.getValue();
            BlockStateModel model = modelSet.get(bs);
            poseStack.pushPose();
            poseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

            random.setSeed(42L);
            List<BlockStateModelPart> parts = new ArrayList<>();
            model.collectParts(blockGetter, blockPos, bs, random, parts);

            for (BlockStateModelPart part : parts) {
                renderPartWithFaceCulling(poseStack, bs, part, blockGetter, blockPos, quadInstance);
            }

            poseStack.popPose();
        }
    }

    private void renderPartWithFaceCulling(PoseStack poseStack, BlockState state, BlockStateModelPart part, PreviewBlockGetter blockGetter, BlockPos pos, QuadInstance quadInstance) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = blockGetter.getBlockState(adjacentPos);

            if (state.skipRendering(adjacentState, direction)) {
                continue;
            }

            for (BakedQuad quad : part.getQuads(direction)) {
                quadInstance.setColor(-1);
                quadInstance.setLightCoords(15728880);
                VertexConsumer buffer = this.bufferSource.getBuffer(getRenderTypeForLayer(quad.materialInfo().layer()));
                buffer.putBakedQuad(poseStack.last(), quad, quadInstance);
            }
        }

        for (BakedQuad quad : part.getQuads(null)) {
            quadInstance.setColor(-1);
            quadInstance.setLightCoords(15728880);
            VertexConsumer buffer = this.bufferSource.getBuffer(getRenderTypeForLayer(quad.materialInfo().layer()));
            buffer.putBakedQuad(poseStack.last(), quad, quadInstance);
        }
    }

    private static RenderType getRenderTypeForLayer(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
    }

    @Override
    protected String getTextureLabel() {
        return "chisel block preview";
    }
}
