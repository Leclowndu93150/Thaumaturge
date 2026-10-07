package com.leclowndu93150.thaumaturge.client.taint;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Vector3f;

public final class FluxGooClientExtensions implements IClientFluidTypeExtensions {
    private static final ResourceLocation STILL = TTIds.rl("block/flux_goo");
    private static final ResourceLocation FLOWING = TTIds.rl("block/flux_goo");
    private static final float FOG_R = 1.0F;
    private static final float FOG_G = 0.0F;
    private static final float FOG_B = 0.5F;
    private static final float FOG_START = 0.0F;
    private static final float FOG_END = 3.0F;

    @Override
    public ResourceLocation getStillTexture() {
        return STILL;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return FLOWING;
    }

    @Override
    public Vector3f modifyFogColor(
            Camera camera,
            float partialTick,
            ClientLevel level,
            int renderDistance,
            float darkenWorldAmount,
            Vector3f fluidFogColor) {
        return fluidFogColor.set(FOG_R, FOG_G, FOG_B);
    }

    @Override
    public void modifyFogRender(
            Camera camera,
            FogRenderer.FogMode mode,
            float renderDistance,
            float partialTick,
            float nearDistance,
            float farDistance,
            FogShape shape) {
        RenderSystem.setShaderFogStart(FOG_START);
        RenderSystem.setShaderFogEnd(FOG_END);
        RenderSystem.setShaderFogShape(shape);
    }
}
