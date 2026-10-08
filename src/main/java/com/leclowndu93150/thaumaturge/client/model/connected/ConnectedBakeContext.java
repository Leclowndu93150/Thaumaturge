package com.leclowndu93150.thaumaturge.client.model.connected;

import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.ResourceLocation;

public record ConnectedBakeContext(ModelBaker models, Function<Material, TextureAtlasSprite> sprites) {
    public TextureAtlasSprite sprite(ResourceLocation texture) {
        return sprites.apply(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
    }
}
