package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityDioptra;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class DioptraRenderer implements BlockEntityRenderer<BlockEntityDioptra, DioptraRenderState> {
    private static final Identifier SURFACE_TEXTURE = TTIds.rl("textures/misc/gridblock.png");
    private static final Identifier WALL_TEXTURE = TTIds.rl("textures/misc/white.png");
    private static final int LAST = BlockEntityDioptra.GRID_SIZE - 1;
    private static final float CELL = 1.0F / LAST;
    private static final float CENTER = LAST / 2.0F;
    private static final float BASE_LIFT = 0.015F;
    private static final float BASE_Y = 1.0F + BASE_LIFT;
    private static final float PEAK_HEIGHT = 2.0F / 3.0F;
    private static final float WALL_HEIGHT = 1.0F;
    private static final float SURFACE_ALPHA = 0.55F;
    private static final float SKIRT_TOP_ALPHA = 0.35F;
    private static final float WALL_BASE_ALPHA = 0.12F;
    private static final float WALL_TINT = 0.8F;
    private static final float DRIFT_AMPLITUDE = 0.12F;
    private static final float RED_DRIFT_RATE = 0.031F;
    private static final float GREEN_DRIFT_RATE = 0.023F;
    private static final float BLUE_DRIFT_RATE = 0.017F;
    private static final float GREEN_DRIFT_PHASE = 2.1F;
    private static final float BLUE_DRIFT_PHASE = 4.2F;
    private static final float VIS_RED = 0.78F;
    private static final float VIS_GREEN = 0.94F;
    private static final float VIS_BLUE = 1.0F;
    private static final float FLUX_RED = 0.62F;
    private static final float FLUX_GREEN = 0.28F;
    private static final float FLUX_BLUE = 0.95F;
    private static final float RIPPLE_FLOOR = 0.6F;
    private static final float RIPPLE_DEPTH = 0.4F;
    private static final float RIPPLE_WAVENUMBER = 0.9F;
    private static final float RIPPLE_SPEED = 0.12F;
    private static final float HEIGHT_GLOW = 0.35F;
    private static final double BOUNDS_HEIGHT = BASE_Y + WALL_HEIGHT;

    public DioptraRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public DioptraRenderState createRenderState() {
        return new DioptraRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityDioptra dioptra, DioptraRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(dioptra, state, partialTicks, cameraPosition, breakProgress);
        dioptra.copyGrid(state.grid);
        state.enabled = dioptra.getBlockState().getValue(BlockStateProperties.ENABLED);
        Level level = dioptra.getLevel();
        state.time = (level == null ? 0L : level.getGameTime()) + partialTicks;
    }

    @Override
    public void submit(DioptraRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Palette palette = Palette.of(state.enabled, state.time);
        float[] fills = fills(state.grid);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.dioptra(SURFACE_TEXTURE), (pose, buffer) -> {
            Matrix4fc matrix = pose.pose();
            writeSurface(buffer, matrix, fills, palette);
            writeSkirts(buffer, matrix, fills, palette);
        });
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.dioptra(WALL_TEXTURE), (pose, buffer) -> writeWalls(buffer, pose.pose(), palette));
    }

    private static void writeSurface(VertexConsumer buffer, Matrix4fc matrix, float[] fills, Palette palette) {
        for (int row = 0; row < LAST; row++) {
            for (int column = 0; column < LAST; column++) {
                surfacePoint(buffer, matrix, fills, palette, column, row, 0.0F, 0.0F);
                surfacePoint(buffer, matrix, fills, palette, column, row + 1, 0.0F, 1.0F);
                surfacePoint(buffer, matrix, fills, palette, column + 1, row + 1, 1.0F, 1.0F);
                surfacePoint(buffer, matrix, fills, palette, column + 1, row, 1.0F, 0.0F);
            }
        }
    }

    private static void surfacePoint(VertexConsumer buffer, Matrix4fc matrix, float[] fills, Palette palette, int column, int row, float u, float v) {
        float fill = fill(fills, column, row);
        float brightness = palette.ripple(column, row) * (1.0F - HEIGHT_GLOW + HEIGHT_GLOW * fill);
        buffer.addVertex(matrix, column * CELL, BASE_Y + fill * PEAK_HEIGHT, row * CELL).setUv(u, v).setColor(palette.red * brightness, palette.green * brightness, palette.blue * brightness,
                SURFACE_ALPHA);
    }

    private static void writeSkirts(VertexConsumer buffer, Matrix4fc matrix, float[] fills, Palette palette) {
        for (int step = 0; step < LAST; step++) {
            skirt(buffer, matrix, fills, palette, step, 0, step + 1, 0);
            skirt(buffer, matrix, fills, palette, step, LAST, step + 1, LAST);
            skirt(buffer, matrix, fills, palette, 0, step, 0, step + 1);
            skirt(buffer, matrix, fills, palette, LAST, step, LAST, step + 1);
        }
    }

    private static void skirt(VertexConsumer buffer, Matrix4fc matrix, float[] fills, Palette palette, int fromColumn, int fromRow, int toColumn, int toRow) {
        skirtPoint(buffer, matrix, palette, fromColumn, fromRow, BASE_Y, 0.0F, 1.0F, 0.0F);
        skirtPoint(buffer, matrix, palette, fromColumn, fromRow, BASE_Y + fill(fills, fromColumn, fromRow) * PEAK_HEIGHT, SKIRT_TOP_ALPHA, 0.0F, 0.0F);
        skirtPoint(buffer, matrix, palette, toColumn, toRow, BASE_Y + fill(fills, toColumn, toRow) * PEAK_HEIGHT, SKIRT_TOP_ALPHA, 0.0F, 1.0F);
        skirtPoint(buffer, matrix, palette, toColumn, toRow, BASE_Y, 0.0F, 1.0F, 1.0F);
    }

    private static void skirtPoint(VertexConsumer buffer, Matrix4fc matrix, Palette palette, int column, int row, float y, float alpha, float u, float v) {
        float brightness = palette.ripple(column, row);
        buffer.addVertex(matrix, column * CELL, y, row * CELL).setUv(u, v).setColor(palette.red * brightness, palette.green * brightness, palette.blue * brightness, alpha);
    }

    private static void writeWalls(VertexConsumer buffer, Matrix4fc matrix, Palette palette) {
        wall(buffer, matrix, palette, 0.0F, 0.0F, 1.0F, 0.0F);
        wall(buffer, matrix, palette, 1.0F, 0.0F, 1.0F, 1.0F);
        wall(buffer, matrix, palette, 1.0F, 1.0F, 0.0F, 1.0F);
        wall(buffer, matrix, palette, 0.0F, 1.0F, 0.0F, 0.0F);
    }

    private static void wall(VertexConsumer buffer, Matrix4fc matrix, Palette palette, float x0, float z0, float x1, float z1) {
        float red = palette.red * WALL_TINT;
        float green = palette.green * WALL_TINT;
        float blue = palette.blue * WALL_TINT;
        float top = BASE_Y + WALL_HEIGHT;
        buffer.addVertex(matrix, x0, BASE_Y, z0).setUv(0.0F, 1.0F).setColor(red, green, blue, WALL_BASE_ALPHA);
        buffer.addVertex(matrix, x0, top, z0).setUv(0.0F, 0.0F).setColor(red, green, blue, 0.0F);
        buffer.addVertex(matrix, x1, top, z1).setUv(1.0F, 0.0F).setColor(red, green, blue, 0.0F);
        buffer.addVertex(matrix, x1, BASE_Y, z1).setUv(1.0F, 1.0F).setColor(red, green, blue, WALL_BASE_ALPHA);
    }

    private static float[] fills(byte[] grid) {
        float[] fills = new float[grid.length];
        for (int index = 0; index < grid.length; index++) {
            fills[index] = grid[index] / (float) BlockEntityDioptra.STEPS;
        }
        return fills;
    }

    private static float fill(float[] fills, int column, int row) {
        return fills[row * BlockEntityDioptra.GRID_SIZE + column];
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityDioptra dioptra) {
        BlockPos pos = dioptra.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + BOUNDS_HEIGHT, pos.getZ() + 1.0);
    }

    private record Palette(float red, float green, float blue, float time) {
        static Palette of(boolean vis, float time) {
            float red = drift(vis ? VIS_RED : FLUX_RED, time * RED_DRIFT_RATE);
            float green = drift(vis ? VIS_GREEN : FLUX_GREEN, time * GREEN_DRIFT_RATE + GREEN_DRIFT_PHASE);
            float blue = drift(vis ? VIS_BLUE : FLUX_BLUE, time * BLUE_DRIFT_RATE + BLUE_DRIFT_PHASE);
            return new Palette(red, green, blue, time);
        }

        private static float drift(float base, float phase) {
            return Mth.clamp(base + DRIFT_AMPLITUDE * Mth.sin(phase), 0.0F, 1.0F);
        }

        float ripple(int column, int row) {
            float distance = Mth.sqrt(Mth.square(column - CENTER) + Mth.square(row - CENTER));
            return RIPPLE_FLOOR + RIPPLE_DEPTH * (0.5F + 0.5F * Mth.sin(distance * RIPPLE_WAVENUMBER - time * RIPPLE_SPEED));
        }
    }
}
