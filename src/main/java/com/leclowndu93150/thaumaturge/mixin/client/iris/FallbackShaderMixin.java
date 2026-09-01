package com.leclowndu93150.thaumaturge.mixin.client.iris;

import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Preserves Thaumaturge node blending when Iris has to use its fallback particle shader. */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.programs.FallbackShader", remap = false)
public abstract class FallbackShaderMixin {
    @Redirect(
            method = "apply",
            at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/blending/BlendModeOverride;apply()V"),
            require = 0)
    private void thaumaturge$preserveNodeBlendMode(BlendModeOverride override) {
        if (!IrisCompat.isNodeTextureBound()) {
            override.apply();
        }
    }
}
