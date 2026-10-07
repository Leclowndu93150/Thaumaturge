package com.leclowndu93150.thaumaturge.client.screen.widget;

import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import net.minecraft.network.chat.Component;

public final class TTScrollButton extends TTImageButton {
    public static final int SIZE = 10;
    private static final int U_LEFT = 20;
    private static final int U_RIGHT = 30;
    private static final int U_VERTICAL = 67;
    private static final int V_UP = 0;
    private static final int V_DOWN = 10;
    private static final int V_HORIZONTAL = 0;
    private static final int ATLAS = 256;

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private TTScrollButton(int x, int y, int u, int v, Component message, Runnable onPress) {
        super(x, y, SIZE, SIZE, TTScreenTextures.GUI_BASE, u, v, SIZE, SIZE, ATLAS, ATLAS, message, onPress);
    }

    public static TTScrollButton of(int x, int y, Direction direction, Component message, Runnable onPress) {
        return switch (direction) {
            case LEFT -> new TTScrollButton(x, y, U_LEFT, V_HORIZONTAL, message, onPress);
            case RIGHT -> new TTScrollButton(x, y, U_RIGHT, V_HORIZONTAL, message, onPress);
            case UP -> new TTScrollButton(x, y, U_VERTICAL, V_UP, message, onPress);
            case DOWN -> new TTScrollButton(x, y, U_VERTICAL, V_DOWN, message, onPress);
        };
    }
}
