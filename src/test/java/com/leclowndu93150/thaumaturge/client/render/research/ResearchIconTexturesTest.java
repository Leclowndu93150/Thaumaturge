package com.leclowndu93150.thaumaturge.client.render.research;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.FastColor.ABGR32;
import org.junit.jupiter.api.Test;

class ResearchIconTexturesTest {
    @Test
    void removesOnlyConnectedDarkBackgroundAndPreservesEnclosedDetails() {
        int background = ABGR32.color(255, 10, 11, 14);
        int purple = ABGR32.color(255, 190, 50, 120);
        try (NativeImage image = new NativeImage(5, 5, true)) {
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) image.setPixelRGBA(x, y, background);
            }
            for (int y = 1; y < 4; y++) {
                for (int x = 1; x < 4; x++) image.setPixelRGBA(x, y, purple);
            }
            image.setPixelRGBA(2, 2, background);

            ResearchIconTextures.clearOpaqueBackground(image);

            assertEquals(background & 0x00FFFFFF, image.getPixelRGBA(0, 0));
            assertEquals(purple, image.getPixelRGBA(1, 1));
            assertEquals(background, image.getPixelRGBA(2, 2));
        }
    }

    @Test
    void resourcePackTransparencyIsNotChanged() {
        try (NativeImage image = new NativeImage(2, 2, true)) {
            int black = ABGR32.color(255, 0, 0, 0);
            int translucent = ABGR32.color(128, 10, 20, 30);
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 2; x++) image.setPixelRGBA(x, y, black);
            }
            image.setPixelRGBA(1, 1, translucent);

            ResearchIconTextures.clearOpaqueBackground(image);

            assertEquals(black, image.getPixelRGBA(0, 0));
            assertEquals(translucent, image.getPixelRGBA(1, 1));
        }
    }
}
