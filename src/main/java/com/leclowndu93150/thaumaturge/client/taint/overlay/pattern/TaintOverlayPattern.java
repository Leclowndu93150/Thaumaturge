package com.leclowndu93150.thaumaturge.client.taint.overlay.pattern;

import com.mojang.blaze3d.platform.NativeImage;

public interface TaintOverlayPattern {
    NativeImage paint(TaintCoverage coverage, TexturePixels veinFill, long seed);
}
