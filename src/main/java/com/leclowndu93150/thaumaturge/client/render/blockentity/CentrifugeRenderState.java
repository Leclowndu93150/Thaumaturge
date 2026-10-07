package com.leclowndu93150.thaumaturge.client.render.blockentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public final class CentrifugeRenderState extends BlockEntityRenderState {
    public final List<BlockStateModelPart> spinnerParts = new ArrayList<>();
    public float rotation;
}
