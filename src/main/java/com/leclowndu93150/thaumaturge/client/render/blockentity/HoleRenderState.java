package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class HoleRenderState extends BlockEntityRenderState {
    public final boolean[] walls = new boolean[Direction.values().length];
    public boolean anyWall;
}
