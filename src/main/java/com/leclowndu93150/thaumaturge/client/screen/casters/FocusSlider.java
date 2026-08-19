package com.leclowndu93150.thaumaturge.client.screen.casters;

import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class FocusSlider extends AbstractWidget {
    private static final int HANDLE_SIZE = 8;
    private static final int HANDLE_U = 20;
    private static final int HANDLE_V = 20;
    private static final int ATLAS = 256;
    private static final int TRACK_INSET = 4;
    private static final int TRACK_FILL = 0x66000000;
    private static final int TRACK_EDGE = 0x33FFFFFF;

    private final float min;
    private final float max;
    private final boolean vertical;
    private final Consumer<Float> onChange;
    private float position;

    public FocusSlider(int x, int y, int w, int h, float min, float max, float value, boolean vertical, Consumer<Float> onChange) {
        super(x, y, w, h, Component.empty());
        this.min = min;
        this.max = max;
        this.vertical = vertical;
        this.onChange = onChange;
        this.position = max > min ? (value - min) / (max - min) : 0.0F;
    }

    public float value() {
        return min + (max - min) * position;
    }

    public void setValue(float value) {
        position = max > min ? Mth.clamp((value - min) / (max - min), 0.0F, 1.0F) : 0.0F;
    }

    private void updateFromMouse(double mouseX, double mouseY) {
        float raw = vertical ? (float) (mouseY - (getY() + TRACK_INSET)) / (height - HANDLE_SIZE) : (float) (mouseX - (getX() + TRACK_INSET)) / (width - HANDLE_SIZE);
        position = Mth.clamp(raw, 0.0F, 1.0F);
        onChange.accept(value());
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, TRACK_FILL);
        if (vertical) {
            graphics.fill(getX(), getY(), getX() + 1, getY() + height, TRACK_EDGE);
            graphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, TRACK_EDGE);
        } else {
            graphics.fill(getX(), getY(), getX() + width, getY() + 1, TRACK_EDGE);
            graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, TRACK_EDGE);
        }
        int hx = vertical ? getX() : getX() + (int) (position * (width - HANDLE_SIZE));
        int hy = vertical ? getY() + (int) (position * (height - HANDLE_SIZE)) : getY();
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.GUI_BASE, hx, hy, HANDLE_U, HANDLE_V, HANDLE_SIZE, HANDLE_SIZE, ATLAS, ATLAS);
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
