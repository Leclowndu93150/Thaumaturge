package com.leclowndu93150.thaumaturge.client.screen.widget;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

public abstract class TCButton extends AbstractButton {
    protected static final int COLOR_WHITE = 0xFFFFFFFF;
    protected static final int IDLE_ALPHA = 0xE6;
    protected static final int HOVER_ALPHA = 0xFF;
    protected static final int DISABLED_ALPHA = 0x66;

    private final Runnable onPress;
    private int tintColor;
    private @Nullable Component description;

    protected TCButton(int x, int y, int width, int height, Component message, Runnable onPress) {
        super(x, y, width, height, message);
        this.tintColor = COLOR_WHITE;
        this.onPress = onPress;
    }

    public static int centerToTopLeftX(int centerX, int width) {
        return centerX - width / 2;
    }

    public static int centerToTopLeftY(int centerY, int height) {
        return centerY - height / 2;
    }

    public int tintColor() {
        return tintColor;
    }

    public void setTintColor(int color) {
        this.tintColor = color;
    }

    public @Nullable Component description() {
        return description;
    }

    public void setDescription(@Nullable Component description) {
        this.description = description;
        refreshTooltip();
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        refreshTooltip();
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (onPress != null) {
            onPress.run();
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        Component message = getMessage();
        return Component.translatable("gui.narrate.button", message == null ? Component.empty() : message);
    }

    protected int activeTintColor(int packedColor, boolean hovered, boolean enabled) {
        int alpha;
        float scale;
        if (!enabled) {
            alpha = DISABLED_ALPHA;
            scale = 0.5F;
        } else if (hovered) {
            alpha = HOVER_ALPHA;
            scale = 1.0F;
        } else {
            alpha = IDLE_ALPHA;
            scale = 0.9F;
        }
        int r = Math.min(255, Math.round(((packedColor >> 16) & 0xFF) * scale));
        int g = Math.min(255, Math.round(((packedColor >> 8) & 0xFF) * scale));
        int b = Math.min(255, Math.round((packedColor & 0xFF) * scale));
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    private void refreshTooltip() {
        Component message = getMessage();
        if (description == null && (message == null || message.getString().isEmpty())) {
            setTooltip((Tooltip) null);
            return;
        }
        MutableComponent combined = message == null ? Component.empty() : message.copy();
        if (description != null) {
            if (message != null && !message.getString().isEmpty()) {
                combined.append(Component.literal("\n"));
            }
            combined.append(description.copy().withStyle(Style.EMPTY.withItalic(true).withColor(0x5555FF)));
        }
        setTooltip(Tooltip.create(combined));
    }
}
