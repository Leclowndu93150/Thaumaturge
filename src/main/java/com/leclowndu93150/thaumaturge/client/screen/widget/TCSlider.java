package com.leclowndu93150.thaumaturge.client.screen.widget;

import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class TCSlider extends AbstractWidget {
    private static final int HANDLE_SIZE = 8;
    private static final int HANDLE_U = 20;
    private static final int HANDLE_V = 20;
    private static final int TRACK_LENGTH = 32;
    private static final int TRACK_THICKNESS = 4;
    private static final int TRACK_VERTICAL_U = 240;
    private static final int TRACK_HORIZONTAL_U = 208;
    private static final int TRACK_V = 176;
    private static final int TRACK_OFFSET = 2;
    private static final int ATLAS = 256;

    private final boolean vertical;
    private final Consumer<Float> onChange;
    private final float min;
    private float max;
    private float position;

    public TCSlider(int x, int y, int width, int height, boolean vertical, float min, float max, float value, Consumer<Float> onChange) {
        super(x, y, width, height, Component.empty());
        this.vertical = vertical;
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        this.position = fractionOf(value);
    }

    public float max() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
        this.position = 0.0F;
    }

    public float value() {
        return min + (max - min) * position;
    }

    public void setValue(float value) {
        position = fractionOf(value);
    }

    private float fractionOf(float value) {
        return max > min ? Mth.clamp((value - min) / (max - min), 0.0F, 1.0F) : 0.0F;
    }

    private void updateFromMouse(double mouseX, double mouseY) {
        float raw = vertical ? (float) (mouseY - (getY() + HANDLE_SIZE / 2.0)) / (height - HANDLE_SIZE) : (float) (mouseX - (getX() + HANDLE_SIZE / 2.0)) / (width - HANDLE_SIZE);
        position = Mth.clamp(raw, 0.0F, 1.0F);
        onChange.accept(value());
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.pose().pushMatrix();
        if (vertical) {
            graphics.pose().translate(getX() + TRACK_OFFSET, getY());
            graphics.pose().scale(1.0F, height / (float) TRACK_LENGTH);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, 0, 0, TRACK_VERTICAL_U, TRACK_V, TRACK_THICKNESS, TRACK_LENGTH, ATLAS, ATLAS);
        } else {
            graphics.pose().translate(getX(), getY() + TRACK_OFFSET);
            graphics.pose().scale(width / (float) TRACK_LENGTH, 1.0F);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, 0, 0, TRACK_HORIZONTAL_U, TRACK_V, TRACK_LENGTH, TRACK_THICKNESS, ATLAS, ATLAS);
        }
        graphics.pose().popMatrix();
        int handleX = vertical ? getX() : getX() + (int) (position * (width - HANDLE_SIZE));
        int handleY = vertical ? getY() + (int) (position * (height - HANDLE_SIZE)) : getY();
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, handleX, handleY, HANDLE_U, HANDLE_V, HANDLE_SIZE, HANDLE_SIZE, ATLAS, ATLAS);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        updateFromMouse(event.x(), event.y());
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        updateFromMouse(event.x(), event.y());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
