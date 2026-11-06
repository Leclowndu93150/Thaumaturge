package com.leclowndu93150.thaumaturge.client.equipment;

import java.util.Optional;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public final class ArmorTrimSprites {
    private static final String TRIM_TEXTURE_ROOT = "trims/entity/";

    private ArmorTrimSprites() {}

    public static Optional<TextureAtlasSprite> find(TextureAtlas armorTrimAtlas, ArmorTrimSpriteKey key) {
        Identifier spriteId = key.trim().layerAssetId(TRIM_TEXTURE_ROOT + key.equipmentAsset().identifier().getPath(), key.equipmentAsset());
        TextureAtlasSprite sprite = armorTrimAtlas.getSprite(spriteId);
        return sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()) ? Optional.empty() : Optional.of(sprite);
    }
}
