package com.leclowndu93150.thaumaturge.client.taint;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public final class FluxGooClientExtensions implements IClientFluidTypeExtensions {
    private static final float FOG_R = 1.0F;
    private static final float FOG_G = 0.0F;
    private static final float FOG_B = 0.5F;
    private static final float FOG_START = 0.0F;
    private static final float FOG_END = 3.0F;

    @Override
    public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
        fluidFogColor.set(FOG_R, FOG_G, FOG_B, fluidFogColor.w);
    }

    @Override
    public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
        fogData.environmentalStart = FOG_START;
        fogData.environmentalEnd = FOG_END;
    }
}
