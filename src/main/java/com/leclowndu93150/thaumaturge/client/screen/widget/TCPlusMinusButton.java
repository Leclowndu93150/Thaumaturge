package com.leclowndu93150.thaumaturge.client.screen.widget;

import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import net.minecraft.network.chat.Component;

public final class TCPlusMinusButton extends TCImageButton {
    public static final int SIZE = 10;
    private static final int U_MINUS = 0;
    private static final int U_PLUS = 10;
    private static final int V = 0;
    private static final int ATLAS = 256;

    private TCPlusMinusButton(int x, int y, boolean minus, Component message, Runnable onPress) {
        super(x, y, SIZE, SIZE, TCScreenTextures.GUI_BASE, minus ? U_MINUS : U_PLUS, V, SIZE, SIZE, ATLAS, ATLAS, message, onPress);
    }

    public static TCPlusMinusButton minus(int x, int y, Component message, Runnable onPress) {
        return new TCPlusMinusButton(x, y, true, message, onPress);
    }

    public static TCPlusMinusButton plus(int x, int y, Component message, Runnable onPress) {
        return new TCPlusMinusButton(x, y, false, message, onPress);
    }
}
