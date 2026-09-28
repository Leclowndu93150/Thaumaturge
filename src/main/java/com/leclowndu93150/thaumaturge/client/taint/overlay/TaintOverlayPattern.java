package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.mojang.blaze3d.platform.NativeImage;

public interface TaintOverlayPattern {
    void paint(NativeImage image, ModelUvLayout layout, long seed);
}
