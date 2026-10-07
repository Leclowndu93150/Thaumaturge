package com.leclowndu93150.thaumaturge.client.render.blockentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public final class EssentiaCrystalizerRenderState extends BlockEntityRenderState {
    public final List<BlockStateModelPart> crystalParts = new ArrayList<>();
    public final int[] tints = {-1};
    public Direction facing = Direction.DOWN;
    public boolean active;
    public float spin;
}
