package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.crystalizer.BlockEntityEssentiaCrystalizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Faithful four-crystal spinner from the TC4 crystalizer renderer. */
public final class EssentiaCrystalizerRenderer implements BlockEntityRenderer<BlockEntityEssentiaCrystalizer> {
    public static final ModelResourceLocation CRYSTAL_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/crystalizer_crystal"));

    private final RandomSource random = RandomSource.create();

    public EssentiaCrystalizerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityEssentiaCrystalizer crystalizer,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (crystalizer.getLevel() == null || crystalizer.aspectKey() == null) return;
        float red = crystalizer.crystalRed;
        float green = crystalizer.crystalGreen;
        float blue = crystalizer.crystalBlue;
        float spin = crystalizer.rotation + crystalizer.rotationSpeed * partialTick;

        poseStack.pushPose();
        orientLegacy(poseStack, crystalizer.getBlockState().getValue(BlockStateProperties.FACING));
        for (int q = 0; q < 4; q++) {
            poseStack.pushPose();
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * q));
            poseStack.translate(0.34F, 0.0F, 1.2125F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
            renderCrystal(
                    crystalizer.getBlockState(), poseStack, buffers, Math.max(light, 200), overlay, red, green, blue);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void renderCrystal(
            BlockState state,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay,
            float red,
            float green,
            float blue) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(CRYSTAL_MODEL_ID);
        for (RenderType renderType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            VertexConsumer consumer = buffers.getBuffer(renderType);
            for (Direction direction : Direction.values()) {
                random.setSeed(42L);
                renderQuads(
                        poseStack,
                        consumer,
                        model.getQuads(state, direction, random, ModelData.EMPTY, renderType),
                        red,
                        green,
                        blue,
                        light,
                        overlay);
            }
            random.setSeed(42L);
            renderQuads(
                    poseStack,
                    consumer,
                    model.getQuads(state, null, random, ModelData.EMPTY, renderType),
                    red,
                    green,
                    blue,
                    light,
                    overlay);
        }
    }

    private static void renderQuads(
            PoseStack poseStack,
            VertexConsumer consumer,
            Iterable<BakedQuad> quads,
            float red,
            float green,
            float blue,
            int light,
            int overlay) {
        for (BakedQuad quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, red, green, blue, 1.0F, light, overlay);
        }
    }

    static void orientLegacy(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        switch (facing) {
            case DOWN -> poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
            case NORTH -> {}
        }
        poseStack.translate(0.0F, 0.0F, -0.5F);
    }
}
