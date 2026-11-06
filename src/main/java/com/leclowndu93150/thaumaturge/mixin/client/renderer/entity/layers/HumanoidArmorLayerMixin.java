package com.leclowndu93150.thaumaturge.mixin.client.renderer.entity.layers;

import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimColors;
import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimSpriteKey;
import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimSpriteSource;
import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimSprites;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin implements ArmorTrimSpriteSource {
    @Shadow
    @Final
    private TextureAtlas armorTrimAtlas;

    @Unique
    private static final String RENDER_TRIM =
            "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V";

    @Unique
    private Function<ArmorTrimSpriteKey, Optional<TextureAtlasSprite>> thaumaturge$armorTrimSprites;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void thaumaturge$bindArmorTrimSprites(CallbackInfo ci) {
        TextureAtlas atlas = armorTrimAtlas;
        thaumaturge$armorTrimSprites = Util.memoize(key -> ArmorTrimSprites.find(atlas, key));
    }

    @ModifyExpressionValue(
            method = RENDER_TRIM,
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/texture/TextureAtlas;getSprite(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private TextureAtlasSprite thaumaturge$armorTrimSprite(
            TextureAtlasSprite sprite,
            @Local(argsOnly = true) Holder<ArmorMaterial> armorMaterial,
            @Local(argsOnly = true) ArmorTrim trim) {
        return thaumaturge$armorTrimSprite(new ArmorTrimSpriteKey(trim, armorMaterial), sprite);
    }

    @WrapOperation(
            method = RENDER_TRIM,
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void thaumaturge$armorTrimTint(
            Model model,
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            Operation<Void> original,
            @Local(argsOnly = true) ArmorTrim trim) {
        int color = ArmorTrimColors.of(trim.material());
        if (color == -1) {
            original.call(model, poseStack, buffer, packedLight, packedOverlay);
        } else {
            model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }

    @Override
    public TextureAtlasSprite thaumaturge$armorTrimSprite(ArmorTrimSpriteKey key, TextureAtlasSprite fallback) {
        return thaumaturge$armorTrimSprites.apply(key).orElse(fallback);
    }
}
