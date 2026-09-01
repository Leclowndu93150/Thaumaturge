package com.leclowndu93150.thaumaturge.mixin.client.iris;

import com.leclowndu93150.thaumaturge.client.render.TCShaders;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Iris blocks unrecognised vanilla/core ShaderInstances by default while a shader pack is active. Thaumaturge's rift
 * and occluding-effect shaders are intentional core shaders whose fragment programs are part of the effect itself, so
 * replacing them with a generic shader changes the effect rather than merely routing it through the shader pack.
 *
 * <p>Apply after Iris's ShaderInstance mixin (lower priority) and opt in only those two registered shader instances.
 * Shadow rendering stays skipped exactly as Iris expects for unknown shaders.</p>
 */
@Mixin(value = ShaderInstance.class, priority = 900)
public abstract class ShaderInstanceMixin {
    @Dynamic("Added to ShaderInstance by Iris's MixinShaderInstance")
    @Inject(method = "iris$shouldSkipThis", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void thaumaturge$allowCustomEffectShaders(CallbackInfoReturnable<Boolean> cir) {
        ShaderInstance shader = (ShaderInstance) (Object) this;
        if (!ShadowRenderer.ACTIVE && TCShaders.isIrisAllowedCustomEffectShader(shader)) {
            cir.setReturnValue(false);
        }
    }
}
