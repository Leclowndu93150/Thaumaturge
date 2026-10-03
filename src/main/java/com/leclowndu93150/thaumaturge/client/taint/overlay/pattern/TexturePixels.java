package com.leclowndu93150.thaumaturge.client.taint.overlay.pattern;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.FastColor;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;

public record TexturePixels(int width, int height, int[] argb) {
    private static final float MAX_CHANNEL = 255.0F;

    public static TexturePixels copyOf(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] argb = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                argb[y * width + x] = FastColor.ABGR32.fromArgb32(image.getPixelRGBA(x, y));
            }
        }
        return new TexturePixels(width, height, argb);
    }

    public int get(int x, int y) {
        return argb[y * width + x];
    }

    public int wrapped(int x, int y) {
        return get(Math.floorMod(x, width), Math.floorMod(y, height));
    }

    public float alphaAt(float u, float v) {
        int x = Mth.clamp((int) (u * width), 0, width - 1);
        int y = Mth.clamp((int) (v * height), 0, height - 1);
        return ARGB32.alpha(get(x, y)) / MAX_CHANNEL;
    }
}
