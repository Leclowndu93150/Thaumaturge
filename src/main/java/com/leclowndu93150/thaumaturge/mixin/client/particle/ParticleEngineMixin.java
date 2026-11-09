package com.leclowndu93150.thaumaturge.mixin.client.particle;

import com.leclowndu93150.thaumaturge.client.particle.TTParticleLayers;
import com.leclowndu93150.thaumaturge.client.warding.WardClientHandler;
import com.leclowndu93150.thaumaturge.content.warding.ClientWardHolder;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @ModifyExpressionValue(
            method =
                    "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;keySet()Ljava/util/Set;"))
    private Set<ParticleRenderType> thaumaturge$orderNitorLayers(Set<ParticleRenderType> layers) {
        return TTParticleLayers.orderNitorLayers(layers);
    }

    @WrapOperation(
            method =
                    "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"))
    private void thaumaturge$blendNitorWithWorld(MeshData mesh, Operation<Void> draw, @Local ParticleRenderType layer) {
        if (!Minecraft.useShaderTransparency() || !TTParticleLayers.usesNitorAdditiveBlend(layer)) {
            draw.call(mesh);
            return;
        }

        // Fabulous composites its particle target with alpha, which dims the world behind additive particles.
        RenderStateShard.PARTICLES_TARGET.clearRenderState();
        try {
            draw.call(mesh);
        } finally {
            RenderStateShard.PARTICLES_TARGET.setupRenderState();
        }
    }

    @Inject(
            method =
                    "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At("RETURN"))
    private void thaumaturge$restoreDepthTest(
            LightTexture lightTexture,
            Camera camera,
            float partialTick,
            Frustum frustum,
            Predicate<ParticleRenderType> renderTypeFilter,
            CallbackInfo ci) {
        RenderSystem.enableDepthTest();
    }

    @Inject(method = "crack", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$wardHitEffect(BlockPos pos, Direction direction, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && ClientWardHolder.isWarded(pos)) {
            WardClientHandler.spawnHitEffect(level, pos, direction, null);
            ci.cancel();
        }
    }
}
