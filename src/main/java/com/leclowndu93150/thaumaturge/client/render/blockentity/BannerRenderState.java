package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class BannerRenderState extends BlockEntityRenderState {
    public float yawDegrees;
    public boolean onWall;
    public int color = -1;
    public @Nullable Identifier aspectTexture;
    public float phase;
}
