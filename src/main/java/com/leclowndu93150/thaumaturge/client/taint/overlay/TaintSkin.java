package com.leclowndu93150.thaumaturge.client.taint.overlay;

import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public record TaintSkin(
        ResourceLocation texture,
        boolean replacesBase,
        @Nullable ResourceLocation glow) {}
