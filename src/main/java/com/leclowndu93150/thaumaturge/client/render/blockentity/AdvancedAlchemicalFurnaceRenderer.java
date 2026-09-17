package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
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
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Renders the Advanced Alchemical Furnace model around its modern controller. */
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

    private static final int VENT_FIRE_LIGHT = LightTexture.pack(14, 0);
    private static final int VENT_BACKING_LIGHT = LightTexture.pack(9, 0);
    private static final int TANK_GOO_LIGHT = LightTexture.pack(12, 0);

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
        if (charged) {
            renderStoredEssentia(furnace.aspects().totalAmount(), poseStack, buffers);
        }
        if (hot) {
            renderHeatVents(furnace.heat(), poseStack, buffers);
        }
        poseStack.popPose();
    }

    /** The original OBJ leaves these four sloped openings to the renderer for animated fire. */
    private static void renderHeatVents(int heat, PoseStack poseStack, MultiBufferSource buffers) {
        TextureAtlasSprite fire = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ResourceLocation.withDefaultNamespace("block/fire_0"));
        TextureAtlasSprite backing = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(TCIds.rl("block/base_metal"));
        VertexConsumer fireBuffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
        VertexConsumer backingBuffer = buffers.getBuffer(Sheets.cutoutBlockSheet());
        float base = 1.0F - Math.min(1.0F, heat / (float) BlockEntityAdvancedAlchemicalFurnace.MAX_POWER);
        for (int rotation = 0; rotation < 4; rotation++) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 1.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(135.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.translate(-0.5F, 0.0F, -1.0F);
            spriteQuad(fireBuffer, poseStack.last(), fire, base, VENT_FIRE_LIGHT);
            spriteQuadBackface(fireBuffer, poseStack.last(), fire, base, VENT_FIRE_LIGHT);
            poseStack.translate(0.0F, 0.0F, 0.05F);
            spriteQuad(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            spriteQuadBackface(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            poseStack.popPose();
        }
    }

    /** Restores the TC4 liquid/window pass omitted by the initial model port. */
    private static void renderStoredEssentia(int stored, PoseStack poseStack, MultiBufferSource buffers) {
        TextureAtlasSprite goo = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(TCIds.rl("block/flux_goo"));
        TextureAtlasSprite backing = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(TCIds.rl("block/base_metal"));
        VertexConsumer gooBuffer = buffers.getBuffer(Sheets.translucentCullBlockSheet());
        VertexConsumer backingBuffer = buffers.getBuffer(Sheets.cutoutBlockSheet());
        float fillBase = 1.0F - Math.min(1.0F, stored / (float) BlockEntityAdvancedAlchemicalFurnace.MAX_ESSENTIA);

        // Liquid surface visible in the central opening.
        poseStack.pushPose();
        poseStack.translate(0.5F, -0.5F, 1.1F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        spriteQuad(gooBuffer, poseStack.last(), goo, 0.0F, TANK_GOO_LIGHT);
        spriteQuadBackface(gooBuffer, poseStack.last(), goo, 0.0F, TANK_GOO_LIGHT);
        poseStack.popPose();

        // Each corner tank has two window faces. The on-texture intentionally leaves
        // these slits transparent so this backing + animated fill can be seen through them.
        for (int rotation = 0; rotation < 4; rotation++) {
            poseStack.pushPose();

            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * rotation));
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            poseStack.translate(0.85F, -1.8F, -1.4F);
            poseStack.scale(0.3F, 0.6F, 1.0F);
            spriteQuad(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            spriteQuadBackface(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            poseStack.translate(0.0F, 0.0F, -0.01F);
            spriteQuad(gooBuffer, poseStack.last(), goo, fillBase, TANK_GOO_LIGHT);
            spriteQuadBackface(gooBuffer, poseStack.last(), goo, fillBase, TANK_GOO_LIGHT);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F * rotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.translate(1.15F, 1.8F, -1.4F);
            poseStack.scale(-0.3F, -0.6F, -1.0F);
            spriteQuad(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            spriteQuadBackface(backingBuffer, poseStack.last(), backing, 0.0F, VENT_BACKING_LIGHT);
            poseStack.translate(0.0F, 0.0F, 0.01F);
            spriteQuad(gooBuffer, poseStack.last(), goo, fillBase, TANK_GOO_LIGHT);
            spriteQuadBackface(gooBuffer, poseStack.last(), goo, fillBase, TANK_GOO_LIGHT);
            poseStack.popPose();

            poseStack.popPose();
        }
    }

    private static void spriteQuad(
            VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, float base, int light) {
        vertex(buffer, pose, 0.0F, 1.0F, sprite.getU0(), sprite.getV0(), light, 1.0F);
        vertex(buffer, pose, 1.0F, 1.0F, sprite.getU1(), sprite.getV0(), light, 1.0F);
        vertex(buffer, pose, 1.0F, base, sprite.getU1(), sprite.getV1(), light, 1.0F);
        vertex(buffer, pose, 0.0F, base, sprite.getU0(), sprite.getV1(), light, 1.0F);
    }

    private static void spriteQuadBackface(
            VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, float base, int light) {
        vertex(buffer, pose, 0.0F, 1.0F, sprite.getU0(), sprite.getV0(), light, -1.0F);
        vertex(buffer, pose, 0.0F, base, sprite.getU0(), sprite.getV1(), light, -1.0F);
        vertex(buffer, pose, 1.0F, base, sprite.getU1(), sprite.getV1(), light, -1.0F);
        vertex(buffer, pose, 1.0F, 1.0F, sprite.getU1(), sprite.getV0(), light, -1.0F);
    }

    private static void vertex(
            VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int light, float normalZ) {
        buffer.addVertex(pose, x, y, 0.0F)
                .setColor(0xFFFFFFFF)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, normalZ);
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
    public AABB getRenderBoundingBox(BlockEntityAdvancedAlchemicalFurnace furnace) {
        var pos = furnace.getBlockPos();
        return new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityAdvancedAlchemicalFurnace furnace) {
        return true;
    }
}
