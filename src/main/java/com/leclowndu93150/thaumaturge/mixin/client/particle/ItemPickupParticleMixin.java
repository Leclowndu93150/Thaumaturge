package com.leclowndu93150.thaumaturge.mixin.client.particle;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemPickupParticle.class)
public class ItemPickupParticleMixin {
    @WrapMethod(method = "render")
    private void thaumaturge$restoreParticleState(
            VertexConsumer buffer, Camera camera, float partialTick, Operation<Void> render) {
        int lightmap = RenderSystem.getShaderTexture(2);
        try {
            render.call(buffer, camera, partialTick);
        } finally {
            // Item batches clear the lightmap and can switch away from the Fabulous particles target.
            RenderSystem.setShaderTexture(2, lightmap);
            RenderStateShard.PARTICLES_TARGET.setupRenderState();
        }
    }
}
