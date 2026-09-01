package com.leclowndu93150.thaumaturge.mixin.client.iris;

import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Iris's translucent-particle program uses a 0.1 alpha test. Thaumcraft nodes deliberately use alpha 0.1 for their
 * unrevealed aura and contain still softer texels in the revealed aspect layer, so the stock Iris cutoff erases those
 * parts. Shader packs may also replace the RenderType's blend mode after it has been configured.
 *
 * <p>This compatibility hook is texture-scoped to Thaumaturge's node atlas. It therefore leaves ordinary particles
 * and other shader-pack effects untouched.</p>
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.programs.ExtendedShader", remap = false)
public abstract class ExtendedShaderMixin {
    private static final float NODE_ALPHA_TEST = 0.0001F;

    @ModifyArg(
            method = "apply",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/irisshaders/iris/uniforms/CapturedRenderingState;setCurrentAlphaTest(F)V"),
            index = 0)
    private float thaumaturge$preserveNodeAlpha(float irisAlphaTest) {
        return IrisCompat.isNodeParticleShaderPass(this) ? Math.min(irisAlphaTest, NODE_ALPHA_TEST) : irisAlphaTest;
    }

    @Inject(method = "applyBlendModes", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$preserveNodeBlendMode(CallbackInfo ci) {
        if (IrisCompat.isNodeParticleShaderPass(this)) {
            ci.cancel();
        }
    }
}
