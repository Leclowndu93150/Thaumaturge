package com.leclowndu93150.thaumaturge.mixin.iris.pathways;

import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.irisshaders.iris.pathways.HandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandRenderer.class)
public abstract class HandRendererMixin {
    @Inject(method = "isHandTranslucent", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$translucentThaumometer(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(TTItems.THAUMOMETER.get())) {
            cir.setReturnValue(true);
        }
    }
}
