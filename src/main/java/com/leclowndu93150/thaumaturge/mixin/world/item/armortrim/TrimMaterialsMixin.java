package com.leclowndu93150.thaumaturge.mixin.world.item.armortrim;

import com.leclowndu93150.thaumaturge.content.equipment.trim.CrystalTrimMaterials;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrimMaterials.class)
public class TrimMaterialsMixin {
    @Inject(method = "getFromIngredient", at = @At("HEAD"), cancellable = true)
    private static void thaumaturge$crystalTrimMaterial(
            HolderLookup.Provider registries,
            ItemStack ingredient,
            CallbackInfoReturnable<Optional<Holder.Reference<TrimMaterial>>> cir) {
        if (CrystalTrimMaterials.isCrystal(ingredient)) {
            cir.setReturnValue(CrystalTrimMaterials.find(registries, ingredient));
        }
    }
}
