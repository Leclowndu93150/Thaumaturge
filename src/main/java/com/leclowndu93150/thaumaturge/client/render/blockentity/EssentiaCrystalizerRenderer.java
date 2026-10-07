package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.essentia.crystalizer.BlockEntityEssentiaCrystalizer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
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
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Animated crystalizer crystals. */
public final class EssentiaCrystalizerRenderer implements BlockEntityRenderer<BlockEntityEssentiaCrystalizer> {
    public static final ModelResourceLocation CRYSTAL_MODEL_ID =
            ModelResourceLocation.standalone(TTIds.rl("block/crystalizer_crystal"));

    private static final float PIXEL = 1.0F / 16.0F;
    private static final float CRADLE_TOP = 11.0F * PIXEL;
    private static final float SIDE_CRYSTAL_SCALE = 0.7F;

    private record CrystalPlacement(float x, float z, float turn, float scale) {}

    private static final List<CrystalPlacement> CRYSTALS = List.of(
            new CrystalPlacement(0.0F, 0.0F, 0.0F, 1.0F),
            new CrystalPlacement(-3.0F, -2.0F, 120.0F, SIDE_CRYSTAL_SCALE),
            new CrystalPlacement(2.0F, 3.0F, 240.0F, SIDE_CRYSTAL_SCALE));

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
        if (crystalizer.getLevel() == null || crystalizer.aspectKey() == null) {
            return;
        }
        float red = crystalizer.crystalRed;
        float green = crystalizer.crystalGreen;
        float blue = crystalizer.crystalBlue;
        float spin = crystalizer.rotation + crystalizer.rotationSpeed * partialTick;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        BlockFacingPose.downBased(poseStack, crystalizer.getBlockState().getValue(BlockStateProperties.FACING));
        poseStack.translate(0.0F, CRADLE_TOP - 0.5F, 0.0F);
        for (CrystalPlacement crystal : CRYSTALS) {
            poseStack.pushPose();
            poseStack.translate(crystal.x() * PIXEL, 0.0F, crystal.z() * PIXEL);
            poseStack.mulPose(Axis.YP.rotationDegrees(crystal.turn() + spin));
            poseStack.scale(crystal.scale(), crystal.scale(), crystal.scale());
            poseStack.translate(-0.5F, 0.0F, -0.5F);
            int crystalLight = LightTexture.pack(Math.max(LightTexture.block(light), 12), LightTexture.sky(light));
            renderCrystal(crystalizer.getBlockState(), poseStack, buffers, crystalLight, overlay, red, green, blue);
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
            VertexConsumer consumer = buffers.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false));
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
            boolean tinted = quad.isTinted() && quad.getTintIndex() == 0;
            consumer.putBulkData(
                    poseStack.last(),
                    quad,
                    tinted ? red : 1.0F,
                    tinted ? green : 1.0F,
                    tinted ? blue : 1.0F,
                    1.0F,
                    light,
                    overlay);
        }
    }
}
