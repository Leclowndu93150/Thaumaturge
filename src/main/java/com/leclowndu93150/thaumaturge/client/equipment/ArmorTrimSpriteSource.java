package com.leclowndu93150.thaumaturge.client.equipment;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface ArmorTrimSpriteSource {
    TextureAtlasSprite thaumaturge$armorTrimSprite(ArmorTrimSpriteKey key, TextureAtlasSprite fallback);
}
