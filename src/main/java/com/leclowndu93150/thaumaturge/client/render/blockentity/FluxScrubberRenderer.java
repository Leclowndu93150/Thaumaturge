package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.device.fluxscrubber.BlockEntityFluxScrubber;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Animates the separate Tip group of the original TC4 obelisk-cap scrubber model. */
public final class FluxScrubberRenderer implements BlockEntityRenderer<BlockEntityFluxScrubber> {
    public static final ModelResourceLocation TIP_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/flux_scrubber_tip"));

    private final RandomSource random = RandomSource.create();

    public FluxScrubberRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityFluxScrubber scrubber,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BlockState state = scrubber.getBlockState();
        poseStack.pushPose();
        EssentiaCrystalizerRenderer.orientLegacy(poseStack, state.getValue(BlockStateProperties.FACING));
        float q = (Minecraft.getInstance().player == null
                ? partialTick + scrubber.animationOffset()
                : Minecraft.getInstance().player.tickCount + partialTick + scrubber.animationOffset());
        float bob = (float) Math.sin(q / 8.0F) * 0.075F + 0.075F;
        poseStack.translate(0.0F, 0.0F, -bob);
        renderTip(state, poseStack, buffers, light, overlay);
        poseStack.popPose();
    }

    private void renderTip(BlockState state, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(TIP_MODEL_ID);
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        for (RenderType renderType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            renderer.renderModel(
                    poseStack.last(),
                    buffers.getBuffer(renderType),
                    state,
                    model,
                    1.0F,
                    1.0F,
                    1.0F,
                    light,
                    overlay,
                    ModelData.EMPTY,
                    renderType);
        }
    }
}
