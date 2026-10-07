package com.leclowndu93150.thaumaturge.client.screen.widget;

import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import net.minecraft.network.chat.Component;

public final class TTPlusMinusButton extends TTImageButton {
    public static final int SIZE = 10;
    private static final int U_MINUS = 0;
    private static final int U_PLUS = 10;
    private static final int V = 0;
    private static final int ATLAS = 256;

    private TTPlusMinusButton(int x, int y, boolean minus, Component message, Runnable onPress) {
        super(x, y, SIZE, SIZE, TTScreenTextures.GUI_BASE, minus ? U_MINUS : U_PLUS, V, SIZE, SIZE, ATLAS, ATLAS, message, onPress);
    }

    public static TTPlusMinusButton minus(int x, int y, Component message, Runnable onPress) {
        return new TTPlusMinusButton(x, y, true, message, onPress);
    }

    public static TTPlusMinusButton plus(int x, int y, Component message, Runnable onPress) {
        return new TTPlusMinusButton(x, y, false, message, onPress);
    }
}
