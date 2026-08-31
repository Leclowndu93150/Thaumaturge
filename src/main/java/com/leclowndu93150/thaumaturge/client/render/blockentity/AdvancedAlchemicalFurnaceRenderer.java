package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Renders the original TC4 Advanced Alchemical Furnace model around its modern controller. */
public final class AdvancedAlchemicalFurnaceRenderer
        implements BlockEntityRenderer<BlockEntityAdvancedAlchemicalFurnace> {
    public static final ModelResourceLocation BASE_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/advanced_alchemical_furnace_base"));
    public static final ModelResourceLocation BASE_ON_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/advanced_alchemical_furnace_base_on"));
    public static final ModelResourceLocation TANK_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/advanced_alchemical_furnace_tank"));
    public static final ModelResourceLocation TANK_ON_MODEL_ID =
            ModelResourceLocation.standalone(TCIds.rl("block/advanced_alchemical_furnace_tank_on"));

    private final RandomSource random = RandomSource.create();

    private static final RandomSource PREVIEW_RANDOM = RandomSource.create();

    public AdvancedAlchemicalFurnaceRenderer(BlockEntityRendererProvider.Context context) {}

    /** Renders the inactive complete furnace for inventory and recipe-viewer previews. */
    public static void renderPreview(
            BlockState state, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        renderModel(state, BASE_MODEL_ID, PREVIEW_RANDOM, poseStack, buffers, light, overlay);
        for (int rotation = 0; rotation < 4; rotation++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * rotation));
            renderModel(state, TANK_MODEL_ID, PREVIEW_RANDOM, poseStack, buffers, light, overlay);
            poseStack.popPose();
        }
    }

    @Override
    public void render(
            BlockEntityAdvancedAlchemicalFurnace furnace,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (!furnace.assembled()) {
            return;
        }

        BlockState state = furnace.getBlockState();
        boolean hot = furnace.heat() > 100;
        boolean charged = !furnace.aspects().isEmpty();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        renderModel(state, hot ? BASE_ON_MODEL_ID : BASE_MODEL_ID, random, poseStack, buffers, light, overlay);
        ModelResourceLocation tank = charged ? TANK_ON_MODEL_ID : TANK_MODEL_ID;
        for (int rotation = 0; rotation < 4; rotation++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * rotation));
            renderModel(state, tank, random, poseStack, buffers, light, overlay);
            poseStack.popPose();
        }
        if (hot) {
            renderHeatVents(furnace.heat(), poseStack, buffers, light);
        }
        poseStack.popPose();
    }

    /** The original OBJ leaves these four sloped openings to the renderer for animated fire. */
    private static void renderHeatVents(int heat, PoseStack poseStack, MultiBufferSource buffers, int light) {
        TextureAtlasSprite fire = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ResourceLocation.withDefaultNamespace("block/fire_0"));
        VertexConsumer buffer = fire.wrap(buffers.getBuffer(Sheets.translucentCullBlockSheet()));
        float base = 1.0F - Math.min(1.0F, heat / (float) BlockEntityAdvancedAlchemicalFurnace.MAX_POWER);
        for (int rotation = 0; rotation < 4; rotation++) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 1.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(135.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.translate(-0.5F, 0.0F, -1.0F);
            heatQuad(buffer, poseStack.last(), fire, base, light);
            poseStack.popPose();
        }
    }

    private static void heatQuad(
            VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, float base, int light) {
        vertex(buffer, pose, 0.0F, 1.0F, sprite.getU0(), sprite.getV0(), light);
        vertex(buffer, pose, 1.0F, 1.0F, sprite.getU1(), sprite.getV0(), light);
        vertex(buffer, pose, 1.0F, base, sprite.getU1(), sprite.getV1(), light);
        vertex(buffer, pose, 0.0F, base, sprite.getU0(), sprite.getV1(), light);
    }

    private static void vertex(
            VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int light) {
        buffer.addVertex(pose, x, y, 0.0F)
                .setColor(0xFFFFFFFF)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static void renderModel(
            BlockState state,
            ModelResourceLocation modelId,
            RandomSource random,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelId);
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

    @Override
    public boolean shouldRenderOffScreen(BlockEntityAdvancedAlchemicalFurnace furnace) {
        return true;
    }
}
