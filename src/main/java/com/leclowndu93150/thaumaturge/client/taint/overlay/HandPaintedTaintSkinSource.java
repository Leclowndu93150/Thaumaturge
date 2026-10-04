package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public final class HandPaintedTaintSkinSource implements TaintSkinSource {
    private static final String TEXTURE_PREFIX = "textures/";
    private static final String PNG = ".png";

    @Override
    public @Nullable TaintSkin resolve(Model model, ResourceLocation baseTexture, TaintSkinResources resources) {
        String path = baseTexture.getPath();
        if (!path.endsWith(PNG)) {
            return null;
        }
        String relative = baseTexture.getNamespace() + "/"
                + (path.startsWith(TEXTURE_PREFIX) ? path.substring(TEXTURE_PREFIX.length()) : path);
        ResourceLocation skin = TCIds.rl(TaintTextures.HAND_PAINTED_ROOT + relative);
        if (!resources.exists(skin)) {
            return null;
        }
        ResourceLocation glow = TCIds.rl(TaintTextures.HAND_PAINTED_ROOT
                + relative.substring(0, relative.length() - PNG.length())
                + TaintTextures.GLOW_SUFFIX
                + PNG);
        return new TaintSkin(skin, true, resources.exists(glow) ? glow : null);
    }
}
