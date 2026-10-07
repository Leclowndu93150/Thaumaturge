package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class AdvancedAlchemicalFurnaceRenderer implements BlockEntityRenderer<BlockEntityAdvancedAlchemicalFurnace, AdvancedAlchemicalFurnaceRenderState> {
    private static final Identifier MODEL = TTIds.rl("models/mesh/advanced_alchemical_furnace.ttmesh");
    private static final RenderType BASE = RenderTypes.entityCutout(TTIds.rl("textures/block/advanced_alchemical_furnace.png"));
    private static final RenderType BASE_HOT = RenderTypes.entityCutout(TTIds.rl("textures/block/advanced_alchemical_furnace_on.png"));
    private static final RenderType TANK = RenderTypes.entityCutout(TTIds.rl("textures/block/advanced_alchemical_furnace_tank.png"));
    private static final RenderType TANK_FILLED = RenderTypes.entityCutout(TTIds.rl("textures/block/advanced_alchemical_furnace_tank_on.png"));
    private static final RenderType TANK_TRIM = RenderTypes.entityCutout(TTIds.rl("textures/block/metal_thaumium.png"));
    private static final SpriteId FIRE_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/fire_0"));
    private static final SpriteId GOO_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, TTIds.rl("block/flux_goo"));
    private static final SpriteId BACKING_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, TTIds.rl("block/base_metal"));
    private static final String PART_BASE = "Base";
    private static final String PART_TANK = "Tank";
    private static final String PART_TANK_TRIM = "TankTrim";
    private static final int WHITE = 0xFFFFFFFF;
    private static final int SIDES = 4;
    private static final float SIDE_ANGLE = 90.0F;
    private static final float UPRIGHT_ANGLE = 90.0F;
    private static final float HALF_TURN = 180.0F;
    private static final int FIRE_LIGHT = LightCoordsUtil.pack(14, 0);
    private static final int BACKING_LIGHT = LightCoordsUtil.pack(9, 0);
    private static final int GOO_LIGHT = LightCoordsUtil.pack(12, 0);
    private static final float VENT_HEIGHT = 1.0F;
    private static final float VENT_TILT = 135.0F;
    private static final float VENT_OFFSET_X = -0.5F;
    private static final float VENT_DEPTH = -1.0F;
    private static final float VENT_BACKING_OFFSET = 0.05F;
    private static final float SURFACE_X = 0.5F;
    private static final float SURFACE_Y = -0.5F;
    private static final float SURFACE_Z = 1.1F;
    private static final float WINDOW_LEFT_X = 0.85F;
    private static final float WINDOW_RIGHT_X = 1.15F;
    private static final float WINDOW_Y = 1.8F;
    private static final float WINDOW_Z = -1.4F;
    private static final float WINDOW_WIDTH = 0.3F;
    private static final float WINDOW_HEIGHT = 0.6F;
    private static final float WINDOW_FILL_OFFSET = 0.01F;
    private static final int BOUNDS_RADIUS = 1;
    private static final int BOUNDS_HEIGHT = 2;

    public AdvancedAlchemicalFurnaceRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public AdvancedAlchemicalFurnaceRenderState createRenderState() {
        return new AdvancedAlchemicalFurnaceRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityAdvancedAlchemicalFurnace furnace, AdvancedAlchemicalFurnaceRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(furnace, state, partialTicks, cameraPosition, breakProgress);
        state.assembled = furnace.isAssembled();
        state.heat = furnace.heat();
        state.stored = furnace.aspects().totalAmount();
    }

    @Override
    public void submit(AdvancedAlchemicalFurnaceRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.assembled) {
            return;
        }
        boolean hot = state.heat > BlockEntityAdvancedAlchemicalFurnace.HOT_HEAT;
        boolean filled = state.stored > 0;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.XN.rotationDegrees(UPRIGHT_ANGLE));
        submitMesh(hot, filled, poseStack, collector, state.lightCoords);
        if (filled) {
            submitStoredEssentia(state.stored, poseStack, collector);
        }
        if (hot) {
            submitHeatVents(state.heat, poseStack, collector);
        }
        poseStack.popPose();
    }

    public static void submitMesh(boolean hot, boolean filled, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        TTMesh mesh = GolemMeshes.get(MODEL);
        for (TTMeshPart part : mesh.parts()) {
            if (PART_BASE.equals(part.name())) {
                collector.submitCustomGeometry(poseStack, hot ? BASE_HOT : BASE, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, WHITE));
            } else if (PART_TANK.equals(part.name())) {
                submitTankPart(part, filled ? TANK_FILLED : TANK, poseStack, collector, light);
            } else if (PART_TANK_TRIM.equals(part.name())) {
                submitTankPart(part, TANK_TRIM, poseStack, collector, light);
            }
        }
    }

    private static void submitTankPart(TTMeshPart part, RenderType type, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        for (int side = 0; side < SIDES; side++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(SIDE_ANGLE * side));
            collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, WHITE));
            poseStack.popPose();
        }
    }

    private static void submitHeatVents(int heat, PoseStack poseStack, SubmitNodeCollector collector) {
        TextureAtlasSprite fire = Minecraft.getInstance().getAtlasManager().get(FIRE_SPRITE);
        TextureAtlasSprite backing = Minecraft.getInstance().getAtlasManager().get(BACKING_SPRITE);
        float flameBase = 1.0F - Math.min(1.0F, heat / (float) BlockEntityAdvancedAlchemicalFurnace.MAX_POWER);
        for (int side = 0; side < SIDES; side++) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, VENT_HEIGHT);
            poseStack.mulPose(Axis.ZP.rotationDegrees(SIDE_ANGLE * side));
            poseStack.mulPose(Axis.XP.rotationDegrees(VENT_TILT));
            poseStack.mulPose(Axis.ZP.rotationDegrees(HALF_TURN));
            poseStack.translate(VENT_OFFSET_X, 0.0F, VENT_DEPTH);
            submitPanel(poseStack, collector, Sheets.translucentBlockItemSheet(), fire, flameBase, FIRE_LIGHT);
            poseStack.translate(0.0F, 0.0F, VENT_BACKING_OFFSET);
            submitPanel(poseStack, collector, Sheets.cutoutBlockItemSheet(), backing, 0.0F, BACKING_LIGHT);
            poseStack.popPose();
        }
    }

    private static void submitStoredEssentia(int stored, PoseStack poseStack, SubmitNodeCollector collector) {
        TextureAtlasSprite goo = Minecraft.getInstance().getAtlasManager().get(GOO_SPRITE);
        TextureAtlasSprite backing = Minecraft.getInstance().getAtlasManager().get(BACKING_SPRITE);
        float fillBase = 1.0F - Math.min(1.0F, stored / (float) BlockEntityAdvancedAlchemicalFurnace.MAX_ESSENTIA);
        poseStack.pushPose();
        poseStack.translate(SURFACE_X, SURFACE_Y, SURFACE_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(HALF_TURN));
        submitPanel(poseStack, collector, Sheets.translucentBlockItemSheet(), goo, 0.0F, GOO_LIGHT);
        poseStack.popPose();
        for (int side = 0; side < SIDES; side++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(SIDE_ANGLE * side));
            poseStack.mulPose(Axis.XN.rotationDegrees(UPRIGHT_ANGLE));
            poseStack.translate(WINDOW_LEFT_X, -WINDOW_Y, WINDOW_Z);
            poseStack.scale(WINDOW_WIDTH, WINDOW_HEIGHT, 1.0F);
            submitWindow(poseStack, collector, goo, backing, fillBase, -WINDOW_FILL_OFFSET);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(Axis.ZN.rotationDegrees(SIDE_ANGLE * side));
            poseStack.mulPose(Axis.XP.rotationDegrees(UPRIGHT_ANGLE));
            poseStack.translate(WINDOW_RIGHT_X, WINDOW_Y, WINDOW_Z);
            poseStack.scale(-WINDOW_WIDTH, -WINDOW_HEIGHT, -1.0F);
            submitWindow(poseStack, collector, goo, backing, fillBase, WINDOW_FILL_OFFSET);
            poseStack.popPose();
        }
    }

    private static void submitWindow(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite goo, TextureAtlasSprite backing, float fillBase, float fillOffset) {
        submitPanel(poseStack, collector, Sheets.cutoutBlockItemSheet(), backing, 0.0F, BACKING_LIGHT);
        poseStack.translate(0.0F, 0.0F, fillOffset);
        submitPanel(poseStack, collector, Sheets.translucentBlockItemSheet(), goo, fillBase, GOO_LIGHT);
    }

    private static void submitPanel(PoseStack poseStack, SubmitNodeCollector collector, RenderType type, TextureAtlasSprite sprite, float bottom, int light) {
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            VertexConsumer wrapped = sprite.wrap(buffer);
            panelVertex(wrapped, pose, 0.0F, 1.0F, 0.0F, 0.0F, light, 1.0F);
            panelVertex(wrapped, pose, 1.0F, 1.0F, 1.0F, 0.0F, light, 1.0F);
            panelVertex(wrapped, pose, 1.0F, bottom, 1.0F, 1.0F, light, 1.0F);
            panelVertex(wrapped, pose, 0.0F, bottom, 0.0F, 1.0F, light, 1.0F);
            panelVertex(wrapped, pose, 0.0F, 1.0F, 0.0F, 0.0F, light, -1.0F);
            panelVertex(wrapped, pose, 0.0F, bottom, 0.0F, 1.0F, light, -1.0F);
            panelVertex(wrapped, pose, 1.0F, bottom, 1.0F, 1.0F, light, -1.0F);
            panelVertex(wrapped, pose, 1.0F, 1.0F, 1.0F, 0.0F, light, -1.0F);
        });
    }

    private static void panelVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int light, float normalZ) {
        buffer.addVertex(pose, x, y, 0.0F).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, normalZ).setColor(WHITE);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityAdvancedAlchemicalFurnace furnace) {
        BlockPos pos = furnace.getBlockPos();
        return new AABB(pos.getX() - BOUNDS_RADIUS, pos.getY(), pos.getZ() - BOUNDS_RADIUS, pos.getX() + BOUNDS_RADIUS + 1, pos.getY() + BOUNDS_HEIGHT, pos.getZ() + BOUNDS_RADIUS + 1);
    }
}
