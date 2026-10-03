package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.leclowndu93150.thaumaturge.client.taint.overlay.pattern.ModelUvLayout;
import com.leclowndu93150.thaumaturge.client.taint.overlay.pattern.TaintCoverage;
import com.leclowndu93150.thaumaturge.client.taint.overlay.pattern.TaintOverlayPattern;
import com.leclowndu93150.thaumaturge.client.taint.overlay.pattern.TexturePixels;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public final class GeneratedTaintSkinSource implements TaintSkinSource {
    private static final int SCALE = 2;
    private static final long CLASS_SEED = 31L;
    private static final long WIDTH_SEED = 7L;

    private final TaintOverlayPattern pattern;

    public GeneratedTaintSkinSource(TaintOverlayPattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public @Nullable TaintSkin resolve(Model model, ResourceLocation baseTexture, TaintSkinResources resources) {
        TexturePixels veinFill = resources.pixels(TaintTextures.VEIN_FILL);
        if (veinFill == null) {
            return null;
        }
        ModelUvLayout layout = ModelUvLayout.of(model);
        TaintCoverage coverage = TaintCoverage.of(layout, resources.pixels(baseTexture), SCALE);
        if (coverage.area() == 0) {
            return null;
        }
        long seed = model.getClass().getName().hashCode() * CLASS_SEED + layout.width() * WIDTH_SEED + layout.height();
        return new TaintSkin(resources.register(pattern.paint(coverage, veinFill, seed)), false, null);
    }
}
