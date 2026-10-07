package com.leclowndu93150.thaumaturge.mixin.client.iris;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pathways.HandRenderer", remap = false)
public abstract class HandRendererMixin {
    @Inject(method = "isHandTranslucent", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$translucentLens(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(TTItems.THAUMOMETER.get())) {
            cir.setReturnValue(true);
        }
    }
}
