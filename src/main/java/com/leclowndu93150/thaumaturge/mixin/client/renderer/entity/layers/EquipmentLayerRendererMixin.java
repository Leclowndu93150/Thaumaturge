package com.leclowndu93150.thaumaturge.mixin.client.renderer.entity.layers;

import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimSpriteKey;
import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimSpriteSource;
import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimSprites;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin implements ArmorTrimSpriteSource {
    @Unique
    private Function<ArmorTrimSpriteKey, Optional<TextureAtlasSprite>> thaumaturge$armorTrimSprites;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void thaumaturge$bindArmorTrimSprites(EquipmentAssetManager equipmentAssets, TextureAtlas armorTrimAtlas, CallbackInfo ci) {
        thaumaturge$armorTrimSprites = Util.memoize(key -> ArmorTrimSprites.find(armorTrimAtlas, key));
    }

    @ModifyExpressionValue(method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
    private Object thaumaturge$armorTrimSprite(Object sprite, @Local ArmorTrim trim, @Local(argsOnly = true) ResourceKey<EquipmentAsset> equipmentAssetId) {
        return thaumaturge$armorTrimSprite(new ArmorTrimSpriteKey(trim, equipmentAssetId), (TextureAtlasSprite) sprite);
    }

    @Override
    public TextureAtlasSprite thaumaturge$armorTrimSprite(ArmorTrimSpriteKey key, TextureAtlasSprite fallback) {
        return thaumaturge$armorTrimSprites.apply(key).orElse(fallback);
    }
}
