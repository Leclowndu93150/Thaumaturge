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
    private static final ResourceLocation SIDE_TEXTURE = TTIds.rl("textures/entity/dioptra_side.png");
    private static final RenderType SURFACE_TYPE = TTRenderTypes.srcAlphaAdditiveTextured(SURFACE_TEXTURE);
    private static final RenderType WALL_TYPE = TTRenderTypes.srcAlphaAdditiveTextured(SIDE_TEXTURE);

    private static final int LAST = BlockEntityDioptra.GRID_SIZE - 1;
    private static final float CELL = 1.0F / LAST;
    private static final float CENTER = LAST / 2.0F;
    private static final float PATCH_INSET = 0.005F;
    private static final float PATCH_SPAN = 0.99F;
    private static final float BASE_Y = 1.001F;
    private static final float PEAK_HEIGHT = 2.0F / 3.0F;
    private static final float WALL_BOTTOM = 1.0F;
    private static final float WALL_TOP = 2.0F;
    private static final float TOP_ALPHA = 0.9F;
    private static final float WALL_ALPHA = 0.8F;
    private static final float RED_PERIOD = 12.0F;
    private static final float GREEN_PERIOD = 11.0F;
    private static final float BLUE_PERIOD = 10.0F;
    private static final float COLOR_SWING = 0.05F;
    private static final float RED_BASE = 0.85F;
    private static final float GREEN_BASE_ENABLED = 0.9F;
    private static final float GREEN_BASE_DISABLED = 0.45F;
    private static final float BLUE_BASE = 0.95F;
    private static final float RED_CELL_FACTOR = 0.8F;
    private static final float WAVE_SPEED = 10.0F;
    private static final float WAVE_DIVISOR = 8.0F;
    private static final float LUMINANCE_BASE = 200.0F;
    private static final float LUMINANCE_SWING = 15.0F;
    private static final float LUMINANCE_MAX = 255.0F;

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
                float brightness = palette.ripple(column, row);
                surfacePoint(buffer, pose, grid, palette, column, row, brightness, 0.0F, 1.0F);
                surfacePoint(buffer, pose, grid, palette, column + 1, row, brightness, 1.0F, 1.0F);
                surfacePoint(buffer, pose, grid, palette, column + 1, row + 1, brightness, 1.0F, 0.0F);
                surfacePoint(buffer, pose, grid, palette, column, row + 1, brightness, 0.0F, 0.0F);
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
            float brightness,
            float u,
            float v) {
        float fill = fill(grid, column, row);
        vertex(
                buffer,
                pose,
                patch(column * CELL),
                BASE_Y + fill * PEAK_HEIGHT,
                patch(row * CELL),
                u,
                v,
                palette.red * RED_CELL_FACTOR * brightness,
                palette.green * brightness,
                palette.blue * brightness,
                TOP_ALPHA);
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
        int cellColumn = Math.min(fromColumn, toColumn);
        int cellRow = Math.min(fromRow, toRow);
        if (cellColumn == LAST) {
            cellColumn--;
        }
        if (cellRow == LAST) {
            cellRow--;
        }
        float brightness = palette.ripple(cellColumn, cellRow);
        boolean alongColumns = fromRow == toRow;
        float start = alongColumns ? 0.0F : 1.0F;
        float end = alongColumns ? 1.0F : 0.0F;
        skirtPoint(buffer, pose, palette, brightness, fromColumn, fromRow, BASE_Y, 0.0F, 0.0F, 1.0F);
        skirtPoint(
                buffer,
                pose,
                palette,
                brightness,
                fromColumn,
                fromRow,
                BASE_Y + fill(grid, fromColumn, fromRow) * PEAK_HEIGHT,
                TOP_ALPHA,
                start,
                start);
        skirtPoint(
                buffer,
                pose,
                palette,
                brightness,
                toColumn,
                toRow,
                BASE_Y + fill(grid, toColumn, toRow) * PEAK_HEIGHT,
                TOP_ALPHA,
                1.0F,
                0.0F);
        skirtPoint(buffer, pose, palette, brightness, toColumn, toRow, BASE_Y, 0.0F, end, end);
    }

    private static void skirtPoint(
            VertexConsumer buffer,
            PoseStack.Pose pose,
            Palette palette,
            float brightness,
            int column,
            int row,
            float y,
            float alpha,
            float u,
            float v) {
        vertex(
                buffer,
                pose,
                patch(column * CELL),
                y,
                patch(row * CELL),
                u,
                v,
                palette.red * RED_CELL_FACTOR * brightness,
                palette.green * brightness,
                palette.blue * brightness,
                alpha);
    }

    private static void writeWalls(VertexConsumer buffer, PoseStack.Pose pose, Palette palette) {
        wall(buffer, pose, palette, 0.0F, 0.0F, 1.0F, 0.0F);
        wall(buffer, pose, palette, 1.0F, 1.0F, 0.0F, 1.0F);
        wall(buffer, pose, palette, 0.0F, 1.0F, 0.0F, 0.0F);
        wall(buffer, pose, palette, 1.0F, 0.0F, 1.0F, 1.0F);
    }

    private static void wall(
            VertexConsumer buffer, PoseStack.Pose pose, Palette palette, float x0, float z0, float x1, float z1) {
        float red = palette.red;
        float green = palette.green;
        float blue = palette.blue;
        vertex(buffer, pose, x0, WALL_BOTTOM, z0, 0.0F, 0.0F, red, green, blue, WALL_ALPHA);
        vertex(buffer, pose, x0, WALL_TOP, z0, 1.0F, 0.0F, red, green, blue, WALL_ALPHA);
        vertex(buffer, pose, x1, WALL_TOP, z1, 1.0F, 1.0F, red, green, blue, WALL_ALPHA);
        vertex(buffer, pose, x1, WALL_BOTTOM, z1, 0.0F, 1.0F, red, green, blue, WALL_ALPHA);
    }

    private static float fill(byte[] grid, int column, int row) {
        return grid[row * BlockEntityDioptra.GRID_SIZE + column] / (float) BlockEntityDioptra.STEPS;
    }

    private static float patch(float coordinate) {
        return PATCH_INSET + PATCH_SPAN * coordinate;
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
                pos.getX() - 0.3,
                pos.getY() - 0.3,
                pos.getZ() - 0.3,
                pos.getX() + 1.3,
                pos.getY() + 2.3,
                pos.getZ() + 1.3);
    }

    private record Palette(float red, float green, float blue, float time) {
        static Palette of(boolean vis, float time) {
            float red = COLOR_SWING * Mth.sin(time / RED_PERIOD) + RED_BASE;
            float green = COLOR_SWING * Mth.sin(time / GREEN_PERIOD) + (vis ? GREEN_BASE_ENABLED : GREEN_BASE_DISABLED);
            float blue = COLOR_SWING * Mth.sin(time / BLUE_PERIOD) + BLUE_BASE;
            return new Palette(red, green, blue, time);
        }

        float ripple(int column, int row) {
            float distance = Mth.sqrt(Mth.square(column - CENTER) + Mth.square(row - CENTER));
            float ripple = Mth.sin((time - WAVE_SPEED * distance) / WAVE_DIVISOR);
            return (LUMINANCE_BASE + LUMINANCE_SWING * ripple) / LUMINANCE_MAX;
        }
    }
}
