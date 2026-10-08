package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityFallingTaint;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class FallingTaintRenderer extends EntityRenderer<EntityFallingTaint> {
    private final BlockRenderDispatcher dispatcher;

    public FallingTaintRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(
            EntityFallingTaint entity,
            float yaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        BlockState state = entity.getFallTile();
        if (state.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        long seed = state.getSeed(entity.origin());
        BakedModel model = dispatcher.getBlockModel(state);
        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);
        for (RenderType type : model.getRenderTypes(state, RandomSource.create(seed), ModelData.EMPTY)) {
            dispatcher
                    .getModelRenderer()
                    .tesselateBlock(
                            entity.level(),
                            model,
                            state,
                            pos,
                            poseStack,
                            buffers.getBuffer(RenderTypeHelper.getMovingBlockRenderType(type)),
                            false,
                            RandomSource.create(seed),
                            seed,
                            OverlayTexture.NO_OVERLAY,
                            ModelData.EMPTY,
                            type);
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFallingTaint entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
