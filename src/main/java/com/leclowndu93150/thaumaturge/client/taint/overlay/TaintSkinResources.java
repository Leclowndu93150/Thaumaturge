package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.client.taint.overlay.pattern.TexturePixels;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

public final class TaintSkinResources {
    private final ResourceManager resourceManager;
    private final TextureManager textureManager;
    private final Map<ResourceLocation, Optional<TexturePixels>> pixels = new HashMap<>();
    private final List<ResourceLocation> registered = new ArrayList<>();

    public TaintSkinResources(ResourceManager resourceManager, TextureManager textureManager) {
        this.resourceManager = resourceManager;
        this.textureManager = textureManager;
    }

    public boolean exists(ResourceLocation id) {
        return resourceManager.getResource(id).isPresent();
    }

    public @Nullable TexturePixels pixels(ResourceLocation id) {
        return pixels.computeIfAbsent(id, this::read).orElse(null);
    }

    public ResourceLocation register(NativeImage image) {
        ResourceLocation id = TCIds.rl(TaintTextures.DYNAMIC_ROOT + registered.size());
        textureManager.register(id, new DynamicTexture(image));
        registered.add(id);
        return id;
    }

    public void release() {
        for (ResourceLocation id : registered) {
            textureManager.release(id);
        }
        registered.clear();
        pixels.clear();
    }

    private Optional<TexturePixels> read(ResourceLocation id) {
        Optional<Resource> resource = resourceManager.getResource(id);
        if (resource.isEmpty()) {
            return Optional.empty();
        }
        try (InputStream stream = resource.get().open();
                NativeImage image = NativeImage.read(stream)) {
            return Optional.of(TexturePixels.copyOf(image));
        } catch (IOException e) {
            Thaumaturge.LOGGER.error("Failed to read taint skin texture {}", id, e);
            return Optional.empty();
        }
    }
}
