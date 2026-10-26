package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityDioptra;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public final class DioptraRenderer implements BlockEntityRenderer<BlockEntityDioptra> {
    private static final ResourceLocation SURFACE_TEXTURE = TTIds.rl("textures/misc/gridblock.png");
    private static final ResourceLocation WALL_TEXTURE = TTIds.rl("textures/misc/white.png");
    private static final RenderType SURFACE_TYPE = TTRenderTypes.additiveTextured(SURFACE_TEXTURE);
    private static final RenderType WALL_TYPE = TTRenderTypes.additiveTextured(WALL_TEXTURE);

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

    private final byte[] gridSnapshot = new byte[BlockEntityDioptra.GRID_LENGTH];

    public DioptraRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityDioptra dioptra,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        boolean enabled = dioptra.getBlockState().getValue(BlockStateProperties.ENABLED);
        Level level = dioptra.getLevel();
        float time = (level == null ? 0L : level.getGameTime()) + partialTick;
        Palette palette = Palette.of(enabled, time);
        dioptra.copyGrid(gridSnapshot);

        VertexConsumer surface = buffers.getBuffer(SURFACE_TYPE);
        PoseStack.Pose pose = poseStack.last();
        writeSurface(surface, pose, gridSnapshot, palette);
        writeSkirts(surface, pose, gridSnapshot, palette);

        VertexConsumer walls = buffers.getBuffer(WALL_TYPE);
        writeWalls(walls, pose, palette);
    }

    private static void writeSurface(VertexConsumer buffer, PoseStack.Pose pose, byte[] grid, Palette palette) {
        for (int row = 0; row < LAST; row++) {
            for (int column = 0; column < LAST; column++) {
                surfacePoint(buffer, pose, grid, palette, column, row, 0.0F, 0.0F);
                surfacePoint(buffer, pose, grid, palette, column, row + 1, 0.0F, 1.0F);
                surfacePoint(buffer, pose, grid, palette, column + 1, row + 1, 1.0F, 1.0F);
                surfacePoint(buffer, pose, grid, palette, column + 1, row, 1.0F, 0.0F);
            }
        }
    }

    private static void surfacePoint(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            byte[] grid,
            Palette palette,
            int column,
            int row,
            float u,
            float v) {
        float fill = fill(grid, column, row);
        float brightness = palette.ripple(column, row) * (1.0F - HEIGHT_GLOW + HEIGHT_GLOW * fill);
        vertex(
                buffer,
                pose,
                column * CELL,
                BASE_Y + fill * PEAK_HEIGHT,
                row * CELL,
                u,
                v,
                palette.red * brightness,
                palette.green * brightness,
                palette.blue * brightness,
                SURFACE_ALPHA);
    }

    private static void writeSkirts(VertexConsumer buffer, PoseStack.Pose pose, byte[] grid, Palette palette) {
        for (int step = 0; step < LAST; step++) {
            skirt(buffer, pose, grid, palette, step, 0, step + 1, 0);
            skirt(buffer, pose, grid, palette, step, LAST, step + 1, LAST);
            skirt(buffer, pose, grid, palette, 0, step, 0, step + 1);
            skirt(buffer, pose, grid, palette, LAST, step, LAST, step + 1);
        }
    }

    private static void skirt(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            byte[] grid,
            Palette palette,
            int fromColumn,
            int fromRow,
            int toColumn,
            int toRow) {
        skirtPoint(buffer, pose, palette, fromColumn, fromRow, BASE_Y, 0.0F, 0.0F, 1.0F);
        skirtPoint(
                buffer,
                pose,
                palette,
                fromColumn,
                fromRow,
                BASE_Y + fill(grid, fromColumn, fromRow) * PEAK_HEIGHT,
                SKIRT_TOP_ALPHA,
                0.0F,
                0.0F);
        skirtPoint(
                buffer,
                pose,
                palette,
                toColumn,
                toRow,
                BASE_Y + fill(grid, toColumn, toRow) * PEAK_HEIGHT,
                SKIRT_TOP_ALPHA,
                0.0F,
                1.0F);
        skirtPoint(buffer, pose, palette, toColumn, toRow, BASE_Y, 0.0F, 1.0F, 1.0F);
    }

    private static void skirtPoint(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            Palette palette,
            int column,
            int row,
            float y,
            float alpha,
            float u,
            float v) {
        float brightness = palette.ripple(column, row);
        vertex(
                buffer,
                pose,
                column * CELL,
                y,
                row * CELL,
                u,
                v,
                palette.red * brightness,
                palette.green * brightness,
                palette.blue * brightness,
                alpha);
    }

    private static void writeWalls(VertexConsumer buffer, PoseStack.Pose pose, Palette palette) {
        wall(buffer, pose, palette, 0.0F, 0.0F, 1.0F, 0.0F);
        wall(buffer, pose, palette, 1.0F, 0.0F, 1.0F, 1.0F);
        wall(buffer, pose, palette, 1.0F, 1.0F, 0.0F, 1.0F);
        wall(buffer, pose, palette, 0.0F, 1.0F, 0.0F, 0.0F);
    }

    private static void wall(
            VertexConsumer buffer, PoseStack.Pose pose, Palette palette, float x0, float z0, float x1, float z1) {
        float red = palette.red * WALL_TINT;
        float green = palette.green * WALL_TINT;
        float blue = palette.blue * WALL_TINT;
        float top = BASE_Y + WALL_HEIGHT;
        vertex(buffer, pose, x0, BASE_Y, z0, 0.0F, 1.0F, red, green, blue, WALL_BASE_ALPHA);
        vertex(buffer, pose, x0, top, z0, 0.0F, 0.0F, red, green, blue, 0.0F);
        vertex(buffer, pose, x1, top, z1, 1.0F, 0.0F, red, green, blue, 0.0F);
        vertex(buffer, pose, x1, BASE_Y, z1, 1.0F, 1.0F, red, green, blue, WALL_BASE_ALPHA);
    }

    private static float fill(byte[] grid, int column, int row) {
        return grid[row * BlockEntityDioptra.GRID_SIZE + column] / (float) BlockEntityDioptra.STEPS;
    }

    private static void vertex(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            float red,
            float green,
            float blue,
            float alpha) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(red, green, blue, alpha);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityDioptra blockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityDioptra dioptra) {
        BlockPos pos = dioptra.getBlockPos();
        return new AABB(
                pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + BOUNDS_HEIGHT, pos.getZ() + 1.0);
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
            return RIPPLE_FLOOR
                    + RIPPLE_DEPTH * (0.5F + 0.5F * Mth.sin(distance * RIPPLE_WAVENUMBER - time * RIPPLE_SPEED));
        }
    }
}
