package com.leclowndu93150.thaumaturge.client.taint.overlay;

import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public interface TaintSkinSource {
    @Nullable
    TaintSkin resolve(Model model, ResourceLocation baseTexture, TaintSkinResources resources);
}
