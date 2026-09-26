package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCFXPipelines;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityDioptra;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class DioptraRenderer implements BlockEntityRenderer<BlockEntityDioptra, DioptraRenderState> {
    private static final Identifier GRID_TEXTURE = TCIds.rl("textures/misc/gridblock.png");
    private static final Identifier SIDE_TEXTURE = TCIds.rl("textures/entity/dioptra_side.png");
    private static final RenderPipeline PIPELINE = TCFXPipelines.additiveTextured(TCIds.rl("pipeline/dioptra"));
    private static final RenderType GRID_TYPE = RenderType.create("tc_dioptra_grid", RenderSetup.builder(PIPELINE).withTexture("Sampler0", GRID_TEXTURE).createRenderSetup());
    private static final RenderType SIDE_TYPE = RenderType.create("tc_dioptra_side", RenderSetup.builder(PIPELINE).withTexture("Sampler0", SIDE_TEXTURE).createRenderSetup());

    private static final int CELLS = 12;
    private static final float HEIGHT_SCALE = 96.0F;
    private static final float GRID_ALPHA = 0.9F;
    private static final float SKIRT_ALPHA = 0.9F;
    private static final float SIDE_ALPHA = 0.8F;
    private static final float RIPPLE_BASE = 200.0F;
    private static final float RIPPLE_AMPLITUDE = 15.0F;
    private static final float RIPPLE_NORMALIZER = 255.0F;

    public DioptraRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public DioptraRenderState createRenderState() {
        return new DioptraRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityDioptra dioptra, DioptraRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(dioptra, state, partialTicks, cameraPosition, breakProgress);
        System.arraycopy(dioptra.grid(), 0, state.grid, 0, state.grid.length);
        state.enabled = dioptra.getBlockState().getValue(BlockStateProperties.ENABLED);
        state.time = (Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount) + partialTicks;
    }

    @Override
    public void submit(DioptraRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float t = state.time;
        float rc = Mth.sin(t / 12.0F) * 0.05F + 0.85F;
        float gc = Mth.sin(t / 11.0F) * 0.05F + (state.enabled ? 0.9F : 0.45F);
        float bc = Mth.sin(t / 10.0F) * 0.05F + 0.95F;
        byte[] grid = state.grid;

        poseStack.pushPose();
        poseStack.translate(0.005F, 1.001F, 0.005F);
        poseStack.scale(0.99F, 1.0F, 0.99F);
        collector.submitCustomGeometry(poseStack, GRID_TYPE, (pose, buffer) -> {
            for (int a = 0; a < CELLS; a++) {
                for (int b = 0; b < CELLS; b++) {
                    double d3 = a - 6;
                    double d5 = b - 6;
                    double dis = Math.sqrt(d3 * d3 + d5 * d5);
                    float ripple = Mth.sin((float) ((t - dis * 10.0) / 8.0));
                    float lum = (RIPPLE_BASE + ripple * RIPPLE_AMPLITUDE) / RIPPLE_NORMALIZER;
                    int color = cellColor(rc, gc, bc, lum, GRID_ALPHA);
                    float x0 = a / (float) CELLS;
                    float x1 = (a + 1) / (float) CELLS;
                    float z0 = b / (float) CELLS;
                    float z1 = (b + 1) / (float) CELLS;
                    float h00 = grid[a + b * 13] / HEIGHT_SCALE;
                    float h10 = grid[a + 1 + b * 13] / HEIGHT_SCALE;
                    float h11 = grid[a + 1 + (b + 1) * 13] / HEIGHT_SCALE;
                    float h01 = grid[a + (b + 1) * 13] / HEIGHT_SCALE;
                    vertex(buffer, pose, x0, h01, z1, 0.0F, 0.0F, color);
                    vertex(buffer, pose, x1, h11, z1, 1.0F, 0.0F, color);
                    vertex(buffer, pose, x1, h10, z0, 1.0F, 1.0F, color);
                    vertex(buffer, pose, x0, h00, z0, 0.0F, 1.0F, color);
                    if (a == 0) {
                        int solid = cellColor(rc, gc, bc, lum, SKIRT_ALPHA);
                        int clear = cellColor(rc, gc, bc, lum, 0.0F);
                        vertex(buffer, pose, 0.0F, 0.0F, z1, 0.0F, 0.0F, clear);
                        vertex(buffer, pose, 0.0F, h01, z1, 1.0F, 0.0F, solid);
                        vertex(buffer, pose, 0.0F, h00, z0, 1.0F, 1.0F, solid);
                        vertex(buffer, pose, 0.0F, 0.0F, z0, 0.0F, 1.0F, clear);
                    }
                    if (a == CELLS - 1) {
                        int solid = cellColor(rc, gc, bc, lum, SKIRT_ALPHA);
                        int clear = cellColor(rc, gc, bc, lum, 0.0F);
                        vertex(buffer, pose, 1.0F, 0.0F, z0, 0.0F, 1.0F, clear);
                        vertex(buffer, pose, 1.0F, h10, z0, 1.0F, 1.0F, solid);
                        vertex(buffer, pose, 1.0F, h11, z1, 1.0F, 0.0F, solid);
                        vertex(buffer, pose, 1.0F, 0.0F, z1, 0.0F, 0.0F, clear);
                    }
                    if (b == 0) {
                        int solid = cellColor(rc, gc, bc, lum, SKIRT_ALPHA);
                        int clear = cellColor(rc, gc, bc, lum, 0.0F);
                        vertex(buffer, pose, x0, 0.0F, 0.0F, 0.0F, 1.0F, clear);
                        vertex(buffer, pose, x0, h00, 0.0F, 0.0F, 0.0F, solid);
                        vertex(buffer, pose, x1, h10, 0.0F, 1.0F, 0.0F, solid);
                        vertex(buffer, pose, x1, 0.0F, 0.0F, 1.0F, 1.0F, clear);
                    }
                    if (b == CELLS - 1) {
                        int solid = cellColor(rc, gc, bc, lum, SKIRT_ALPHA);
                        int clear = cellColor(rc, gc, bc, lum, 0.0F);
                        vertex(buffer, pose, x1, 0.0F, 1.0F, 1.0F, 1.0F, clear);
                        vertex(buffer, pose, x1, h11, 1.0F, 1.0F, 0.0F, solid);
                        vertex(buffer, pose, x0, h01, 1.0F, 0.0F, 0.0F, solid);
                        vertex(buffer, pose, x0, 0.0F, 1.0F, 0.0F, 1.0F, clear);
                    }
                }
            }
        });
        poseStack.popPose();

        int sideColor = ARGB.colorFromFloat(SIDE_ALPHA, Math.min(1.0F, rc), Math.min(1.0F, gc), Math.min(1.0F, bc));
        collector.submitCustomGeometry(poseStack, SIDE_TYPE, (pose, buffer) -> {
            sideQuad(buffer, pose, 0.0F, 0.0F, 1.0F, 0.0F, sideColor);
            sideQuad(buffer, pose, 1.0F, 1.0F, 0.0F, 1.0F, sideColor);
            sideQuad(buffer, pose, 0.0F, 1.0F, 0.0F, 0.0F, sideColor);
            sideQuad(buffer, pose, 1.0F, 0.0F, 1.0F, 1.0F, sideColor);
        });
    }

    private static void sideQuad(VertexConsumer buffer, PoseStack.Pose pose, float x0, float z0, float x1, float z1, int color) {
        float yBottom = 1.0F;
        float yTop = 2.0F;
        vertex(buffer, pose, x0, yBottom, z0, 0.0F, 0.0F, color);
        vertex(buffer, pose, x0, yTop, z0, 1.0F, 0.0F, color);
        vertex(buffer, pose, x1, yTop, z1, 1.0F, 1.0F, color);
        vertex(buffer, pose, x1, yBottom, z1, 0.0F, 1.0F, color);
    }

    private static int cellColor(float r, float g, float b, float lum, float alpha) {
        return ARGB.colorFromFloat(alpha, Math.min(1.0F, r * 0.8F * lum), Math.min(1.0F, g * lum), Math.min(1.0F, b * lum));
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityDioptra blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 0.3, pos.getY() - 0.3, pos.getZ() - 0.3, pos.getX() + 1.3, pos.getY() + 2.3, pos.getZ() + 1.3);
    }
}
