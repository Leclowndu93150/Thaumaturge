package com.leclowndu93150.thaumaturge.mixin.world.item.crafting;

import com.leclowndu93150.thaumaturge.content.equipment.trim.CrystalTrimMaterials;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmithingTrimRecipe.class)
public class SmithingTrimRecipeMixin {
    @ModifyExpressionValue(method = "applyTrim", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;", ordinal = 0))
    private static @Nullable Object thaumaturge$crystalTrimMaterial(@Nullable Object material, @Local(argsOnly = true, ordinal = 1) ItemStack materialItem) {
        return material != null ? material : CrystalTrimMaterials.of(materialItem);
    }
}
