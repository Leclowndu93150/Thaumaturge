package com.leclowndu93150.thaumaturge.client.screen.pip;

import java.util.Map;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record BlockPreviewRenderState(Map<BlockPos, BlockState> blocks, float rotX, float rotY, float zoom, float previewScale, float centerX, float centerY, int x0, int y0, int x1, int y1,
        @Nullable ScreenRectangle scissorArea) implements PictureInPictureRenderState {

    @Override
    public float scale() {
        return 1.0f;
    }

    @Override
    public Matrix3x2f pose() {
        return PictureInPictureRenderState.IDENTITY_POSE;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea);
    }
}
