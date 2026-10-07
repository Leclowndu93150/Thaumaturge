package com.leclowndu93150.thaumaturge.client.taint;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public final class LiquidDeathClientExtensions implements IClientFluidTypeExtensions {
    private static final ResourceLocation TEXTURE = TTIds.rl("block/animatedglow");
    private static final int TINT = 0xFF4D0066;

    @Override
    public ResourceLocation getStillTexture() {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return TEXTURE;
    }

    @Override
    public int getTintColor() {
        return TINT;
    }
}
